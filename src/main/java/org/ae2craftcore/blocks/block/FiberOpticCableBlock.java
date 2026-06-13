package org.ae2craftcore.blocks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

@RegisterBlock(name = "fiber_optic_cable", resistance = 1.0f, noOcclusion = true)
public class FiberOpticCableBlock extends Block {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = new EnumMap<>(Map.of(
            Direction.NORTH, NORTH,
            Direction.EAST, EAST,
            Direction.SOUTH, SOUTH,
            Direction.WEST, WEST,
            Direction.UP, UP,
            Direction.DOWN, DOWN
    ));

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
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
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

    private int getIndex(BlockState state) {
        int i = 0;
        if (state.getValue(NORTH)) i |= 1;
        if (state.getValue(EAST)) i |= 2;
        if (state.getValue(SOUTH)) i |= 4;
        if (state.getValue(WEST)) i |= 8;
        if (state.getValue(UP)) i |= 16;
        if (state.getValue(DOWN)) i |= 32;
        return i;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return cache[getIndex(state)];
    }

    private boolean canConnect(BlockGetter level, BlockPos neighborPos) {
        var state = level.getBlockState(neighborPos);
        return state.getBlock() instanceof FiberOpticCableBlock || state.getBlock() instanceof SfpModuleBlock;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        return this.defaultBlockState()
                .setValue(NORTH, canConnect(level, pos.north()))
                .setValue(EAST, canConnect(level, pos.east()))
                .setValue(SOUTH, canConnect(level, pos.south()))
                .setValue(WEST, canConnect(level, pos.west()))
                .setValue(UP, canConnect(level, pos.above()))
                .setValue(DOWN, canConnect(level, pos.below()));
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState,
                                           @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(direction), canConnect(level, neighborPos));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }
}