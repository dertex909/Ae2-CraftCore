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

package org.ae2craftcore.compat.extendedAE;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingProvider;
import com.glodblock.github.extendedae.common.EAESingletons;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixPattern;
import net.minecraft.world.item.ItemStack;
import org.ae2craftcore.network.packet.RecipeTerminalSyncPacket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static java.util.Locale.ROOT;

public class ExtendedAeCompat {
    private static boolean isLoaded = false;
    private static boolean init = false;

    private static void checkLoaded() {
        if (!init) {
            try {
                Class.forName("com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixPattern");
                isLoaded = true;
            } catch (Throwable e) {
                isLoaded = false;
            }
            init = true;
        }
    }

    public static void addMatrixAssemblerGroups(IGrid grid, List<RecipeTerminalSyncPacket.MachineGroupInfo> groups, HashSet<String> addedNames, HashMap<String, Integer> groupCounts) {
        checkLoaded();
        if (isLoaded) Internal.addGroups(grid, groups, addedNames, groupCounts);
    }

    public static void requestUpdateForMatrixAssemblers(IGrid grid) {
        checkLoaded();
        if (isLoaded) Internal.requestUpdate(grid);
    }

    private static class Internal {
        static void addGroups(IGrid grid, List<RecipeTerminalSyncPacket.MachineGroupInfo> groups, HashSet<String> addedNames, HashMap<String, Integer> groupCounts) {
            try {
                var matrixPatterns = grid.getMachines(TileAssemblerMatrixPattern.class);
                if (matrixPatterns != null) {
                    for (var pattern : matrixPatterns) {
                        var component = pattern.getCustomName();
                        String name = component != null ? component.getString() : "Assembler Matrix";
                        if (!name.isEmpty()) {
                            String lower = name.toLowerCase(ROOT);
                            groupCounts.put(lower, groupCounts.getOrDefault(lower, 0) + 1);
                        }
                    }
                    for (var pattern : matrixPatterns) {
                        var component = pattern.getCustomName();
                        String name = component != null ? component.getString() : "Assembler Matrix";
                        if (!name.isEmpty()) {
                            String lower = name.toLowerCase(ROOT);
                            if (addedNames.add(lower)) {
                                var icon = new ItemStack(EAESingletons.ASSEMBLER_MATRIX_PATTERN.asItem());
                                int count = groupCounts.getOrDefault(lower, 0);
                                groups.add(new RecipeTerminalSyncPacket.MachineGroupInfo(name, icon, count));
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        static void requestUpdate(IGrid grid) {
            try {
                var matrixPatterns = grid.getMachines(TileAssemblerMatrixPattern.class);
                if (matrixPatterns != null) for (var pattern : matrixPatterns) {
                    ICraftingProvider.requestUpdate(pattern.getMainNode());
                }
            } catch (Exception ignored) {
            }
        }
    }
}
