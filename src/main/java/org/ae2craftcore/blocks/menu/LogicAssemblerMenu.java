/*
 * Ae2 CraftCore
 * Copyright (C) 2026 dertex909
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.ae2craftcore.blocks.menu;

import appeng.api.upgrades.Upgrades;
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
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.registry.ModMenuTypes;
import org.ae2craftcore.registry.ModRecipeTypes;
import org.jetbrains.annotations.NotNull;

public class LogicAssemblerMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    private ItemStack lastCheckedSlot0 = ItemStack.EMPTY;
    private ItemStack lastCheckedSlot1 = ItemStack.EMPTY;
    private boolean lastResultSlot0 = false;
    private boolean lastResultSlot1 = false;

    public static boolean canInstallUpgradeCard(Container inv, int slot, ItemStack stack) {
        int maxAllowed = Upgrades.getMaxInstallable(stack.getItem(), LogicAssemblerBlock.HOLDER.get());
        if (maxAllowed <= 0) return false;
        int currentCount = 0;

        if (slot != 3) {
            var installed = inv.getItem(3);
            if (!installed.isEmpty() && installed.is(stack.getItem())) currentCount += installed.getCount();
        }
        if (slot != 4) {
            var installed = inv.getItem(4);
            if (!installed.isEmpty() && installed.is(stack.getItem())) currentCount += installed.getCount();
        }
        if (slot != 5) {
            var installed = inv.getItem(5);
            if (!installed.isEmpty() && installed.is(stack.getItem())) currentCount += installed.getCount();
        }
        if (slot != 6) {
            var installed = inv.getItem(6);
            if (!installed.isEmpty() && installed.is(stack.getItem())) currentCount += installed.getCount();
        }
        return currentCount < maxAllowed;
    }

    public LogicAssemblerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(7), new SimpleContainerData(3));
    }

    public LogicAssemblerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenuTypes.LOGIC_ASSEMBLER.get(), containerId);
        checkContainerSize(container, 7);
        this.container = container;
        this.data = data;

        container.startOpen(playerInventory.player);
        var level = playerInventory.player.level();

        this.addSlot(new Slot(container, 0, 39, 23) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return LogicAssemblerMenu.this.isValidInputForSlot(level, 0, stack);
            }
        });

        this.addSlot(new Slot(container, 1, 39, 55) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return LogicAssemblerMenu.this.isValidInputForSlot(level, 1, stack);
            }
        });

        this.addSlot(new Slot(container, 2, 113, 40) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });

        for (int i = 0; i < 4; i++) {
            this.addSlot(new Slot(container, 3 + i, 175, 6 + i * 18) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return canInstallUpgradeCard(container, this.getSlotIndex(), stack);
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

    private boolean isValidInputForSlot(Level level, int slot, ItemStack stack) {
        if (level == null) return true;

        var otherStack = this.container.getItem(slot ^ 1);

        if (slot == 0) {
            if (ItemStack.isSameItemSameComponents(stack, this.lastCheckedSlot0)
                    && ItemStack.isSameItemSameComponents(otherStack, this.lastCheckedSlot1)) {
                return this.lastResultSlot0;
            }
        } else {
            if (ItemStack.isSameItemSameComponents(stack, this.lastCheckedSlot1)
                    && ItemStack.isSameItemSameComponents(otherStack, this.lastCheckedSlot0)) {
                return this.lastResultSlot1;
            }
        }

        var recipes = level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get());
        boolean result = false;

        if (slot == 0) {
            for (var holder : recipes) {
                var recipe = holder.value();
                if (recipe.getTop().test(stack)) {
                    if (otherStack.isEmpty()) {
                        result = true;
                        break;
                    }
                    if (recipe.getBottom().test(otherStack)) {
                        result = true;
                        break;
                    }
                }
            }
            this.lastCheckedSlot0 = stack.copy();
            this.lastCheckedSlot1 = otherStack.copy();
            this.lastResultSlot0 = result;
        } else {
            for (var holder : recipes) {
                var recipe = holder.value();
                if (recipe.getBottom().test(stack)) {
                    if (otherStack.isEmpty()) {
                        result = true;
                        break;
                    }
                    if (recipe.getTop().test(otherStack)) {
                        result = true;
                        break;
                    }
                }
            }
            this.lastCheckedSlot1 = stack.copy();
            this.lastCheckedSlot0 = otherStack.copy();
            this.lastResultSlot1 = result;
        }

        return result;
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }

    public int getCraftingChance() {
        return this.data.get(2);
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
                if (Upgrades.getMaxInstallable(itemstack1.getItem(), LogicAssemblerBlock.HOLDER.get()) > 0) {
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