package org.ae2craftcore.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.ae2craftcore.blocks.block.*;

public class MultiblockValidator {

    public record ValidationResult(boolean isValid, String errorReason, BlockPos absolutePos, BlockPos relativePos) {
    }

    public static ValidationResult validate(Level level, BlockPos center) {
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    int dist = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
                    var currentPos = center.offset(x, y, z);
                    var state = level.getBlockState(currentPos);
                    var block = state.getBlock();
                    var res = validatePosition(x, y, z, dist, block, currentPos);
                    if (!res.isValid()) return res;
                }
            }
        }
        return new ValidationResult(true, "Success", null, null);
    }

    private static ValidationResult validatePosition(int x, int y, int z, int dist, Block block, BlockPos currentPos) {
        var relPos = new BlockPos(x, y, z);

        switch (dist) {
            case 1 -> {
                boolean isFace = (Math.abs(x) == 1 && y == 0 && z == 0) || (x == 0 && Math.abs(y) == 1 && z == 0) || (x == 0 && y == 0 && Math.abs(z) == 1);

                if (isFace) {
                    if (block != ShockAbsorberSpringBlock.HOLDER.get() && block != MuMetalBlock.HOLDER.get()) {
                        return new ValidationResult(false, "Expected Shock Absorbing Spring or MuMetal Block", currentPos, relPos);
                    }
                } else {
                    if (block != MuMetalBlock.HOLDER.get()) {
                        return new ValidationResult(false, "Expected MuMetal Block", currentPos, relPos);
                    }
                }
            }
            case 2 -> {
                boolean isInjector = (y == 0) && ((x == 2 && z == 0) || (x == -2 && z == 0) || (x == 0 && z == 2) || (x == 0 && z == -2));

                if (isInjector) {
                    if (block != FisInjectorBlock.HOLDER.get() && block != MuMetalBlock.HOLDER.get()) {
                        return new ValidationResult(false, "Expected FIS Injector or MuMetal Block", currentPos, relPos);
                    }
                } else {
                    if (block != MuMetalBlock.HOLDER.get() && block != MuMetalBlock.HOLDER.get()) {
                        return new ValidationResult(false, "Expected Mu-Metal or MuMetal Block", currentPos, relPos);
                    }
                }
            }
            case 3 -> {
                if (block != Blocks.AIR) {
                    return new ValidationResult(false, "Expected Air (this layer must be empty)", currentPos, relPos);
                }
            }
            case 4 -> {
                if (block != ShieldedMeshBlock.HOLDER.get() && block != MuMetalBlock.HOLDER.get()) {
                    return new ValidationResult(false, "Expected Shielded Mesh or MuMetal Block", currentPos, relPos);
                }
            }
        }
        return new ValidationResult(true, "Success", null, null);
    }
}