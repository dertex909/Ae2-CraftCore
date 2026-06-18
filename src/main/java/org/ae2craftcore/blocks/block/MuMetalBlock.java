package org.ae2craftcore.blocks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.multiblock.MultiblockValidator;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

@RegisterBlock(name = "mu_metal", strength = 2.5f, resistance = 6.0f, sound = "chain", noOcclusion = true, requiresCorrectTool = true)
public class MuMetalBlock extends Block {
    public static DeferredHolder<Block, MuMetalBlock> HOLDER;

    public MuMetalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) MultiblockValidator.notifyCryostat(level, pos);
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, isMoving);
            if (!level.isClientSide) MultiblockValidator.notifyCryostat(level, pos);
        }
    }
}