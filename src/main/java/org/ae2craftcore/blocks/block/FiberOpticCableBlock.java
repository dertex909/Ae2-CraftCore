package org.ae2craftcore.blocks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.ae2craftcore.blocks.blockentity.FiberOpticCableBlockEntity;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

@RegisterBlock(name = "fiber_optic_cable", resistance = 1.0f, noOcclusion = true)
public class FiberOpticCableBlock extends BaseEntityBlock {
    public static final MapCodec<FiberOpticCableBlock> CODEC = simpleCodec(FiberOpticCableBlock::new);

    private static final VoxelShape CORE_SHAPE = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);
    private static final Map<Direction, VoxelShape> SHAPES_BY_DIRECTION = new EnumMap<>(Map.of(
            Direction.NORTH, Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 6.0),
            Direction.SOUTH, Block.box(6.0, 6.0, 10.0, 10.0, 10.0, 16.0),
            Direction.WEST, Block.box(0.0, 6.0, 6.0, 6.0, 10.0, 10.0),
            Direction.EAST, Block.box(10.0, 6.0, 6.0, 16.0, 10.0, 10.0),
            Direction.DOWN, Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0),
            Direction.UP, Block.box(6.0, 10.0, 6.0, 10.0, 16.0, 10.0)
    ));

    private final VoxelShape[] cache = new VoxelShape[64];

    public FiberOpticCableBlock(Properties properties) {
        super(properties);
        for (int i = 0; i < 64; i++) {
            var shape = CORE_SHAPE;
            if ((i & 1) != 0) shape = Shapes.or(shape, SHAPES_BY_DIRECTION.get(Direction.NORTH));
            if ((i & 2) != 0) shape = Shapes.or(shape, SHAPES_BY_DIRECTION.get(Direction.EAST));
            if ((i & 4) != 0) shape = Shapes.or(shape, SHAPES_BY_DIRECTION.get(Direction.SOUTH));
            if ((i & 8) != 0) shape = Shapes.or(shape, SHAPES_BY_DIRECTION.get(Direction.WEST));
            if ((i & 16) != 0) shape = Shapes.or(shape, SHAPES_BY_DIRECTION.get(Direction.UP));
            if ((i & 32) != 0) shape = Shapes.or(shape, SHAPES_BY_DIRECTION.get(Direction.DOWN));
            cache[i] = shape.optimize();
        }
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
        return new FiberOpticCableBlockEntity(pos, state);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        var be = level.getBlockEntity(pos);
        if (be instanceof FiberOpticCableBlockEntity cable) return cache[cable.getConnectionMask()];
        int mask = 0;
        if (canConnect(level, pos.north())) mask |= 1;
        if (canConnect(level, pos.east())) mask |= 2;
        if (canConnect(level, pos.south())) mask |= 4;
        if (canConnect(level, pos.west())) mask |= 8;
        if (canConnect(level, pos.above())) mask |= 16;
        if (canConnect(level, pos.below())) mask |= 32;
        return cache[mask];
    }

    private boolean canConnect(BlockGetter level, BlockPos neighborPos) {
        var state = level.getBlockState(neighborPos);
        return state.getBlock() instanceof FiberOpticCableBlock || state.getBlock() instanceof SfpModuleBlock;
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            var be = level.getBlockEntity(pos);
            if (be instanceof FiberOpticCableBlockEntity cable) cable.recalculateConnections();
            for (var dir : Direction.values()) {
                var neighborBe = level.getBlockEntity(pos.relative(dir));
                if (neighborBe instanceof FiberOpticCableBlockEntity nCable) nCable.recalculateConnections();
            }
        }
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                   @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            var be = level.getBlockEntity(pos);
            if (be instanceof FiberOpticCableBlockEntity cable) cable.recalculateConnections();
        }
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) for (var dir : Direction.values()) {
                var neighborBe = level.getBlockEntity(pos.relative(dir));
                if (neighborBe instanceof FiberOpticCableBlockEntity nCable) nCable.recalculateConnections();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}