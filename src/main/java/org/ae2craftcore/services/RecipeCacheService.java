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
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.ae2craftcore.blocks.blockentity.MeMachineInterfaceBlockEntity;
import org.ae2craftcore.compat.extendedAE.ExtendedAeCompat;
import org.ae2craftcore.items.RecipeStorageCellItem;

import java.util.*;

import static appeng.crafting.pattern.AEPatternDecoder.INSTANCE;
import static org.ae2craftcore.Ae2craftcore.MODID;

public class RecipeCacheService implements IRecipeCacheService, IGridServiceProvider {
    private static final Set<RecipeCacheService> ALL_SERVICES = Collections.newSetFromMap(new WeakHashMap<>());
    private final IGrid grid;
    private List<IPatternDetails> cachedPatterns = Collections.emptyList();
    private long lastSignature = -1;
    private int updateCooldown = 20;

    public RecipeCacheService(IGrid grid) {
        this.grid = grid;
        ALL_SERVICES.add(this);
    }

    public void tick() {
        if (updateCooldown-- <= 0) {
            updateCooldown = 20;
            long currentSignature = calculateGridSignature();
            if (lastSignature != -1 && currentSignature != lastSignature) {
                invalidate();
                for (var machine : grid.getMachines(MeMachineInterfaceBlockEntity.class)) {
                    ICraftingProvider.requestUpdate(machine.getMainNode());
                }
                ExtendedAeCompat.requestUpdateForMatrixAssemblers(grid);
            }
        }
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
        for (var drive : RecipeStorageCellItem.getDrives(grid)) signature = 31 * signature + scanDriveSignature(drive);
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
                driveSig = 31 * driveSig + i + count * 1000L + machineCount * 100000L + System.identityHashCode(recipeCell.getCellStack());
            } else {
                driveSig = 31 * driveSig + i;
            }
        }

        return driveSig;
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

    @EventBusSubscriber(modid = MODID)
    public static class TickHandler {
        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            for (RecipeCacheService service : ALL_SERVICES) if (service != null) service.tick();
        }
    }
}