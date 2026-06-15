package org.ae2craftcore.blocks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import appeng.block.AEBaseEntityBlock;

@RegisterBlock(name = "sfp_module", strength = 4.0f, requiresCorrectTool = true)
public class SfpModuleBlock extends AEBaseEntityBlock<SfpModuleBlockEntity> implements IOrientableBlock {

    public static DeferredHolder<Block, SfpModuleBlock> HOLDER;
    public static final BooleanProperty IS_INPUT = BooleanProperty.create("is_input");
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final MapCodec<SfpModuleBlock> CODEC = simpleCodec(SfpModuleBlock::new);

    private static final Component MSG_MODE_INPUT = Component.literal("SFP Module mode changed to: INPUT");
    private static final Component MSG_MODE_OUTPUT = Component.literal("SFP Module mode changed to: OUTPUT");
    private static final Component MSG_OUTPUT_WARNING = Component.literal("SFP Module is in OUTPUT mode. Channels are only configurable on INPUT modules!");

    public SfpModuleBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(IS_INPUT, true).setValue(FACING, Direction.NORTH));
        this.setBlockEntity(SfpModuleBlockEntity.class, null, null, null);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }

    @Override
    protected @NotNull MapCodec<? extends AEBaseEntityBlock<SfpModuleBlockEntity>> codec() {
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

    @Nullable
    @Override
    public SfpModuleBlockEntity getBlockEntity(BlockGetter level, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        return be instanceof SfpModuleBlockEntity sfp ? sfp : null;
    }

    @Nullable
    @Override
    public SfpModuleBlockEntity getBlockEntity(BlockGetter level, int x, int y, int z) {
        return this.getBlockEntity(level, new BlockPos(x, y, z));
    }

    @Override
    public BlockEntityType<SfpModuleBlockEntity> getBlockEntityType() {
        return SfpModuleBlockEntity.TYPE;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide) {
            var held = player.getItemInHand(InteractionHand.MAIN_HAND);
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SfpModuleBlockEntity sfp) {
                if (held.isEmpty()) {
                    boolean currentInput = state.getValue(IS_INPUT);
                    boolean newInput = !currentInput;
                    int savedChannels = sfp.getChannels();
                    level.removeBlockEntity(pos);
                    var newState = state.setValue(IS_INPUT, newInput);
                    level.setBlock(pos, newState, 3);
                    var newBe = level.getBlockEntity(pos);
                    if (newBe instanceof SfpModuleBlockEntity newSfp) newSfp.setChannels(savedChannels);
                    player.displayClientMessage(newInput ? MSG_MODE_INPUT : MSG_MODE_OUTPUT, true);
                } else {
                    if (state.getValue(IS_INPUT)) {
                        player.openMenu(sfp, pos);
                    } else {
                        player.displayClientMessage(MSG_OUTPUT_WARNING, true);
                    }
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
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == SfpModuleBlockEntity.TYPE ? (BlockEntityTicker<T>) (BlockEntityTicker<SfpModuleBlockEntity>) SfpModuleBlockEntity::tick : null;
    }
}