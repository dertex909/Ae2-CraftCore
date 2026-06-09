package org.ae2craftcore.multiblock.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface MultiblockDefinition {

    ResourceLocation id();

    List<MultiblockNodeSpec> nodes();

    List<MultiblockPortSpec> ports();

    boolean matchesController(BlockState state);

    boolean matchesPart(BlockState state);

    BlockState createPartState(Direction facing, MultiblockNodeSpec node);

    default boolean canPlaceNode(Level level, BlockPos worldPos, MultiblockNodeSpec node) {
        if (!level.isLoaded(worldPos)) return false;
        var state = level.getBlockState(worldPos);
        return state.isAir() || state.canBeReplaced();
    }

    default Optional<MultiblockPortSpec> port(String id) {
        return ports().stream().filter(port -> port.id().equals(id)).findFirst();
    }

    default @Nullable IFluidHandler getPartFluidHandler(Level level, BlockPos worldPos, BlockState state,
                                                        MultiblockNodeSpec unusedNode, @Nullable Direction direction) {
        return null;
    }
}