package org.ae2craftcore.compat.rei;

import com.google.common.collect.ImmutableList;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.ae2craftcore.compat.CompatUtil;
import org.ae2craftcore.recipe.LogicAssemblerRecipe;

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
                EntryIngredients.ofIngredient(recipe.getTop()),
                EntryIngredients.ofIngredient(recipe.getBottom())
        );
        this.outputs = ImmutableList.of(EntryIngredients.of(recipe.getResultItem(CompatUtil.getRegistryAccess())));
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
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(holder.id());
    }
}
