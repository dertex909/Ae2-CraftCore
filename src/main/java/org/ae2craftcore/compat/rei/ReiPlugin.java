package org.ae2craftcore.compat.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
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
        registry.registerRecipeFiller(LogicAssemblerRecipe.class, ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get(), LogicAssemblerRecipeDisplay::new);
    }
}
