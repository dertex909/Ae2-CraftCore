package org.ae2craftcore.blocks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.blocks.blockentity.CryostatBlockEntity;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisterBlock(name = "cryostat", strength = 4.0f, resistance = 4.0f, requiresCorrectTool = true)
public class CryostatBlock extends BaseEntityBlock {

    public static DeferredHolder<Block, CryostatBlock> HOLDER;
    public static final MapCodec<CryostatBlock> CODEC = simpleCodec(CryostatBlock::new);

    public CryostatBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CryostatBlockEntity(pos, state);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CryostatBlockEntity cryostat) cryostat.checkStructureAndNotify(player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}