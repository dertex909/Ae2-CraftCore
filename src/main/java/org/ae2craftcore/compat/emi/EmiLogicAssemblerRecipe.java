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

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.compat.CompatUtil;
import org.ae2craftcore.recipe.LogicAssemblerRecipe;

public class EmiLogicAssemblerRecipe extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "logic_assembling"),
            EmiStack.of(LogicAssemblerBlock.HOLDER.get())
    ) {
        @Override
        public Component getName() {
            return Component.translatable("block.ae2craftcore.logic_assembler");
        }
    };

    private final RecipeHolder<LogicAssemblerRecipe> holder;
    private final EmiIngredient topInput;
    private final EmiIngredient bottomInput;
    private final EmiStack outputStack;

    public EmiLogicAssemblerRecipe(RecipeHolder<LogicAssemblerRecipe> holder) {
        super(CATEGORY, holder.id(), CompatUtil.BG_WIDTH, CompatUtil.BG_HEIGHT);
        this.holder = holder;

        var recipe = holder.value();

        this.topInput = EmiIngredient.of(recipe.getTop());
        this.bottomInput = EmiIngredient.of(recipe.getBottom());
        this.outputStack = EmiStack.of(recipe.getResultItem(CompatUtil.getRegistryAccess()));

        this.inputs.add(this.topInput);
        this.inputs.add(this.bottomInput);
        this.outputs.add(this.outputStack);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(CompatUtil.TEXTURE, 0, 0, CompatUtil.BG_WIDTH, CompatUtil.BG_HEIGHT, CompatUtil.BG_U, CompatUtil.BG_V);
        widgets.addAnimatedTexture(CompatUtil.TEXTURE, CompatUtil.PROGRESS_X, CompatUtil.PROGRESS_Y, CompatUtil.PROGRESS_WIDTH, CompatUtil.PROGRESS_HEIGHT, CompatUtil.PROGRESS_U, CompatUtil.PROGRESS_V, CompatUtil.PROGRESS_DURATION_MS, false, true, false);

        var recipe = holder.value();

        widgets.addSlot(this.topInput, CompatUtil.SLOT_IN_X - 1, CompatUtil.SLOT_TOP_Y - 1).drawBack(false);
        widgets.addSlot(this.bottomInput, CompatUtil.SLOT_IN_X - 1, CompatUtil.SLOT_BOTTOM_Y - 1).drawBack(false);
        widgets.addSlot(this.outputStack, CompatUtil.SLOT_OUT_X - 1, CompatUtil.SLOT_OUT_Y - 1).drawBack(false).recipeContext(this);

        String text = CompatUtil.formatChance(recipe.getChance());
        var font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        widgets.addText(Component.literal(text), CompatUtil.CHANCE_X - (textWidth >> 1), CompatUtil.CHANCE_Y, 0x000000, false);
    }
}
