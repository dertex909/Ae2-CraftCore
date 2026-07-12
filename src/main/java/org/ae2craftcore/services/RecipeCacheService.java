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
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.ae2craftcore.items.RecipeStorageCellItem;
import org.ae2craftcore.registry.AttachmentRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static appeng.crafting.pattern.AEPatternDecoder.INSTANCE;

public class RecipeCacheService implements IRecipeCacheService, IGridServiceProvider {
    private final IGrid grid;
    private List<IPatternDetails> cachedPatterns = Collections.emptyList();
    private long lastSignature = -1;

    public RecipeCacheService(IGrid grid) {
        this.grid = grid;
    }

    @Override
    public List<IPatternDetails> getCachedPatterns(Level level) {
        long currentSignature = calculateGridSignature();
        if (currentSignature == lastSignature) return this.cachedPatterns;
        this.cachedPatterns = rebuildCache(level);
        this.lastSignature = currentSignature;
        return this.cachedPatterns;
    }

    @Override
    public void invalidate() {
        this.lastSignature = -1;
    }

    public long calculateGridSignature() {
        long signature = 17;

        for (var drive : grid.getMachines(DriveBlockEntity.class)) {
            var inv = drive.getInternalInventory();
            if (inv != null) signature = 31 * signature + scanInventorySignature(inv);
        }

        for (var chest : grid.getMachines(MEChestBlockEntity.class)) {
            var inv = chest.getInternalInventory();
            if (inv != null) signature = 31 * signature + scanInventorySignature(inv);
        }

        return signature;
    }

    private long scanInventorySignature(InternalInventory inv) {
        long invSig = 0;
        for (int i = 0; i < inv.size(); i++) {
            var stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof RecipeStorageCellItem) {
                int count = stack.getOrDefault(AttachmentRegistry.RECIPE_COUNT.get(), 0);
                int machineCount = stack.getOrDefault(AttachmentRegistry.MACHINE_COUNT.get(), 0);
                invSig = 31 * invSig + i + count * 1000L + machineCount * 100000L + System.identityHashCode(stack);
            } else {
                invSig = 31 * invSig + i;
            }
        }
        return invSig;
    }

    private List<IPatternDetails> rebuildCache(Level level) {
        var patterns = RecipeStorageCellItem.getAllPatternsForGrid(grid);
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