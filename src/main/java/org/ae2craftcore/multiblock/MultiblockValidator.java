package org.ae2craftcore.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.*;
import org.ae2craftcore.blocks.blockentity.CryostatBlockEntity;
import org.ae2craftcore.blocks.blockentity.PicInjectorBlockEntity;
import org.ae2craftcore.items.DewarVesselItem;
import org.ae2craftcore.items.BaseResources;
import org.ae2craftcore.registry.AutoAttachmentRegistry;

import java.util.Objects;

public class MultiblockValidator {

    public record ValidationResult(boolean isValid, String errorReason, BlockPos absolutePos, BlockPos relativePos) {
    }

    public static ValidationResult validateStructure(Level level, BlockPos center) {
        for (int dx = -8; dx <= 8; dx++) {
            for (int dy = -8; dy <= 8; dy++) {
                for (int dz = -8; dz <= 8; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    var otherPos = center.offset(dx, dy, dz);
                    var otherState = level.getBlockState(otherPos);
                    if (otherState.getBlock() == CryostatBlock.HOLDER.get()) {
                        var be = level.getBlockEntity(otherPos);
                        if (be instanceof CryostatBlockEntity otherCore) if (otherCore.isStructureValid()) {
                            return new ValidationResult(false, "Overlap with another valid Cryostat core", otherPos, new BlockPos(dx, dy, dz));
                        }
                    }
                }
            }
        }

        int opticalInterfaceCount = 0;
        int monitorCount = 0;

        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    int dist = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
                    var currentPos = center.offset(x, y, z);
                    var state = level.getBlockState(currentPos);
                    var block = state.getBlock();

                    if (block == OpticalInterfaceBlock.HOLDER.get()) {
                        opticalInterfaceCount++;
                        if (opticalInterfaceCount > 1) {
                            return new ValidationResult(false, "Maximum 1 Optical Interface is allowed", currentPos, new BlockPos(x, y, z));
                        }
                        if (!isSideCasingFace(x, y, z)) {
                            return new ValidationResult(false, "Optical Interface can only be placed on side casing faces (not top, bottom, or edges)", currentPos, new BlockPos(x, y, z));
                        }
                        var expectedFacing = getOutwardFacing(x, z);
                        if (state.getValue(OpticalInterfaceBlock.FACING) != expectedFacing) {
                            return new ValidationResult(false, "Optical Interface must face outwards", currentPos, new BlockPos(x, y, z));
                        }
                    } else if (block == MultiblockMonitorBlock.HOLDER.get()) {
                        monitorCount++;
                        if (monitorCount > 1) {
                            return new ValidationResult(false, "Maximum 1 Multiblock Monitor is allowed", currentPos, new BlockPos(x, y, z));
                        }
                        if (!isSideCasingFace(x, y, z)) {
                            return new ValidationResult(false, "Multiblock Monitor can only be placed on side casing faces (not top, bottom, or edges)", currentPos, new BlockPos(x, y, z));
                        }
                        var expectedFacing = getOutwardFacing(x, z);
                        if (state.getValue(MultiblockMonitorBlock.FACING) != expectedFacing) {
                            return new ValidationResult(false, "Multiblock Monitor must face outwards", currentPos, new BlockPos(x, y, z));
                        }
                    }

                    var res = validatePosition(x, y, z, dist, state, currentPos);
                    if (!res.isValid()) return res;
                }
            }
        }
        return new ValidationResult(true, "Success", null, null);
    }

    public static ValidationResult validateInventories(Level level, BlockPos center) {
        var cryoBE = level.getBlockEntity(center);
        if (cryoBE instanceof CryostatBlockEntity cryo) {
            var stack0 = cryo.getContainer().getItem(0);
            if (stack0.isEmpty() || !stack0.is(DewarVesselItem.DEWAR_VESSEL.get()) || Objects.requireNonNullElse(stack0.get(AutoAttachmentRegistry.VESSEL_STATE.get()), 0) != 2) {
                return new ValidationResult(false, "Cryostat slot 0 must contain a Dewar Vessel with Helium-3", center, BlockPos.ZERO);
            }
            for (int i = 1; i <= 3; i++) {
                var stackI = cryo.getContainer().getItem(i);
                if (stackI.isEmpty() || !stackI.is(DewarVesselItem.DEWAR_VESSEL.get()) || Objects.requireNonNullElse(stackI.get(AutoAttachmentRegistry.VESSEL_STATE.get()), 0) != 1) {
                    return new ValidationResult(false, "Cryostat slot " + i + " must contain a Dewar Vessel with Helium-4", center, BlockPos.ZERO);
                }
            }
        } else {
            return new ValidationResult(false, "Cryostat Block Entity not found", center, BlockPos.ZERO);
        }

        BlockPos[] injectorOffsets = {
                new BlockPos(2, 0, 0),
                new BlockPos(-2, 0, 0),
                new BlockPos(0, 0, 2),
                new BlockPos(0, 0, -2)
        };
        for (var offset : injectorOffsets) {
            var injectorPos = center.offset(offset);
            var state = level.getBlockState(injectorPos);
            if (state.is(PicInjectorBlock.HOLDER.get())) {
                var be = level.getBlockEntity(injectorPos);
                if (be instanceof PicInjectorBlockEntity injectorBE) {
                    var picStack = injectorBE.getItem(0);
                    if (picStack.isEmpty() || !picStack.is(BaseResources.PHOTONIC_INTEGRATED_CIRCUIT.get())) {
                        return new ValidationResult(false, "PIC Injector must contain a Photonic Integrated Circuit", injectorPos, offset);
                    }
                    if (picStack.getDamageValue() >= picStack.getMaxDamage()) {
                        return new ValidationResult(false, "Photonic Integrated Circuit in PIC Injector has 0 durability", injectorPos, offset);
                    }
                }
            }
        }

        return new ValidationResult(true, "Success", null, null);
    }

    private static ValidationResult validatePosition(int x, int y, int z, int dist, BlockState state, BlockPos currentPos) {
        var relPos = new BlockPos(x, y, z);
        var block = state.getBlock();

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
                    if (block != PicInjectorBlock.HOLDER.get() && block != MuMetalBlock.HOLDER.get()) {
                        return new ValidationResult(false, "Expected PIC Injector or MuMetal Block", currentPos, relPos);
                    }
                    if (block == PicInjectorBlock.HOLDER.get()) {
                        var expectedFacing = getOutwardFacing(x, z);
                        if (state.getValue(PicInjectorBlock.FACING) != expectedFacing) {
                            return new ValidationResult(false, "Expected PIC Injector facing " + expectedFacing.getName() + " (towards vacuum layer)", currentPos, relPos);
                        }
                    }
                } else {
                    if (block != MuMetalBlock.HOLDER.get()) {
                        return new ValidationResult(false, "Expected MuMetal Block", currentPos, relPos);
                    }
                }
            }
            case 3 -> {
                if (block != Blocks.AIR) {
                    return new ValidationResult(false, "Expected Air (this layer must be empty)", currentPos, relPos);
                }
            }
            case 4 -> {
                if (block != VacuumCasingBlock.HOLDER.get() && block != MuMetalBlock.HOLDER.get() && block != OpticalInterfaceBlock.HOLDER.get() && block != MultiblockMonitorBlock.HOLDER.get()) {
                    return new ValidationResult(false, "Expected Vacuum Casing, MuMetal, Optical Interface, or Multiblock Monitor Block", currentPos, relPos);
                }
            }
        }
        return new ValidationResult(true, "Success", null, null);
    }

    private static boolean isSideCasingFace(int x, int y, int z) {
        return (Math.abs(x) == 4 && Math.abs(z) < 4 && Math.abs(y) < 4) || (Math.abs(z) == 4 && Math.abs(x) < 4 && Math.abs(y) < 4);
    }

    private static Direction getOutwardFacing(int x, int z) {
        if (Math.abs(x) == 4) return x > 0 ? Direction.EAST : Direction.WEST;
        if (Math.abs(z) == 4) return z > 0 ? Direction.SOUTH : Direction.NORTH;
        if (x > 0) return Direction.EAST;
        if (x < 0) return Direction.WEST;
        if (z > 0) return Direction.SOUTH;
        return Direction.NORTH;
    }
}