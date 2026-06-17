package org.ae2craftcore.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.OpticalInterfaceBlock;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;

@RegisterBlockEntity(name = "optical_interface", blocks = {OpticalInterfaceBlock.class})
public class OpticalInterfaceBlockEntity extends BlockEntity {
    public static BlockEntityType<OpticalInterfaceBlockEntity> TYPE;

    private int powerTicks = 0;

    public OpticalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public void receivePower() {
        this.powerTicks = 80;
        this.setChanged();
    }

    public boolean isPowered() {
        return this.powerTicks > 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OpticalInterfaceBlockEntity blockEntity) {
        if (level.isClientSide) return;
        if (blockEntity.powerTicks > 0) blockEntity.powerTicks--;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("PowerTicks", this.powerTicks);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("PowerTicks")) this.powerTicks = tag.getInt("PowerTicks");
    }
}