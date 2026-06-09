package org.ae2craftcore.multiblock.api;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface MultiblockPartDataHolder {

    void setMultiblockData(BlockPos masterPos, BlockPos localPos);

    @Nullable BlockPos masterPos();

    @Nullable BlockPos localPos();
}