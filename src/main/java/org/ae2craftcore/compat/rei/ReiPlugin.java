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

package org.ae2craftcore.compat.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.registry.display.DisplayConsumer;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.recipe.LogicAssemblerRecipe;
import org.ae2craftcore.registry.ModRecipeTypes;

@REIPluginClient
public class ReiPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new LogicAssemblerRecipeCategory());
        registry.addWorkstations(LogicAssemblerRecipeCategory.ID, EntryStacks.of(LogicAssemblerBlock.HOLDER.get()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        ((DisplayConsumer.RecipeManagerConsumer) registry).beginRecipeFiller(LogicAssemblerRecipe.class)
                .filterType(ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get()).fill(LogicAssemblerRecipeDisplay::new);
    }
}
