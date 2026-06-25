package org.ae2craftcore.blocks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.blocks.blockentity.RecipeTerminalBlockEntity;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisterBlock(name = "recipe_terminal", strength = 3.0f, resistance = 3.0f, requiresCorrectTool = true)
public class RecipeTerminalBlock extends BaseEntityBlock {

    public static DeferredHolder<Block, RecipeTerminalBlock> HOLDER;
    public static final MapCodec<RecipeTerminalBlock> CODEC = simpleCodec(RecipeTerminalBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public RecipeTerminalBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
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
        return new RecipeTerminalBlockEntity(pos, state);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RecipeTerminalBlockEntity terminal) player.openMenu(terminal, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        var direction = state.getValue(FACING);
        return switch (direction) {
            case DOWN -> Block.box(2.0D, 14.0D, 2.0D, 14.0D, 16.0D, 14.0D);
            case UP -> Block.box(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D);
            case NORTH -> Block.box(2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D);
            case SOUTH -> Block.box(2.0D, 2.0D, 0.0D, 14.0D, 14.0D, 2.0D);
            case WEST -> Block.box(14.0D, 2.0D, 2.0D, 16.0D, 14.0D, 14.0D);
            default -> Block.box(0.0D, 2.0D, 2.0D, 2.0D, 14.0D, 14.0D);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }
}