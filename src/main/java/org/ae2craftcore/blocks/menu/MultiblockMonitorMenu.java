package org.ae2craftcore.blocks.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.ae2craftcore.registry.ModMenuTypes;
import org.jetbrains.annotations.NotNull;

public class MultiblockMonitorMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final BlockPos blockPos;

    public MultiblockMonitorMenu(int containerId) {
        this(containerId, new SimpleContainerData(7), BlockPos.ZERO);
    }

    public MultiblockMonitorMenu(int containerId, ContainerData data, BlockPos pos) {
        super(ModMenuTypes.MULTIBLOCK_MONITOR.get(), containerId);
        this.data = data;
        this.blockPos = pos;
        this.addDataSlots(data);
    }

    public boolean isStructureValid() {
        return this.data.get(0) == 1;
    }

    public boolean isInventoriesValid() {
        return this.data.get(1) == 1;
    }

    public boolean isMePowered() {
        return this.data.get(2) == 1;
    }

    public int getPic1Durability() {
        return this.data.get(3);
    }

    public int getPic2Durability() {
        return this.data.get(4);
    }

    public int getPic3Durability() {
        return this.data.get(5);
    }

    public int getPic4Durability() {
        return this.data.get(6);
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
        return true;
    }
}