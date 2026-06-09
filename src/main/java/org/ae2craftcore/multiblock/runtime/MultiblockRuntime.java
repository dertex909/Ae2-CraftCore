package org.ae2craftcore.multiblock.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.ae2craftcore.multiblock.api.MultiblockDefinition;
import org.ae2craftcore.multiblock.api.MultiblockNodeSpec;
import org.ae2craftcore.multiblock.api.MultiblockPartDataHolder;
import org.ae2craftcore.multiblock.api.MultiblockPortSpec;

import java.util.ArrayList;
import java.util.Optional;

public final class MultiblockRuntime {

    private MultiblockRuntime() {
    }

    public static boolean canAssemble(Level level, BlockPos masterPos, Direction facing, MultiblockDefinition definition) {
        for (var node : definition.nodes()) {
            var worldPos = HorizontalMultiblockTransform.toWorld(masterPos, facing, node.localPos());
            if (!definition.canPlaceNode(level, worldPos, node)) return false;
        }
        return true;
    }

    public static void assemble(Level level, BlockPos masterPos, Direction facing, MultiblockDefinition definition) {
        var nodes = new ArrayList<>(definition.nodes());

        for (var node : nodes) {
            var worldPos = HorizontalMultiblockTransform.toWorld(masterPos, facing, node.localPos());
            level.setBlock(worldPos, definition.createPartState(facing, node), Block.UPDATE_ALL);
            var blockEntity = level.getBlockEntity(worldPos);
            if (blockEntity instanceof MultiblockPartDataHolder dataHolder) {
                dataHolder.setMultiblockData(masterPos, node.localPos());
            }
        }
    }

    public static void disassemble(Level level, BlockPos masterPos, Direction facing, MultiblockDefinition definition) {
        for (var node : definition.nodes()) {
            var worldPos = HorizontalMultiblockTransform.toWorld(masterPos, facing, node.localPos());
            if (!level.isLoaded(worldPos)) continue;
            var state = level.getBlockState(worldPos);
            if (definition.matchesPart(state)) level.removeBlock(worldPos, false);
        }
    }

    public static Optional<MultiblockPortSpec> portById(MultiblockDefinition definition, String id) {
        return definition.port(id);
    }

    public static Direction portWorldFace(Direction facing, MultiblockPortSpec port) {
        return HorizontalMultiblockTransform.rotateDirection(port.localFace(), facing);
    }

    public static BlockPos externalPortPos(BlockPos masterPos, Direction facing, MultiblockPortSpec port) {
        var externalLocalPos = port.hostLocalPos().relative(port.localFace());
        return HorizontalMultiblockTransform.toWorld(masterPos, facing, externalLocalPos);
    }

    public static Optional<BlockPos> externalPortPos(BlockPos masterPos, Direction facing,
                                                     MultiblockDefinition definition, String portId) {
        return portById(definition, portId).map(port -> externalPortPos(masterPos, facing, port));
    }

    public static Optional<MultiblockNodeSpec> nodeAtLocalPos(MultiblockDefinition definition, BlockPos localPos) {
        return definition.nodes().stream().filter(node -> node.localPos().equals(localPos)).findFirst();
    }
}