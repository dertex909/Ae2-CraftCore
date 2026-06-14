package org.ae2craftcore.blocks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

@RegisterBlock(name = "fiber_optic_cable", resistance = 1.0f, noOcclusion = true)
public class FiberOpticCableBlock extends Block {
    public static final MapCodec<FiberOpticCableBlock> CODEC = simpleCodec(FiberOpticCableBlock::new);

    public static final IntegerProperty CONNECTION_MASK = IntegerProperty.create("connection_mask", 0, 63);

    private static final Direction[] DIRECTIONS = Direction.values();

    private static final int[] DIR_BITS = new int[6];
    private static final int[] OPPOSITE_BITS = new int[6];

    static {
        for (var dir : DIRECTIONS) {
            int ord = dir.ordinal();
            DIR_BITS[ord] = switch (dir) {
                case NORTH -> 1;
                case EAST -> 2;
                case SOUTH -> 4;
                case WEST -> 8;
                case UP -> 16;
                case DOWN -> 32;
            };
            OPPOSITE_BITS[ord] = switch (dir) {
                case NORTH -> 4;
                case EAST -> 8;
                case SOUTH -> 1;
                case WEST -> 2;
                case UP -> 32;
                case DOWN -> 16;
            };
        }
    }

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

    private static final ThreadLocal<Deque<UpdateTask>> UPDATE_QUEUE = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Boolean> IS_UPDATING = ThreadLocal.withInitial(() -> false);

    private record UpdateTask(Level level, BlockPos pos) {
    }

    public FiberOpticCableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CONNECTION_MASK, 0));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONNECTION_MASK);
    }

    @Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        int mask = state.getValue(CONNECTION_MASK);
        if (mask < 0 || mask > 63) mask = 0;
        return cache[mask];
    }

    private boolean canConnectToNeighbor(Level level, BlockPos myPos, Direction dir) {
        var neighborPos = myPos.relative(dir);
        var state = level.getBlockState(neighborPos);
        if (!(state.getBlock() instanceof FiberOpticCableBlock || state.getBlock() instanceof SfpModuleBlock)) {
            return false;
        }
        if (state.getBlock() instanceof SfpModuleBlock) {
            return state.getValue(SfpModuleBlock.FACING) == dir.getOpposite();
        }

        if (state.hasProperty(CONNECTION_MASK)) {
            int neighborMask = state.getValue(CONNECTION_MASK);
            int neighborConnectionsCount = Integer.bitCount(neighborMask);
            if (neighborConnectionsCount < 2) return true;
            int oppositeBit = OPPOSITE_BITS[dir.ordinal()];
            return (neighborMask & oppositeBit) != 0;
        }

        return false;
    }

    public int calculateNewMask(Level level, BlockPos pos, int currentMask) {
        int potentialMask = 0;
        for (var dir : DIRECTIONS) if (canConnectToNeighbor(level, pos, dir)) potentialMask |= DIR_BITS[dir.ordinal()];
        int existingMask = currentMask & potentialMask;

        Direction firstDir = null;
        int newMask = 0;
        int finalCount = 0;

        for (var dir : DIRECTIONS) {
            int dirBit = DIR_BITS[dir.ordinal()];
            if ((existingMask & dirBit) != 0) {
                newMask |= dirBit;
                if (finalCount == 0) firstDir = dir;
                finalCount++;
                if (finalCount >= 2) break;
            }
        }

        if (finalCount == 1) {
            var opposite = firstDir.getOpposite();
            int oppositeBit = DIR_BITS[opposite.ordinal()];
            if ((potentialMask & oppositeBit) != 0) {
                newMask |= oppositeBit;
                finalCount++;
            }
        }

        if (finalCount < 2) for (var dir : DIRECTIONS) {
            int dirBit = DIR_BITS[dir.ordinal()];
            if ((potentialMask & dirBit) != 0 && (newMask & dirBit) == 0) {
                newMask |= dirBit;
                finalCount++;
                if (finalCount >= 2) break;
            }
        }

        return newMask;
    }

    public static void enqueueRecalculate(Level level, BlockPos pos) {
        if (level.isClientSide()) return;
        var queue = UPDATE_QUEUE.get();
        queue.addLast(new UpdateTask(level, pos.immutable()));
        if (IS_UPDATING.get()) return;

        IS_UPDATING.set(true);
        try {
            var visited = new HashSet<BlockPos>();
            while (!queue.isEmpty()) {
                var task = queue.pollFirst();
                if (!visited.add(task.pos)) continue;

                var state = task.level.getBlockState(task.pos);
                if (state.getBlock() instanceof FiberOpticCableBlock cable) {
                    int oldMask = state.getValue(CONNECTION_MASK);
                    int newMask = cable.calculateNewMask(task.level, task.pos, oldMask);
                    if (oldMask != newMask) {
                        var newState = state.setValue(CONNECTION_MASK, newMask);
                        task.level.setBlock(task.pos, newState, 3);
                        for (var dir : DIRECTIONS) {
                            var neighborPos = task.pos.relative(dir);
                            var neighborState = task.level.getBlockState(neighborPos);
                            if (neighborState.getBlock() instanceof FiberOpticCableBlock) {
                                queue.addLast(new UpdateTask(task.level, neighborPos.immutable()));
                            }
                        }
                    }
                }
            }
        } finally {
            IS_UPDATING.set(false);
            queue.clear();
        }
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            enqueueRecalculate(level, pos);
            for (var dir : DIRECTIONS) {
                var neighborPos = pos.relative(dir);
                var neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof FiberOpticCableBlock) enqueueRecalculate(level, neighborPos);
            }
        }
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                   @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) enqueueRecalculate(level, pos);
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, isMoving);
            if (!level.isClientSide) for (var dir : DIRECTIONS) {
                var neighborPos = pos.relative(dir);
                var neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof FiberOpticCableBlock) enqueueRecalculate(level, neighborPos);
            }
        }
    }
}