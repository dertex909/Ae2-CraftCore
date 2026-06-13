package org.ae2craftcore.blocks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.blocks.blockentity.SfpModuleBlockEntity;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import appeng.api.orientation.IOrientableBlock;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;

@RegisterBlock(name = "sfp_module", strength = 3.0f, resistance = 3.0f, requiresCorrectTool = true)
public class SfpModuleBlock extends BaseEntityBlock implements IOrientableBlock {

    public static DeferredHolder<Block, SfpModuleBlock> HOLDER;
    public static final BooleanProperty IS_INPUT = BooleanProperty.create("is_input");
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final MapCodec<SfpModuleBlock> CODEC = simpleCodec(SfpModuleBlock::new);

    public SfpModuleBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(IS_INPUT, true).setValue(FACING, Direction.NORTH));
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IS_INPUT, FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return defaultBlockState().setValue(IS_INPUT, true).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new SfpModuleBlockEntity(pos, state);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide) {
            var held = player.getItemInHand(InteractionHand.MAIN_HAND);
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SfpModuleBlockEntity sfp) if (held.isEmpty()) {
                boolean currentInput = state.getValue(IS_INPUT);
                boolean newInput = !currentInput;
                level.setBlock(pos, state.setValue(IS_INPUT, newInput), 3);
                sfp.setSfpMode(newInput ? SfpModuleBlockEntity.SFPMode.INPUT : SfpModuleBlockEntity.SFPMode.OUTPUT);
                var msg = Component.literal("SFP Module mode changed to: " + (newInput ? "INPUT" : "OUTPUT"));
                player.displayClientMessage(msg, true);
            } else {
                if (state.getValue(IS_INPUT)) {
                    player.openMenu(sfp, pos);
                } else {
                    player.displayClientMessage(Component.literal("SFP Module is in OUTPUT mode. Channels are only configurable on INPUT modules!"), true);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                   @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            var be = level.getBlockEntity(pos);
            if (be instanceof SfpModuleBlockEntity sfp) sfp.markNeedsTrace();
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, SfpModuleBlockEntity.TYPE, SfpModuleBlockEntity::tick);
    }
}