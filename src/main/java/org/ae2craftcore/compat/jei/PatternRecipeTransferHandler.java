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

package org.ae2craftcore.compat.jei;

import appeng.api.stacks.GenericStack;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.menu.me.items.PatternEncodingTermMenu;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class PatternRecipeTransferHandler implements IUniversalRecipeTransferHandler<PatternEncodingTermMenu> {

    private static List<List<GenericStack>> getCraftingIngredients(IRecipeSlotsView recipeSlots) {
        var inputSlots = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
        var ingredients = new ArrayList<List<GenericStack>>(9);

        for (int i = 0; i < 9; i++) {
            ingredients.add(i < inputSlots.size() ? getIngredients(inputSlots.get(i)) : List.of());
        }

        return ingredients;
    }

    private static List<List<GenericStack>> getInputs(IRecipeSlotsView recipeSlots) {
        return recipeSlots.getSlotViews(RecipeIngredientRole.INPUT).stream()
                .map(PatternRecipeTransferHandler::getIngredients).toList();
    }

    private static List<GenericStack> getOutputs(IRecipeSlotsView recipeSlots) {
        return recipeSlots.getSlotViews(RecipeIngredientRole.OUTPUT).stream().map(PatternRecipeTransferHandler::getIngredients)
                .map(stacks -> stacks.stream().findFirst().orElse(null)).filter(Objects::nonNull).toList();
    }

    private static List<GenericStack> getIngredients(IRecipeSlotView slot) {
        var ingredients = new ArrayList<GenericStack>();
        slot.getIngredients(VanillaTypes.ITEM_STACK).map(GenericStack::fromItemStack)
                .filter(Objects::nonNull).forEach(ingredients::add);
        slot.getIngredients(NeoForgeTypes.FLUID_STACK).map(GenericStack::fromFluidStack)
                .filter(Objects::nonNull).forEach(ingredients::add);
        return ingredients;
    }

    @Override
    public @NotNull Class<? extends PatternEncodingTermMenu> getContainerClass() {
        return PatternEncodingTermMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<PatternEncodingTermMenu>> getMenuType() {
        return Optional.of(PatternEncodingTermMenu.TYPE);
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(@NotNull PatternEncodingTermMenu menu, @NotNull Object recipe,
                                               @NotNull IRecipeSlotsView recipeSlots, @NotNull Player player, boolean maxTransfer, boolean doTransfer) {
        if (!doTransfer) return null;

        var recipeHolder = recipe instanceof RecipeHolder<?> holder ? holder : null;
        var recipeValue = recipeHolder != null ? recipeHolder.value() : null;

        if (EncodingHelper.isSupportedCraftingRecipe(recipeValue)) {
            EncodingHelper.encodeCraftingRecipe(menu, recipeHolder, getCraftingIngredients(recipeSlots), stack -> true);
        } else {
            EncodingHelper.encodeProcessingRecipe(menu, getInputs(recipeSlots), getOutputs(recipeSlots));
        }

        return null;
    }
}
