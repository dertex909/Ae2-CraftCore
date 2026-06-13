package org.ae2craftcore.blocks.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.ae2craftcore.registry.ModMenuTypes;
import org.jetbrains.annotations.NotNull;

public class SfpModuleMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final BlockPos blockPos;

    public SfpModuleMenu(int containerId) {
        this(containerId, new SimpleContainerData(3), BlockPos.ZERO);
    }

    public SfpModuleMenu(int containerId, ContainerData data, BlockPos pos) {
        super(ModMenuTypes.SFP_MODULE.get(), containerId);
        this.data = data;
        this.blockPos = pos;
        this.addDataSlots(data);
    }

    public int getChannels() {
        return this.data.get(0);
    }

    public boolean isPathValid() {
        return this.data.get(2) == 1;
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
