package org.ae2craftcore.blocks.menu;

import appeng.menu.AEBaseMenu;
import appeng.parts.encoding.EncodingMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.ae2craftcore.blocks.blockentity.MeMachineInterfaceBlockEntity;
import org.ae2craftcore.mixin.SlotAccessor;
import org.ae2craftcore.network.packet.RecipeTerminalSyncPacket;
import org.ae2craftcore.parts.RecipeTerminalPart;
import org.ae2craftcore.registry.ModMenuTypes;
import org.ae2craftcore.items.RecipeStorageCellItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecipeTerminalMenu extends AEBaseMenu {
    public static final int IMAGE_WIDTH = 322;
    public static final int IMAGE_HEIGHT = 236;

    public static final int MACHINE_LIST_X = 8;
    public static final int MACHINE_LIST_Y = 22;
    public static final int MACHINE_LIST_WIDTH = 76;
    public static final int MACHINE_LIST_HEIGHT = 112;

    public static final int RECIPE_LIST_X = 88;
    public static final int RECIPE_LIST_Y = 22;
    public static final int RECIPE_LIST_WIDTH = 82;
    public static final int RECIPE_LIST_HEIGHT = 112;

    public static final int ENCODING_X = 178;
    public static final int ENCODING_Y = 38;
    public static final int ENCODING_WIDTH = 124;
    public static final int ENCODING_SLOT_X = ENCODING_X;
    public static final int ENCODING_SLOT_Y = ENCODING_Y;

    public static final int MODE_TABS_X = ENCODING_X + ENCODING_WIDTH + 2;
    public static final int MODE_TABS_Y = ENCODING_Y;

    public static final int PLAYER_INV_X = 80;
    public static final int PLAYER_INV_Y = 154;
    public static final int HOTBAR_Y = 212;

    private final RecipeTerminalPart part;
    private final Container phantomContainer = new SimpleContainer(36);
    private final List<RecipePhantomSlot> encodingSlots = new ArrayList<>();
    private final List<Slot> processingInputSlots = new ArrayList<>();
    private final List<Slot> processingOutputSlots = new ArrayList<>();
    private int processingScrollOffset = 0;
    private String selectedGroup = "";
    private EncodingMode encodingMode = EncodingMode.PROCESSING;
    private final List<ItemStack> clientRecipes = new ArrayList<>();
    private final List<String> clientGroups = new ArrayList<>();
    private boolean firstSync = true;

    public RecipeTerminalMenu(int containerId, Inventory playerInventory, RecipeTerminalPart part) {
        super(ModMenuTypes.RECIPE_TERMINAL.get(), containerId, playerInventory, part);
        this.part = part;

        this.addEncodingModeSlots();
        this.setEncodingMode(EncodingMode.PROCESSING);

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * 18, HOTBAR_Y));
        }
    }

    private void addEncodingModeSlots() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addPhantomSlot(EncodingMode.CRAFTING, col + row * 3, ENCODING_SLOT_X + 15 + col * 18, ENCODING_SLOT_Y + 5 + row * 18);
            }
        }

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 3; col++) {
                var slot = new RecipePhantomSlot(this.phantomContainer, col + row * 3, ENCODING_SLOT_X + 15 + col * 18, ENCODING_SLOT_Y + 7 + row * 18, EncodingMode.PROCESSING);
                this.encodingSlots.add(slot);
                var added = this.addSlot(slot);
                this.processingInputSlots.add(added);
            }
        }
        for (int i = 0; i < 9; i++) {
            var slot = new RecipePhantomSlot(this.phantomContainer, 27 + i, ENCODING_SLOT_X + 100, ENCODING_SLOT_Y + 7 + i * 18, EncodingMode.PROCESSING);
            this.encodingSlots.add(slot);
            var added = this.addSlot(slot);
            this.processingOutputSlots.add(added);
        }

        this.addPhantomSlot(EncodingMode.SMITHING_TABLE, 0, ENCODING_SLOT_X + 15, ENCODING_SLOT_Y + 23);
        this.addPhantomSlot(EncodingMode.SMITHING_TABLE, 1, ENCODING_SLOT_X + 33, ENCODING_SLOT_Y + 23);
        this.addPhantomSlot(EncodingMode.SMITHING_TABLE, 2, ENCODING_SLOT_X + 51, ENCODING_SLOT_Y + 23);

        this.addPhantomSlot(EncodingMode.STONECUTTING, 0, ENCODING_SLOT_X + 15, ENCODING_SLOT_Y + 23);
    }

    private void addPhantomSlot(EncodingMode mode, int containerSlot, int x, int y) {
        var slot = new RecipePhantomSlot(this.phantomContainer, containerSlot, x, y, mode);
        this.encodingSlots.add(slot);
        this.addSlot(slot);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.firstSync) {
            this.firstSync = false;
            this.syncRecipesToClient();
        }
    }

    public void setQuantumValid() {
    }

    public void syncRecipesToClient() {
        if (this.part == null) return;
        var node = this.part.getGridNode();
        if (node == null) return;
        var grid = node.getGrid();
        if (grid == null) return;

        boolean isQuantumValid = RecipeStorageCellItem.isQuantumComputerValidForGrid(grid, this.part.getLevel());
        var list = RecipeStorageCellItem.getAllPatternsForGrid(grid);

        var groups = new ArrayList<String>();
        try {
            var machines = grid.getMachines(MeMachineInterfaceBlockEntity.class);
            if (machines != null) for (var machine : machines) {
                String name = machine.getInterfaceName();
                if (name != null && !name.isEmpty() && !groups.contains(name)) groups.add(name);
            }
        } catch (Exception e) {
            org.ae2craftcore.Ae2craftcore.LOGGER.error("Failed to query ME Machine Interfaces on grid: ", e);
        }

        if (this.getPlayer() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new RecipeTerminalSyncPacket(list, groups, isQuantumValid));
        }
    }

    public void setClientRecipes(List<ItemStack> recipes) {
        this.clientRecipes.clear();
        this.clientRecipes.addAll(recipes);
    }

    public List<ItemStack> getClientRecipes() {
        return this.clientRecipes;
    }

    public void setClientGroups(List<String> groups) {
        this.clientGroups.clear();
        this.clientGroups.addAll(groups);
    }

    public List<String> getClientGroups() {
        return this.clientGroups;
    }

    public RecipeTerminalPart getPart() {
        return this.part;
    }

    public Container getPhantomContainer() {
        return this.phantomContainer;
    }

    public int getProcessingScrollOffset() {
        return this.processingScrollOffset;
    }

    public void setProcessingScrollOffset(int scrollOffset) {
        this.processingScrollOffset = scrollOffset;
        this.updateProcessingSlots();
    }

    public void updateProcessingSlots() {
        for (int i = 0; i < this.processingInputSlots.size(); i++) {
            var slot = this.processingInputSlots.get(i);
            int row = i / 3;
            int effectiveRow = row - this.processingScrollOffset;
            ((SlotAccessor) slot).ae2craftcore$setY(ENCODING_SLOT_Y + 7 + effectiveRow * 18);
        }
        for (int row = 0; row < this.processingOutputSlots.size(); row++) {
            var slot = this.processingOutputSlots.get(row);
            int effectiveRow = row - this.processingScrollOffset;
            ((SlotAccessor) slot).ae2craftcore$setY(ENCODING_SLOT_Y + 7 + effectiveRow * 18);
        }
    }

    public String getSelectedGroup() {
        return this.selectedGroup;
    }

    public void setSelectedGroup(String group) {
        this.selectedGroup = group != null ? group : "";
    }

    public void setEncodingMode(EncodingMode mode) {
        this.encodingMode = mode != null ? mode : EncodingMode.PROCESSING;
    }

    public void clearEncodingSlots() {
        for (int i = 0; i < this.phantomContainer.getContainerSize(); i++) {
            this.phantomContainer.setItem(i, ItemStack.EMPTY);
        }
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (slotId >= 0 && slotId < this.encodingSlots.size()) {
            var slot = this.getSlot(slotId);
            var carried = this.getCarried();
            if (carried.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                var copy = carried.copy();
                copy.setCount(1);
                slot.set(copy);
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        var itemstack = ItemStack.EMPTY;
        var slot = this.slots.get(index);
        if (slot.hasItem()) {
            var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            int playerInventoryStart = this.encodingSlots.size();
            int hotbarStart = playerInventoryStart + 27;
            int hotbarEnd = hotbarStart + 9;

            if (index >= playerInventoryStart && index < hotbarStart) {
                if (!this.moveItemStackTo(itemstack1, hotbarStart, hotbarEnd, false)) return ItemStack.EMPTY;
            } else if (index >= hotbarStart && index < hotbarEnd) {
                if (!this.moveItemStackTo(itemstack1, playerInventoryStart, hotbarStart, false)) return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    private class RecipePhantomSlot extends Slot {
        private final EncodingMode mode;

        public RecipePhantomSlot(Container container, int slot, int x, int y, EncodingMode mode) {
            super(container, slot, x, y);
            this.mode = mode;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return true;
        }

        @Override
        public boolean mayPickup(@NotNull Player player) {
            return false;
        }

        @Override
        public boolean isActive() {
            if (RecipeTerminalMenu.this.encodingMode != this.mode) return false;
            if (this.mode == EncodingMode.PROCESSING) {
                int index = this.getContainerSlot();
                int scroll = RecipeTerminalMenu.this.processingScrollOffset;
                if (index < 27) {
                    int row = index / 3;
                    int effectiveRow = row - scroll;
                    return effectiveRow >= 0 && effectiveRow < 3;
                } else {
                    int row = index - 27;
                    int effectiveRow = row - scroll;
                    return effectiveRow >= 0 && effectiveRow < 3;
                }
            }
            return true;
        }
    }
}