package org.ae2craftcore.blocks.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.ae2craftcore.registry.ModMenuTypes;
import org.ae2craftcore.registry.ModRecipeTypes;
import org.jetbrains.annotations.NotNull;

import appeng.core.definitions.AEItems;

public class LogicAssemblerMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public LogicAssemblerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(7), new SimpleContainerData(2));
    }

    public LogicAssemblerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenuTypes.LOGIC_ASSEMBLER.get(), containerId);
        checkContainerSize(container, 7);
        this.container = container;
        this.data = data;

        container.startOpen(playerInventory.player);

        Level level = playerInventory.player.level();

        this.addSlot(new Slot(container, 0, 39, 23) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return isValidInputForSlot(level, container, 0, stack);
            }
        });

        this.addSlot(new Slot(container, 1, 39, 55) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return isValidInputForSlot(level, container, 1, stack);
            }
        });

        this.addSlot(new Slot(container, 2, 113, 40) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });

        for (int i = 0; i < 4; i++) {
            this.addSlot(new Slot(container, 3 + i, 152, 8 + i * 18) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return AEItems.SPEED_CARD.is(stack);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 94 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 152));
        }

        this.addDataSlots(data);
    }

    private static boolean isValidInputForSlot(Level level, Container container, int slot, ItemStack stack) {
        if (level == null) return true;

        var otherStack = container.getItem(slot == 0 ? 1 : 0);
        var recipes = level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get());

        for (var holder : recipes) {
            var recipe = holder.value();
            boolean matchesCurrent = (slot == 0) ? recipe.getTop().test(stack) : recipe.getBottom().test(stack);

            if (matchesCurrent) {
                if (otherStack.isEmpty()) return true;
                boolean matchesOther = (slot == 0) ? recipe.getBottom().test(otherStack) : recipe.getTop().test(otherStack);
                if (matchesOther) return true;
            }
        }
        return false;
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        var itemstack = ItemStack.EMPTY;
        var slot = this.slots.get(index);
        if (slot.hasItem()) {
            var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 7) {
                if (!this.moveItemStackTo(itemstack1, 7, 43, true)) return ItemStack.EMPTY;
            } else {
                if (AEItems.SPEED_CARD.is(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 3, 7, false)) {
                        if (!this.moveItemStackTo(itemstack1, 0, 2, false)) return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(itemstack1, 0, 2, false)) return ItemStack.EMPTY;
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
        return this.container.stillValid(player);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}