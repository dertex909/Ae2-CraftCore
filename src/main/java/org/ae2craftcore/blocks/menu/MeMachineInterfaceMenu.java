package org.ae2craftcore.blocks.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.ae2craftcore.blocks.blockentity.MeMachineInterfaceBlockEntity;
import org.ae2craftcore.registry.ModMenuTypes;
import org.jetbrains.annotations.NotNull;

public class MeMachineInterfaceMenu extends AbstractContainerMenu {
    private final MeMachineInterfaceBlockEntity blockEntity;
    private final BlockPos blockPos;

    public MeMachineInterfaceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, (MeMachineInterfaceBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public MeMachineInterfaceMenu(int containerId, MeMachineInterfaceBlockEntity blockEntity) {
        super(ModMenuTypes.ME_MACHINE_INTERFACE.get(), containerId);
        this.blockEntity = blockEntity;
        this.blockPos = blockEntity != null ? blockEntity.getBlockPos() : BlockPos.ZERO;
    }

    public MeMachineInterfaceBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.blockEntity == null) return false;
        return player.level().getBlockEntity(this.blockPos) == this.blockEntity;
    }
}