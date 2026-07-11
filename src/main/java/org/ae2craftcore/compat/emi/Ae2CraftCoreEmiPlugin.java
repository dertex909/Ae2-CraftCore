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

package org.ae2craftcore.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.registry.ModRecipeTypes;

@EmiEntrypoint
public class Ae2CraftCoreEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(EmiLogicAssemblerRecipe.CATEGORY);
        registry.addWorkstation(EmiLogicAssemblerRecipe.CATEGORY, EmiStack.of(LogicAssemblerBlock.HOLDER.get()));

        registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get()).stream()
                .map(EmiLogicAssemblerRecipe::new).forEach(registry::addRecipe);
    }
}
