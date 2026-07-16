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

package org.ae2craftcore.mixin.extendedae;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixFunction;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixPattern;
import org.ae2craftcore.services.IRecipeCacheService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.core.component.DataComponents.CUSTOM_DATA;
import static org.ae2craftcore.network.packet.RecipeTerminalSavePacket.RECIPEMACHINEGROUP;

@Mixin(value = TileAssemblerMatrixPattern.class)
public abstract class MixinTileAssemblerMatrixPattern extends TileAssemblerMatrixFunction {

    @Final
    @Shadow
    private List<IPatternDetails> patterns;

    @SuppressWarnings("DataFlowIssue")
    public MixinTileAssemblerMatrixPattern() {
        super(null, null, null);
    }

    @Inject(method = "getAvailablePatterns", at = @At("RETURN"), cancellable = true)
    private void addVirtualPatterns(CallbackInfoReturnable<List<IPatternDetails>> cir) {
        var level = this.getLevel();
        if (level == null) return;
        var grid = this.getMainNode().getGrid();
        if (grid == null) return;

        var cacheService = grid.getService(IRecipeCacheService.class);
        if (cacheService == null) return;

        var allPatterns = cacheService.getCachedPatterns(level);
        if (allPatterns.isEmpty()) return;

        var combined = new ArrayList<>(this.patterns);
        var component = this.getCustomName();
        String myName = component != null ? component.getString() : "Assembler Matrix";

        for (var pattern : allPatterns) {
            var definition = pattern.getDefinition();
            if (definition != null) {
                var stack = definition.toStack();
                var customData = stack.get(CUSTOM_DATA);
                if (customData != null) {
                    var tag = customData.copyTag();
                    if (tag.contains(RECIPEMACHINEGROUP)) {
                        String patternGroup = tag.getString(RECIPEMACHINEGROUP);
                        if (patternGroup.equalsIgnoreCase(myName)) combined.add(pattern);
                    }
                }
            }
        }

        cir.setReturnValue(combined);
    }

    @Inject(method = "pushPattern", at = @At("HEAD"), cancellable = true)
    private void acceptVirtualPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder, CallbackInfoReturnable<Boolean> cir) {
        if (!this.patterns.contains(patternDetails)) if (this.isFormed() && this.getMainNode().isActive()) {
            if (this.cluster != null) {
                boolean pushed = this.cluster.pushCraftingJob(patternDetails, inputHolder);
                cir.setReturnValue(pushed);
            }
        }
    }
}
