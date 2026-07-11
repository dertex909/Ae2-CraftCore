package org.ae2craftcore.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.registry.ModRecipeTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "core");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new LogicAssemblerRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Objects.requireNonNull(Minecraft.getInstance().level);
        var recipeManager = level.getRecipeManager();
        registration.addRecipes(LogicAssemblerRecipeCategory.RECIPE_TYPE, recipeManager.getAllRecipesFor(ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(LogicAssemblerBlock.HOLDER.get()), LogicAssemblerRecipeCategory.RECIPE_TYPE);
    }
}
