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
