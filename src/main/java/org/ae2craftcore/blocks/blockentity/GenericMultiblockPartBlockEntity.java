package org.ae2craftcore.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.GenericMultiblockPartBlock;
import org.ae2craftcore.multiblock.api.MultiblockPartDataHolder;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisterBlockEntity(name = "multiblock_part", blocks = {GenericMultiblockPartBlock.class})
public class GenericMultiblockPartBlockEntity extends BlockEntity implements MultiblockPartDataHolder {
    public static BlockEntityType<GenericMultiblockPartBlockEntity> TYPE;

    @Nullable
    private BlockPos masterPos;
    @Nullable
    private BlockPos localPos;

    public GenericMultiblockPartBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    public void setMultiblockData(BlockPos masterPos, BlockPos localPos) {
        this.masterPos = masterPos.immutable();
        this.localPos = localPos.immutable();
        setChanged();
    }

    @Override
    public @Nullable BlockPos masterPos() {
        return masterPos;
    }

    @Override
    public @Nullable BlockPos localPos() {
        return localPos;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (masterPos != null) tag.putLong("master", masterPos.asLong());
        if (localPos != null) tag.putLong("local", localPos.asLong());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        masterPos = tag.contains("master") ? BlockPos.of(tag.getLong("master")) : null;
        localPos = tag.contains("local") ? BlockPos.of(tag.getLong("local")) : null;
    }
}