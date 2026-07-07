package org.ae2craftcore.blocks.block;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.menu.locator.MenuLocators;
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
import org.ae2craftcore.blocks.blockentity.MeMachineInterfaceBlockEntity;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static appeng.menu.MenuOpener.open;
import static net.minecraft.world.Containers.dropContents;
import static org.ae2craftcore.registry.ModMenuTypes.ME_MACHINE_INTERFACE;

@RegisterBlock(name = "me_machine_interface", strength = 3.0f, resistance = 3.0f, requiresCorrectTool = true)
public class MeMachineInterfaceBlock extends BaseEntityBlock {

    public static DeferredHolder<Block, MeMachineInterfaceBlock> HOLDER;
    public static final MapCodec<MeMachineInterfaceBlock> CODEC = simpleCodec(MeMachineInterfaceBlock::new);

    public MeMachineInterfaceBlock(Properties properties) {
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
        return new MeMachineInterfaceBlockEntity(pos, state);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MeMachineInterfaceBlockEntity inter) {
                open(ME_MACHINE_INTERFACE.get(), player, MenuLocators.forBlockEntity(inter));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            var be = level.getBlockEntity(pos);
            if (be instanceof MeMachineInterfaceBlockEntity inter) inter.updateAdjacentMachine();
        }
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                   @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            var be = level.getBlockEntity(pos);
            if (be instanceof MeMachineInterfaceBlockEntity inter) {
                inter.updateAdjacentMachine();
                ICraftingProvider.requestUpdate(inter.getMainNode());
            }
        }
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            var be = level.getBlockEntity(pos);
            if (be instanceof MeMachineInterfaceBlockEntity inter) {
                dropContents(level, pos, inter.getInternalInventory().toContainer());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}