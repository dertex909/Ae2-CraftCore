/*
 * Ae2 CraftCore
 * Copyright (C) 2026 dertex909
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.ae2craftcore.services;

import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.stacks.AEItemKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.ae2craftcore.items.RecipeStorageCellItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static appeng.crafting.pattern.AEPatternDecoder.INSTANCE;

public class RecipeCacheService implements IRecipeCacheService, IGridServiceProvider {
    private static final long UNINITIALIZED_SIGNATURE = -1L;

    private final IGrid grid;
    private List<IPatternDetails> cachedPatterns = Collections.emptyList();
    private long lastSignature = UNINITIALIZED_SIGNATURE;

    public RecipeCacheService(IGrid grid) {
        this.grid = grid;
    }

    @Override
    public List<IPatternDetails> getCachedPatterns(Level level) {
        long currentSignature = calculateGridSignature();

        if (currentSignature != this.lastSignature) {
            this.cachedPatterns = rebuildCache(level);
            this.lastSignature = currentSignature;
        }

        return this.cachedPatterns;
    }

    @Override
    public void invalidate() {
        this.lastSignature = UNINITIALIZED_SIGNATURE;
        this.cachedPatterns = Collections.emptyList();
    }

    public long calculateGridSignature() {
        long signature = 17;
        for (var drive : RecipeStorageCellItem.getDrives(this.grid)) {
            signature = 31 * signature + scanDriveSignature(drive);
        }
        return signature;
    }

    private long scanDriveSignature(IChestOrDrive drive) {
        long driveSig = 0;
        int cellCount = drive.getCellCount();

        for (int i = 0; i < cellCount; i++) {
            var recipeCell = RecipeStorageCellItem.getRecipeCell(drive, i);
            if (recipeCell != null) {
                int count = recipeCell.getRecipeCount();
                int machineCount = recipeCell.getMachineCount();
                var stack = recipeCell.getCellStack();
                int stackHash = stack.isEmpty() ? 0 : ItemStack.hashItemAndComponents(stack);
                driveSig = 31 * driveSig + i + count * 1000L + machineCount * 100000L + stackHash;
            } else {
                driveSig = 31 * driveSig + i;
            }
        }

        return driveSig;
    }

    private List<IPatternDetails> rebuildCache(Level level) {
        var patterns = RecipeStorageCellItem.getAllPatternsForGrid(this.grid);
        if (patterns.isEmpty()) return Collections.emptyList();

        var list = new ArrayList<IPatternDetails>(patterns.size());
        for (var patternStack : patterns) {
            var details = INSTANCE.decodePattern(AEItemKey.of(patternStack), level);
            if (details != null) list.add(details);
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public void addNode(IGridNode gridNode, CompoundTag savedData) {
    }
}