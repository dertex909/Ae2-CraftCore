package org.ae2craftcore.blocks.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.slot.FakeSlot;
import appeng.parts.encoding.EncodingMode;
import appeng.api.inventories.InternalInventory;
import appeng.menu.guisync.GuiSync;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.neoforged.neoforge.network.PacketDistributor;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.blockentity.MeMachineInterfaceBlockEntity;
import org.ae2craftcore.mixin.SlotAccessor;
import org.ae2craftcore.network.packet.RecipeTerminalSyncPacket;
import org.ae2craftcore.parts.RecipeTerminalPart;
import org.ae2craftcore.registry.ModMenuTypes;
import org.ae2craftcore.items.RecipeStorageCellItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RecipeTerminalMenu extends AEBaseMenu {
    public static final int IMAGE_WIDTH = 322;
    public static final int IMAGE_HEIGHT = 236;

    public static final int MACHINE_LIST_X = 6;
    public static final int MACHINE_LIST_Y = 16;
    public static final int MACHINE_LIST_WIDTH = 132;
    public static final int MACHINE_LIST_HEIGHT = 108;

    public static final int RECIPE_LIST_X = 171;
    public static final int RECIPE_LIST_Y = 16;
    public static final int RECIPE_LIST_WIDTH = 132;
    public static final int RECIPE_LIST_HEIGHT = 108;

    public static final int ENCODING_X = 173;
    public static final int ENCODING_Y = 156;
    public static final int ENCODING_WIDTH = 124;
    public static final int ENCODING_SLOT_X = ENCODING_X;
    public static final int ENCODING_SLOT_Y = ENCODING_Y;

    public static final int MODE_TABS_X = ENCODING_X + ENCODING_WIDTH + 22;
    public static final int MODE_TABS_Y = ENCODING_Y - 9;

    public static final int PLAYER_INV_X = 8;
    public static final int PLAYER_INV_Y = 152;
    public static final int HOTBAR_Y = 210;

    private final RecipeTerminalPart part;
    private final List<Slot> processingInputSlots = new ArrayList<>();
    private final List<Slot> processingOutputSlots = new ArrayList<>();
    private int processingScrollOffset = 0;
    private String selectedGroup = "";
    private final List<ItemStack> clientRecipes = new ArrayList<>();
    private final List<String> clientGroups = new ArrayList<>();
    private boolean firstSync = true;

    @GuiSync(97)
    public EncodingMode mode = EncodingMode.PROCESSING;
    @GuiSync(96)
    public boolean substitute = false;
    @GuiSync(95)
    public boolean substituteFluids = true;
    @GuiSync(94)
    @Nullable
    public ResourceLocation stonecuttingRecipeId;

    public RecipeTerminalMenu(int containerId, Inventory playerInventory, RecipeTerminalPart part) {
        super(ModMenuTypes.RECIPE_TERMINAL.get(), containerId, playerInventory, part);
        this.part = part;

        this.addEncodingModeSlots();
        this.registerClientAction("setStonecuttingRecipeId", ResourceLocation.class, this.part.getLogic()::setStonecuttingRecipeId);

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
        var logic = this.part.getLogic();
        var encodedInputs = logic.getEncodedInputInv().createMenuWrapper();
        var encodedOutputs = logic.getEncodedOutputInv().createMenuWrapper();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new RecipeTerminalPhantomSlot(encodedInputs, col + row * 3, ENCODING_SLOT_X + 7 + col * 18, ENCODING_SLOT_Y + 7 + row * 18, EncodingMode.CRAFTING));
            }
        }
        this.addSlot(new RecipeResultSlot(EncodingMode.CRAFTING, ENCODING_SLOT_X + 98, ENCODING_SLOT_Y + 25));

        for (int row = 0; row < 27; row++) {
            for (int col = 0; col < 3; col++) {
                int index = col + row * 3;
                var slot = new RecipeTerminalProcessingInputSlot(encodedInputs, index, ENCODING_SLOT_X + 16 + col * 18, ENCODING_SLOT_Y + 7 + row * 18);
                this.processingInputSlots.add(this.addSlot(slot));
            }
        }
        for (int i = 0; i < 27; i++) {
            var slot = new RecipeTerminalProcessingOutputSlot(encodedOutputs, i, ENCODING_SLOT_X + 101, ENCODING_SLOT_Y + 7 + i * 18);
            this.processingOutputSlots.add(this.addSlot(slot));
        }

        this.addSlot(new RecipeTerminalPhantomSlot(encodedInputs, 0, ENCODING_SLOT_X + 7, ENCODING_SLOT_Y + 25, EncodingMode.SMITHING_TABLE));
        this.addSlot(new RecipeTerminalPhantomSlot(encodedInputs, 1, ENCODING_SLOT_X + 25, ENCODING_SLOT_Y + 25, EncodingMode.SMITHING_TABLE));
        this.addSlot(new RecipeTerminalPhantomSlot(encodedInputs, 2, ENCODING_SLOT_X + 43, ENCODING_SLOT_Y + 25, EncodingMode.SMITHING_TABLE));
        this.addSlot(new RecipeResultSlot(EncodingMode.SMITHING_TABLE, ENCODING_SLOT_X + 101, ENCODING_SLOT_Y + 25));

        this.addSlot(new RecipeTerminalPhantomSlot(encodedInputs, 0, ENCODING_SLOT_X + 7, ENCODING_SLOT_Y + 25, EncodingMode.STONECUTTING));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (isServerSide() && this.part != null) {
            var logic = this.part.getLogic();
            if (this.mode != logic.getMode()) this.mode = logic.getMode();
            this.substitute = logic.isSubstitution();
            this.substituteFluids = logic.isFluidSubstitution();
            this.stonecuttingRecipeId = logic.getStonecuttingRecipeId();
        }

        if (this.firstSync) {
            this.firstSync = false;
            this.syncRecipesToClient();
        }
    }

    public void syncRecipesToClient() {
        if (this.part == null) return;
        var node = this.part.getGridNode();
        if (node == null) return;
        var grid = node.getGrid();
        if (grid == null) return;

        var list = RecipeStorageCellItem.getAllPatternsForGrid(grid);

        var groups = new ArrayList<String>();
        try {
            var machines = grid.getMachines(MeMachineInterfaceBlockEntity.class);
            if (machines != null) for (var machine : machines) {
                String name = machine.getInterfaceName();
                if (name != null && !name.isEmpty() && !groups.contains(name)) groups.add(name);
            }
        } catch (Exception e) {
            Ae2craftcore.LOGGER.error("Failed to query ME Machine Interfaces on grid: ", e);
        }

        if (this.getPlayer() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new RecipeTerminalSyncPacket(list, groups));
        }
    }

    public void setPhantomSlotCount(int slotId, int count) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            var slot = this.getSlot(slotId);
            if (slot instanceof FakeSlot) {
                var stack = slot.getItem();
                if (!stack.isEmpty()) {
                    var copy = stack.copy();
                    copy.setCount(Math.clamp(count, 1, 999999));
                    slot.set(copy);
                }
            }
        }
    }

    public boolean isProcessingOutputSlot(Slot slot) {
        return this.processingOutputSlots.contains(slot);
    }

    public boolean isProcessingInputSlot(Slot slot) {
        return this.processingInputSlots.contains(slot);
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
        for (int i = 0; i < this.processingOutputSlots.size(); i++) {
            var slot = this.processingOutputSlots.get(i);
            int effectiveRow = i - this.processingScrollOffset;
            ((SlotAccessor) slot).ae2craftcore$setY(ENCODING_SLOT_Y + 7 + effectiveRow * 18);
        }
    }

    public String getSelectedGroup() {
        return this.selectedGroup;
    }

    public void setSelectedGroup(String group) {
        this.selectedGroup = group != null ? group : "";
    }

    public EncodingMode getEncodingMode() {
        return this.mode;
    }

    public void setEncodingMode(EncodingMode mode) {
        this.mode = mode != null ? mode : EncodingMode.PROCESSING;
        if (this.part != null) this.part.getLogic().setMode(this.mode);
    }

    public void clearEncodingSlots() {
        this.part.getLogic().getEncodedInputInv().clear();
        this.part.getLogic().getEncodedOutputInv().clear();
    }

    public boolean canCycleProcessingOutputs() {
        return this.mode == EncodingMode.PROCESSING && this.processingOutputSlots.stream()
                .filter(s -> !s.getItem().isEmpty()).count() > 1;
    }

    public void cycleProcessingOutputs() {
        if (this.mode != EncodingMode.PROCESSING) return;

        var newOutputs = new ItemStack[this.processingOutputSlots.size()];
        for (int i = 0; i < this.processingOutputSlots.size(); i++) {
            newOutputs[i] = ItemStack.EMPTY;
            if (!this.processingOutputSlots.get(i).getItem().isEmpty()) {
                for (int j = 1; j < this.processingOutputSlots.size(); j++) {
                    var nextItem = this.processingOutputSlots.get((i + j) % this.processingOutputSlots.size()).getItem();
                    if (!nextItem.isEmpty()) {
                        newOutputs[i] = nextItem;
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < newOutputs.length; i++) this.processingOutputSlots.get(i).set(newOutputs[i]);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        var slot = this.slots.get(index);
        if (slot instanceof RecipeResultSlot) return ItemStack.EMPTY;
        var itemstack = ItemStack.EMPTY;
        if (slot.hasItem()) {
            var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            int playerInventoryStart = 123;
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

    public ItemStack getCraftingResult() {
        var player = this.getPlayer();
        if (player == null) return ItemStack.EMPTY;
        var level = player.level();

        var grid = NonNullList.withSize(9, ItemStack.EMPTY);
        var hasInput = false;

        for (var slot : this.slots) {
            if (slot instanceof RecipeTerminalPhantomSlot phantomSlot && phantomSlot.mode == EncodingMode.CRAFTING) {
                int index = phantomSlot.getContainerSlot();
                if (index >= 0 && index < 9) {
                    var itemStack = phantomSlot.getItem();
                    if (!itemStack.isEmpty()) {
                        grid.set(index, itemStack.copyWithCount(1));
                        hasInput = true;
                    }
                }
            }
        }
        if (!hasInput) return ItemStack.EMPTY;

        var input = CraftingInput.of(3, 3, grid);
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElse(null);
        if (recipe == null) return ItemStack.EMPTY;
        return recipe.value().assemble(input, level.registryAccess());
    }

    public ItemStack getSmithingResult() {
        var player = this.getPlayer();
        if (player == null) return ItemStack.EMPTY;
        var level = player.level();

        var templateStack = ItemStack.EMPTY;
        var baseStack = ItemStack.EMPTY;
        var additionStack = ItemStack.EMPTY;

        for (var slot : this.slots) {
            if (slot instanceof RecipeTerminalPhantomSlot phantomSlot && phantomSlot.mode == EncodingMode.SMITHING_TABLE) {
                int index = phantomSlot.getContainerSlot();
                var itemStack = phantomSlot.getItem();
                if (index == 0) templateStack = itemStack;
                else if (index == 1) baseStack = itemStack;
                else if (index == 2) additionStack = itemStack;
            }
        }

        if (templateStack.isEmpty() || baseStack.isEmpty() || additionStack.isEmpty()) return ItemStack.EMPTY;

        var input = new SmithingRecipeInput(templateStack, baseStack, additionStack);
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, level).orElse(null);
        if (recipe == null) return ItemStack.EMPTY;
        return recipe.value().assemble(input, level.registryAccess());
    }

    public class RecipeTerminalPhantomSlot extends FakeSlot {
        final EncodingMode mode;

        public RecipeTerminalPhantomSlot(InternalInventory inv, int index, int x, int y, EncodingMode mode) {
            super(inv, index);
            ((SlotAccessor) this).ae2craftcore$setX(x);
            ((SlotAccessor) this).ae2craftcore$setY(y);
            this.mode = mode;
        }

        public EncodingMode getMode() {
            return this.mode;
        }

        @Override
        public int getMaxStackSize() {
            return this.mode == EncodingMode.PROCESSING ? 999999 : 1;
        }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack) {
            return this.mode == EncodingMode.PROCESSING ? 999999 : 1;
        }

        @Override
        public boolean isActive() {
            return RecipeTerminalMenu.this.mode == this.mode;
        }
    }

    public void selectStonecutterRecipeOnServer(ResourceLocation recipeId) {
        this.sendClientAction("setStonecuttingRecipeId", recipeId);
    }

    public class RecipeTerminalProcessingInputSlot extends FakeSlot {
        public RecipeTerminalProcessingInputSlot(InternalInventory inv, int index, int x, int y) {
            super(inv, index);
            ((SlotAccessor) this).ae2craftcore$setX(x);
            ((SlotAccessor) this).ae2craftcore$setY(y);
        }

        @Override
        public int getMaxStackSize() {
            return 999999;
        }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack) {
            return 999999;
        }

        @Override
        public boolean isActive() {
            if (RecipeTerminalMenu.this.mode != EncodingMode.PROCESSING) return false;
            int row = this.getContainerSlot() / 3;
            int scroll = RecipeTerminalMenu.this.processingScrollOffset;
            int effectiveRow = row - scroll;
            return effectiveRow >= 0 && effectiveRow < 3;
        }
    }

    public class RecipeTerminalProcessingOutputSlot extends FakeSlot {
        public RecipeTerminalProcessingOutputSlot(InternalInventory inv, int index, int x, int y) {
            super(inv, index);
            ((SlotAccessor) this).ae2craftcore$setX(x);
            ((SlotAccessor) this).ae2craftcore$setY(y);
        }

        @Override
        public int getMaxStackSize() {
            return 999999;
        }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack) {
            return 999999;
        }

        @Override
        public boolean isActive() {
            if (RecipeTerminalMenu.this.mode != EncodingMode.PROCESSING) return false;
            int row = this.getContainerSlot();
            int scroll = RecipeTerminalMenu.this.processingScrollOffset;
            int effectiveRow = row - scroll;
            return effectiveRow >= 0 && effectiveRow < 3;
        }
    }

    public class RecipeResultSlot extends Slot {
        private final EncodingMode mode;

        public RecipeResultSlot(EncodingMode mode, int x, int y) {
            super(new SimpleContainer(1), 0, x, y);
            this.mode = mode;
        }

        @Override
        public @NotNull ItemStack getItem() {
            if (this.mode == EncodingMode.CRAFTING) {
                return RecipeTerminalMenu.this.getCraftingResult();
            } else if (this.mode == EncodingMode.SMITHING_TABLE) {
                return RecipeTerminalMenu.this.getSmithingResult();
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(@NotNull Player player) {
            return false;
        }

        @Override
        public boolean isActive() {
            return RecipeTerminalMenu.this.mode == this.mode;
        }
    }
}