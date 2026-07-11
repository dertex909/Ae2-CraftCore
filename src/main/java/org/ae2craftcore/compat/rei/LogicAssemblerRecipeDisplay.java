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

import com.google.common.collect.ImmutableList;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.ae2craftcore.recipe.LogicAssemblerRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class LogicAssemblerRecipeDisplay implements Display {
    private final RecipeHolder<LogicAssemblerRecipe> holder;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;

    public LogicAssemblerRecipeDisplay(RecipeHolder<LogicAssemblerRecipe> holder) {
        this.holder = holder;
        var recipe = holder.value();
        this.inputs = ImmutableList.of(
                EntryIngredients.ofIngredient(recipe.top()),
                EntryIngredients.ofIngredient(recipe.bottom())
        );
        this.outputs = ImmutableList.of(EntryIngredients.of(recipe.getResultItem()));
    }

    public RecipeHolder<LogicAssemblerRecipe> getHolder() {
        return holder;
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return LogicAssemblerRecipeCategory.ID;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return Optional.of(holder.id().identifier());
    }

    @Override
    @Nullable
    public DisplaySerializer<? extends Display> getSerializer() {
        return null;
    }
}
