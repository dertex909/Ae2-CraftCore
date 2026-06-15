package org.ae2craftcore.blocks.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.ae2craftcore.items.BaseResources;
import org.ae2craftcore.registry.ModMenuTypes;
import org.jetbrains.annotations.NotNull;

public class FisInjectorMenu extends AbstractContainerMenu {
    private final Container container;

    public FisInjectorMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(1));
    }

    public FisInjectorMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.FIS_INJECTOR.get(), containerId);
        checkContainerSize(container, 1);
        this.container = container;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, 0, 80, 26) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(BaseResources.QUANTUM_SCRAP.get());
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 62 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 120));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        var itemstack = ItemStack.EMPTY;
        var slot = this.slots.get(index);
        if (slot.hasItem()) {
            var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 1) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) return ItemStack.EMPTY;
            } else {
                if (itemstack1.is(BaseResources.QUANTUM_SCRAP.get())) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
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