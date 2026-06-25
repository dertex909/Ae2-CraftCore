package org.ae2craftcore.blocks.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.ae2craftcore.blocks.blockentity.RecipeTerminalBlockEntity;
import org.ae2craftcore.items.RecipeStorageCellItem;
import org.ae2craftcore.registry.ModMenuTypes;
import org.jetbrains.annotations.NotNull;

public class RecipeTerminalMenu extends AbstractContainerMenu {
    private final RecipeTerminalBlockEntity blockEntity;
    private final BlockPos blockPos;
    private final Container phantomContainer = new SimpleContainer(12);
    private String selectedGroup = "";

    public RecipeTerminalMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, (RecipeTerminalBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public RecipeTerminalMenu(int containerId, Inventory playerInventory, RecipeTerminalBlockEntity blockEntity) {
        super(ModMenuTypes.RECIPE_TERMINAL.get(), containerId);
        this.blockEntity = blockEntity;
        this.blockPos = blockEntity != null ? blockEntity.getBlockPos() : BlockPos.ZERO;

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

        if (blockEntity != null) {
            this.addSlot(new Slot(blockEntity.getCellInventory(), 0, 144, 40) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.getItem() instanceof RecipeStorageCellItem;
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        } else {
            this.addSlot(new Slot(new SimpleContainer(1), 0, 144, 40));
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

    public RecipeTerminalBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
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

            if (index == 12) {
                if (!this.moveItemStackTo(itemstack1, 13, 49, true)) return ItemStack.EMPTY;
            } else if (index >= 13 && index < 49) {
                if (itemstack1.getItem() instanceof RecipeStorageCellItem) {
                    if (!this.moveItemStackTo(itemstack1, 12, 13, false)) {
                        return ItemStack.EMPTY;
                    }
                }
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

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.blockEntity == null) return false;
        return player.level().getBlockEntity(this.blockPos) == this.blockEntity;
    }
}