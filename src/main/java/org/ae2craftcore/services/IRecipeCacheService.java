package org.ae2craftcore.services;

import appeng.api.networking.IGridService;
import appeng.api.crafting.IPatternDetails;
import net.minecraft.world.level.Level;

import java.util.List;

public interface IRecipeCacheService extends IGridService {
    List<IPatternDetails> getCachedPatterns(Level level);

    void invalidate();
}