package org.ae2craftcore.multiblock.api;

import org.jetbrains.annotations.NotNull;

public record MultiblockRoleId(String value) {

    public MultiblockRoleId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Multiblock role id cannot be blank");
    }

    public static MultiblockRoleId of(String value) {
        return new MultiblockRoleId(value);
    }

    @Override
    public @NotNull String toString() {
        return value;
    }
}