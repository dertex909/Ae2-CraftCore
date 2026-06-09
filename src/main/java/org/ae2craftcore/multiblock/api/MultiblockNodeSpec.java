package org.ae2craftcore.multiblock.api;

import net.minecraft.core.BlockPos;

import java.util.Map;

public record MultiblockNodeSpec(
        BlockPos localPos,
        MultiblockRoleId roleId,
        Map<String, String> properties
) {

    public MultiblockNodeSpec {
        properties = properties == null ? Map.of() : Map.copyOf(properties);
    }

    public static MultiblockNodeSpec of(BlockPos localPos, MultiblockRoleId roleId) {
        return new MultiblockNodeSpec(localPos, roleId, Map.of());
    }
}