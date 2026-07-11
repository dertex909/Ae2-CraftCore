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

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.compat.CompatUtil;
import org.ae2craftcore.recipe.LogicAssemblerRecipe;
import org.ae2craftcore.registry.ModRecipeTypes;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class LogicAssemblerRecipeCategory implements IRecipeCategory<RecipeHolder<LogicAssemblerRecipe>> {
    public static final IRecipeHolderType<LogicAssemblerRecipe> RECIPE_TYPE = IRecipeHolderType.create(ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get());

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progress;
    private final Component title;

    public LogicAssemblerRecipeCategory(IGuiHelper guiHelper) {
        var texture = CompatUtil.TEXTURE;
        this.background = guiHelper.createDrawable(texture, CompatUtil.BG_U, CompatUtil.BG_V, CompatUtil.BG_WIDTH, CompatUtil.BG_HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(LogicAssemblerBlock.HOLDER.get()));
        this.title = Component.translatable("block.ae2craftcore.logic_assembler");
        var progressStatic = guiHelper.createDrawable(texture, CompatUtil.PROGRESS_U, CompatUtil.PROGRESS_V, CompatUtil.PROGRESS_WIDTH, CompatUtil.PROGRESS_HEIGHT);
        this.progress = guiHelper.createAnimatedDrawable(progressStatic, CompatUtil.PROGRESS_DURATION_MS / 50, IDrawableAnimated.StartDirection.BOTTOM, false);
    }

    @Override
    public @NonNull IRecipeType<RecipeHolder<LogicAssemblerRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<LogicAssemblerRecipe> holder, @NotNull IFocusGroup focuses) {
        var recipe = holder.value();

        builder.addInputSlot(CompatUtil.SLOT_IN_X, CompatUtil.SLOT_TOP_Y).add(recipe.getTop());
        builder.addInputSlot(CompatUtil.SLOT_IN_X, CompatUtil.SLOT_BOTTOM_Y).add(recipe.getBottom());
        builder.addOutputSlot(CompatUtil.SLOT_OUT_X, CompatUtil.SLOT_OUT_Y).add(recipe.getResultItem());
    }

    @Override
    public void draw(RecipeHolder<LogicAssemblerRecipe> holder, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);
        progress.draw(guiGraphics, CompatUtil.PROGRESS_X, CompatUtil.PROGRESS_Y);

        String text = CompatUtil.formatChance(holder.value().getChance());
        var font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        guiGraphics.text(font, text, CompatUtil.CHANCE_X - (textWidth >> 1), CompatUtil.CHANCE_Y, 0x000000, false);
    }
}