package org.ae2craftcore.multiblock.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Map;

public record MultiblockPortSpec(
        String id,
        BlockPos hostLocalPos,
        Direction localFace,
        MultiblockIoProfile ioProfile,
        Map<String, String> properties
) {

    public MultiblockPortSpec {
        properties = properties == null ? Map.of() : Map.copyOf(properties);
    }

    public static MultiblockPortSpec controller(String id, Direction localFace, MultiblockIoProfile ioProfile) {
        return new MultiblockPortSpec(id, BlockPos.ZERO, localFace, ioProfile, Map.of());
    }

    public static MultiblockPortSpec part(String id, BlockPos hostLocalPos, Direction localFace,
                                          MultiblockIoProfile ioProfile) {
        return new MultiblockPortSpec(id, hostLocalPos, localFace, ioProfile, Map.of());
    }

    public boolean supportsFluid() {
        return ioProfile.fluidInsert() || ioProfile.fluidExtract();
    }
}
