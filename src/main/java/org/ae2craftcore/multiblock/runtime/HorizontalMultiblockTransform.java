package org.ae2craftcore.multiblock.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class HorizontalMultiblockTransform {

    private HorizontalMultiblockTransform() {
    }

    public static BlockPos rotate(BlockPos localPos, Direction facing) {
        int x = localPos.getX();
        int y = localPos.getY();
        int z = localPos.getZ();

        return switch (facing) {
            case NORTH -> new BlockPos(x, y, -z);
            case SOUTH -> new BlockPos(-x, y, z);
            case EAST -> new BlockPos(z, y, x);
            case WEST -> new BlockPos(-z, y, -x);
            default -> new BlockPos(x, y, z);
        };
    }

    public static BlockPos toWorld(BlockPos masterPos, Direction facing, BlockPos localPos) {
        return masterPos.offset(rotate(localPos, facing));
    }

    public static Direction rotateDirection(Direction localDirection, Direction facing) {
        if (localDirection.getAxis().isVertical()) return localDirection;
        var rotated = rotate(BlockPos.ZERO.relative(localDirection), facing);
        var direction = Direction.fromDelta(rotated.getX(), rotated.getY(), rotated.getZ());
        return direction == null ? localDirection : direction;
    }
}