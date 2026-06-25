package org.ae2craftcore.blocks.menu;

import appeng.menu.AEBaseMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.ae2craftcore.network.packet.RecipeTerminalSyncPacket;
import org.ae2craftcore.parts.RecipeTerminalPart;
import org.ae2craftcore.registry.ModMenuTypes;
import org.ae2craftcore.items.RecipeStorageCellItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecipeTerminalMenu extends AEBaseMenu {
    private final RecipeTerminalPart part;
    private final Container phantomContainer = new SimpleContainer(12);
    private String selectedGroup = "";
    private final List<ItemStack> clientRecipes = new ArrayList<>();
    private boolean firstSync = true;

    public RecipeTerminalMenu(int containerId, Inventory playerInventory, RecipeTerminalPart part) {
        super(ModMenuTypes.RECIPE_TERMINAL.get(), containerId, playerInventory, part);
        this.part = part;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(this.phantomContainer, col + row * 3, 84 + col * 18, 22 + row * 18) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return true;
                    }
                });
            }
        }

        for (int i = 0; i < 3; i++) {
            this.addSlot(new Slot(this.phantomContainer, 9 + i, 84 + i * 18, 84) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return true;
                }
            });
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 48 + col * 18, 138 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 48 + col * 18, 196));
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.firstSync) {
            this.firstSync = false;
            this.syncRecipesToClient();
        }
    }

    private boolean quantumValid = false;

    public boolean isQuantumValid() {
        return this.quantumValid;
    }

    public void setQuantumValid(boolean valid) {
        this.quantumValid = valid;
    }

    public void syncRecipesToClient() {
        if (this.part == null) return;
        var node = this.part.getGridNode();
        if (node == null) return;
        var grid = node.getGrid();
        if (grid == null) return;

        boolean isQuantumValid = RecipeStorageCellItem.isQuantumComputerValidForGrid(grid, this.part.getLevel());
        var list = RecipeStorageCellItem.getAllPatternsForGrid(grid);

        if (this.getPlayer() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new RecipeTerminalSyncPacket(list, isQuantumValid));
        }
    }

    public void setClientRecipes(List<ItemStack> recipes) {
        this.clientRecipes.clear();
        this.clientRecipes.addAll(recipes);
    }

    public List<ItemStack> getClientRecipes() {
        return this.clientRecipes;
    }

    public RecipeTerminalPart getPart() {
        return this.part;
    }

    public Container getPhantomContainer() {
        return this.phantomContainer;
    }

    public String getSelectedGroup() {
        return this.selectedGroup;
    }

    public void setSelectedGroup(String group) {
        this.selectedGroup = group != null ? group : "";
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (slotId >= 0 && slotId < 12) {
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

            if (index >= 12 && index < 39) {
                if (!this.moveItemStackTo(itemstack1, 39, 48, false)) return ItemStack.EMPTY;
            } else if (index >= 39 && index < 48) {
                if (!this.moveItemStackTo(itemstack1, 12, 39, false)) return ItemStack.EMPTY;
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
}