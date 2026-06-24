package org.ae2craftcore.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.ae2craftcore.blocks.block.*;
import org.ae2craftcore.blocks.blockentity.CryostatBlockEntity;
import org.ae2craftcore.blocks.blockentity.PicInjectorBlockEntity;
import org.ae2craftcore.items.DewarVesselItem;
import org.ae2craftcore.items.BaseResources;
import org.ae2craftcore.registry.AttachmentRegistry;

import java.util.Objects;

public class MultiblockValidator {

    private static final BlockPos[] INJECTOR_OFFSETS = {
            new BlockPos(2, 0, 0),
            new BlockPos(-2, 0, 0),
            new BlockPos(0, 0, 2),
            new BlockPos(0, 0, -2)
    };

    public static void notifyCryostat(Level level, BlockPos pos) {
        if (level.isClientSide()) return;

        int originX = pos.getX();
        int originY = pos.getY();
        int originZ = pos.getZ();

        int lastChunkX = Integer.MIN_VALUE;
        int lastChunkZ = Integer.MIN_VALUE;
        LevelChunk lastChunk = null;

        var tempPos = new BlockPos.MutableBlockPos();

        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    tempPos.set(originX + x, originY + y, originZ + z);

                    int chunkX = tempPos.getX() >> 4;
                    int chunkZ = tempPos.getZ() >> 4;

                    if (lastChunk == null || lastChunkX != chunkX || lastChunkZ != chunkZ) {
                        lastChunkX = chunkX;
                        lastChunkZ = chunkZ;
                        lastChunk = level.getChunkSource().getChunk(chunkX, chunkZ, false) instanceof LevelChunk c ? c : null;
                    }

                    if (lastChunk != null) {
                        var be = lastChunk.getBlockEntity(tempPos);
                        if (be instanceof CryostatBlockEntity cryo) {
                            cryo.runStructureScanAndUpdates();
                            return;
                        }
                    }
                }
            }
        }
    }

    public record ValidationResult(boolean isValid, String errorReason, BlockPos absolutePos, BlockPos relativePos) {
    }

    public static ValidationResult validateStructure(Level level, BlockPos center) {
        int originX = center.getX();
        int originY = center.getY();
        int originZ = center.getZ();

        int lastChunkX = Integer.MIN_VALUE;
        int lastChunkZ = Integer.MIN_VALUE;
        LevelChunk lastChunk = null;

        var tempPos = new BlockPos.MutableBlockPos();

        for (int dx = -8; dx <= 8; dx++) {
            for (int dy = -8; dy <= 8; dy++) {
                for (int dz = -8; dz <= 8; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    tempPos.set(originX + dx, originY + dy, originZ + dz);

                    int chunkX = tempPos.getX() >> 4;
                    int chunkZ = tempPos.getZ() >> 4;

                    if (lastChunk == null || lastChunkX != chunkX || lastChunkZ != chunkZ) {
                        lastChunkX = chunkX;
                        lastChunkZ = chunkZ;
                        lastChunk = level.getChunkSource().getChunk(chunkX, chunkZ, false) instanceof LevelChunk c ? c : null;
                    }

                    if (lastChunk != null) {
                        var otherState = lastChunk.getBlockState(tempPos);
                        if (otherState.getBlock() == CryostatBlock.HOLDER.get()) {
                            var be = lastChunk.getBlockEntity(tempPos);
                            if (be instanceof CryostatBlockEntity otherCore) if (otherCore.isStructureValid()) {
                                return new ValidationResult(false, "Overlap with another valid Cryostat core", tempPos.immutable(), new BlockPos(dx, dy, dz));
                            }
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

                    tempPos.set(originX + x, originY + y, originZ + z);

                    int chunkX = tempPos.getX() >> 4;
                    int chunkZ = tempPos.getZ() >> 4;

                    if (lastChunk == null || lastChunkX != chunkX || lastChunkZ != chunkZ) {
                        lastChunkX = chunkX;
                        lastChunkZ = chunkZ;
                        lastChunk = level.getChunkSource().getChunk(chunkX, chunkZ, false) instanceof LevelChunk c ? c : null;
                    }

                    if (lastChunk != null) {
                        var state = lastChunk.getBlockState(tempPos);
                        var block = state.getBlock();

                        if (block == OpticalInterfaceBlock.HOLDER.get()) {
                            opticalInterfaceCount++;
                            if (opticalInterfaceCount > 1) {
                                return new ValidationResult(false, "Maximum 1 Optical Interface is allowed", tempPos.immutable(), new BlockPos(x, y, z));
                            }
                            if (!isSideCasingFace(x, y, z)) {
                                return new ValidationResult(false, "Optical Interface can only be placed on side casing faces (not top, bottom, or edges)", tempPos.immutable(), new BlockPos(x, y, z));
                            }
                            var expectedFacing = getOutwardFacing(x, z);
                            if (state.getValue(OpticalInterfaceBlock.FACING) != expectedFacing) {
                                return new ValidationResult(false, "Optical Interface must face outwards", tempPos.immutable(), new BlockPos(x, y, z));
                            }
                        } else if (block == MultiblockMonitorBlock.HOLDER.get()) {
                            monitorCount++;
                            if (monitorCount > 1) {
                                return new ValidationResult(false, "Maximum 1 Multiblock Monitor is allowed", tempPos.immutable(), new BlockPos(x, y, z));
                            }
                            if (!isSideCasingFace(x, y, z)) {
                                return new ValidationResult(false, "Multiblock Monitor can only be placed on side casing faces (not top, bottom, or edges)", tempPos.immutable(), new BlockPos(x, y, z));
                            }
                            var expectedFacing = getOutwardFacing(x, z);
                            if (state.getValue(MultiblockMonitorBlock.FACING) != expectedFacing) {
                                return new ValidationResult(false, "Multiblock Monitor must face outwards", tempPos.immutable(), new BlockPos(x, y, z));
                            }
                        }

                        var res = validatePosition(x, y, z, dist, state, tempPos);
                        if (!res.isValid()) return res;
                    } else {
                        return new ValidationResult(false, "Structure chunk is not loaded", tempPos.immutable(), new BlockPos(x, y, z));
                    }
                }
            }
        }
        return new ValidationResult(true, "Success", null, null);
    }

    public static ValidationResult validateInventories(Level level, BlockPos center) {
        var cryoBE = level.getBlockEntity(center);
        if (cryoBE instanceof CryostatBlockEntity cryo) {
            var stack0 = cryo.getContainer().getItem(0);
            if (stack0.isEmpty() || !stack0.is(DewarVesselItem.DEWAR_VESSEL.get()) || Objects.requireNonNullElse(stack0.get(AttachmentRegistry.VESSEL_STATE.get()), 0) != 2) {
                return new ValidationResult(false, "Cryostat slot 0 must contain a Dewar Vessel with Helium-3", center, BlockPos.ZERO);
            }
            for (int i = 1; i <= 3; i++) {
                var stackI = cryo.getContainer().getItem(i);
                if (stackI.isEmpty() || !stackI.is(DewarVesselItem.DEWAR_VESSEL.get()) || Objects.requireNonNullElse(stackI.get(AttachmentRegistry.VESSEL_STATE.get()), 0) != 1) {
                    return new ValidationResult(false, "Cryostat slot " + i + " must contain a Dewar Vessel with Helium-4", center, BlockPos.ZERO);
                }
            }
        } else {
            return new ValidationResult(false, "Cryostat Block Entity not found", center, BlockPos.ZERO);
        }

        int originX = center.getX();
        int originY = center.getY();
        int originZ = center.getZ();

        var tempPos = new BlockPos.MutableBlockPos();

        for (var offset : INJECTOR_OFFSETS) {
            tempPos.set(originX + offset.getX(), originY + offset.getY(), originZ + offset.getZ());
            var state = level.getBlockState(tempPos);
            if (state.is(PicInjectorBlock.HOLDER.get())) {
                var be = level.getBlockEntity(tempPos);
                if (be instanceof PicInjectorBlockEntity injectorBE) {
                    var picStack = injectorBE.getItem(0);
                    if (picStack.isEmpty() || !picStack.is(BaseResources.PHOTONIC_INTEGRATED_CIRCUIT.get())) {
                        return new ValidationResult(false, "PIC Injector must contain a Photonic Integrated Circuit", tempPos.immutable(), offset);
                    }
                    if (picStack.getDamageValue() >= picStack.getMaxDamage()) {
                        return new ValidationResult(false, "Photonic Integrated Circuit in PIC Injector has 0 durability", tempPos.immutable(), offset);
                    }
                }
            }
        }

        return new ValidationResult(true, "Success", null, null);
    }

    private static ValidationResult validatePosition(int x, int y, int z, int dist, BlockState state, BlockPos.MutableBlockPos currentPos) {
        var block = state.getBlock();

        switch (dist) {
            case 1 -> {
                boolean isFace = (Math.abs(x) == 1 && y == 0 && z == 0) || (x == 0 && Math.abs(y) == 1 && z == 0) || (x == 0 && y == 0 && Math.abs(z) == 1);

                if (isFace) {
                    if (block != ShockAbsorberSpringBlock.HOLDER.get() && block != MuMetalBlock.HOLDER.get()) {
                        return new ValidationResult(false, "Expected Shock Absorbing Spring or MuMetal Block", currentPos.immutable(), new BlockPos(x, y, z));
                    }
                } else {
                    if (block != MuMetalBlock.HOLDER.get()) {
                        return new ValidationResult(false, "Expected MuMetal Block", currentPos.immutable(), new BlockPos(x, y, z));
                    }
                }
            }
            case 2 -> {
                boolean isInjector = (y == 0) && ((x == 2 && z == 0) || (x == -2 && z == 0) || (x == 0 && z == 2) || (x == 0 && z == -2));

                if (isInjector) {
                    if (block != PicInjectorBlock.HOLDER.get() && block != MuMetalBlock.HOLDER.get()) {
                        return new ValidationResult(false, "Expected PIC Injector or MuMetal Block", currentPos.immutable(), new BlockPos(x, y, z));
                    }
                    if (block == PicInjectorBlock.HOLDER.get()) {
                        var expectedFacing = getOutwardFacing(x, z);
                        if (state.getValue(PicInjectorBlock.FACING) != expectedFacing) {
                            return new ValidationResult(false, "Expected PIC Injector facing " + expectedFacing.getName() + " (towards vacuum layer)", currentPos.immutable(), new BlockPos(x, y, z));
                        }
                    }
                } else {
                    if (block != MuMetalBlock.HOLDER.get()) {
                        return new ValidationResult(false, "Expected MuMetal Block", currentPos.immutable(), new BlockPos(x, y, z));
                    }
                }
            }
            case 3 -> {
                if (block != Blocks.AIR) {
                    return new ValidationResult(false, "Expected Air (this layer must be empty)", currentPos.immutable(), new BlockPos(x, y, z));
                }
            }
            case 4 -> {
                if (block != VacuumCasingBlock.HOLDER.get() && block != MuMetalBlock.HOLDER.get() && block != OpticalInterfaceBlock.HOLDER.get() && block != MultiblockMonitorBlock.HOLDER.get()) {
                    return new ValidationResult(false, "Expected Vacuum Casing, MuMetal, Optical Interface, or Multiblock Monitor Block", currentPos.immutable(), new BlockPos(x, y, z));
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