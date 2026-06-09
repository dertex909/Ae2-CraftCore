package org.ae2craftcore.blocks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.ae2craftcore.blocks.blockentity.GenericMultiblockPartBlockEntity;
import org.ae2craftcore.multiblock.api.*;
import org.ae2craftcore.multiblock.runtime.MultiblockRuntime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class GenericMultiblockPartBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<GenericMultiblockPartBlock> CODEC = simpleCodec(GenericMultiblockPartBlock::new);

    public GenericMultiblockPartBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new GenericMultiblockPartBlockEntity(pos, state);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, @NotNull BlockPos pos,
                                                 @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide) {
            var masterPos = masterPos(level, pos);
            var definition = definition(level, pos);
            if (masterPos != null && definition.isPresent() && level.isLoaded(masterPos)) {
                var masterState = level.getBlockState(masterPos);
                if (definition.get().matchesController(masterState)) level.destroyBlock(masterPos, true);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                         @NotNull BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            var masterPos = masterPos(level, pos);
            var definition = definition(level, pos);
            if (masterPos != null && definition.isPresent() && level.isLoaded(masterPos)) {
                var masterState = level.getBlockState(masterPos);
                if (definition.get().matchesController(masterState) && masterState.hasProperty(FACING)) {
                    var masterFacing = masterState.getValue(FACING);
                    MultiblockRuntime.disassemble(level, masterPos, masterFacing, definition.get());
                    level.removeBlock(masterPos, false);
                }
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    public IFluidHandler getMasterFluidHandler(Level level, BlockPos pos) {
        var definition = definition(level, pos);
        var node = resolveNode(level, pos);
        if (definition.isEmpty() || node.isEmpty()) return null;
        return definition.get().getPartFluidHandler(level, pos, level.getBlockState(pos), node.get(), null);
    }

    private Optional<MultiblockDefinition> definition(Level level, BlockPos pos) {
        var masterPos = masterPos(level, pos);
        if (masterPos == null || !level.isLoaded(masterPos)) return Optional.empty();
        var masterState = level.getBlockState(masterPos);
        if (masterState.getBlock() instanceof MultiblockDefinitionProvider provider) {
            return Optional.of(provider.getMultiblockDefinition());
        }
        return Optional.empty();
    }

    private Optional<MultiblockNodeSpec> resolveNode(Level level, BlockPos pos) {
        var localPos = localPos(level, pos);
        var definition = definition(level, pos);
        if (localPos == null || definition.isEmpty()) return Optional.empty();
        return MultiblockRuntime.nodeAtLocalPos(definition.get(), localPos);
    }

    private List<MultiblockPortSpec> resolvePorts(Level level, BlockPos pos) {
        var localPos = localPos(level, pos);
        var definition = definition(level, pos);
        if (localPos == null || definition.isEmpty()) return List.of();
        return definition.get().ports().stream().filter(port -> port.hostLocalPos().equals(localPos))
                .toList();
    }

    @Nullable
    private Direction masterFacing(Level level, BlockPos pos) {
        var masterPos = masterPos(level, pos);
        if (masterPos == null || !level.isLoaded(masterPos)) return null;
        var masterState = level.getBlockState(masterPos);
        return masterState.hasProperty(FACING) ? masterState.getValue(FACING) : null;
    }

    @Nullable
    private BlockPos masterPos(Level level, BlockPos pos) {
        return partData(level, pos).map(MultiblockPartDataHolder::masterPos).orElse(null);
    }

    @Nullable
    private BlockPos localPos(Level level, BlockPos pos) {
        return partData(level, pos).map(MultiblockPartDataHolder::localPos).orElse(null);
    }

    private Optional<MultiblockPartDataHolder> partData(Level level, BlockPos pos) {
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MultiblockPartDataHolder dataHolder) return Optional.of(dataHolder);
        return Optional.empty();
    }
}