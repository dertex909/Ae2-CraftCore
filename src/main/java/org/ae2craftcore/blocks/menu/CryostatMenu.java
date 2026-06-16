package org.ae2craftcore.blocks.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.ae2craftcore.items.DewarVesselItem;
import org.ae2craftcore.registry.AutoAttachmentRegistry;
import org.ae2craftcore.registry.ModMenuTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CryostatMenu extends AbstractContainerMenu {
    private final Container container;

    public CryostatMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(4));
    }

    public CryostatMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.CRYOSTAT.get(), containerId);
        checkContainerSize(container, 4);
        this.container = container;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, 0, 79, 18) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                if (!stack.is(DewarVesselItem.DEWAR_VESSEL.get())) return false;
                int state = Objects.requireNonNullElse(stack.get(AutoAttachmentRegistry.VESSEL_STATE.get()), 0);
                return state == 2;
            }
        });

        for (int i = 0; i < 3; i++) {
            this.addSlot(new Slot(container, i + 1, 57 + i * 22, 40) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    if (!stack.is(DewarVesselItem.DEWAR_VESSEL.get())) return false;
                    int state = Objects.requireNonNullElse(stack.get(AutoAttachmentRegistry.VESSEL_STATE.get()), 0);
                    return state == 1;
                }
            });
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 68 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 126));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        var itemstack = ItemStack.EMPTY;
        var slot = this.slots.get(index);
        if (slot.hasItem()) {
            var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 4) {
                if (!this.moveItemStackTo(itemstack1, 4, 40, true)) return ItemStack.EMPTY;
            } else {
                if (itemstack1.is(DewarVesselItem.DEWAR_VESSEL.get())) {
                    int state = Objects.requireNonNullElse(itemstack1.get(AutoAttachmentRegistry.VESSEL_STATE.get()), 0);
                    if (state == 2) {
                        if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (state == 1) {
                        if (!this.moveItemStackTo(itemstack1, 1, 4, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
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