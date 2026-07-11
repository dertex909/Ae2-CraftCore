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

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.compat.CompatUtil;

import java.util.ArrayList;
import java.util.List;

public class LogicAssemblerRecipeCategory implements DisplayCategory<LogicAssemblerRecipeDisplay> {
    private static final int PADDING = 5;

    public static final CategoryIdentifier<LogicAssemblerRecipeDisplay> ID = CategoryIdentifier.of(
            Identifier.fromNamespaceAndPath(Ae2craftcore.MODID, "logic_assembling")
    );

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(LogicAssemblerBlock.HOLDER.get());
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.ae2craftcore.logic_assembler");
    }

    @Override
    public CategoryIdentifier<LogicAssemblerRecipeDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public List<Widget> setupDisplay(LogicAssemblerRecipeDisplay recipeDisplay, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.wrapRenderer(bounds, new BackgroundRenderer(getDisplayWidth(recipeDisplay), getDisplayHeight())));

        var innerX = bounds.x + PADDING;
        var innerY = bounds.y + PADDING;

        widgets.add(Widgets.createTexturedWidget(CompatUtil.TEXTURE, innerX, innerY, CompatUtil.BG_U, CompatUtil.BG_V, CompatUtil.BG_WIDTH, CompatUtil.BG_HEIGHT));
        widgets.add(Widgets.wrapRenderer(bounds, new LogicAssemblerProgressBar(CompatUtil.TEXTURE, innerX + CompatUtil.PROGRESS_X, innerY + CompatUtil.PROGRESS_Y, CompatUtil.PROGRESS_WIDTH, CompatUtil.PROGRESS_HEIGHT, CompatUtil.PROGRESS_U, CompatUtil.PROGRESS_V)));

        var ingredients = recipeDisplay.getInputEntries();
        var output = recipeDisplay.getOutputEntries().getFirst();

        widgets.add(Widgets.createSlot(new Point(innerX + CompatUtil.SLOT_IN_X, innerY + CompatUtil.SLOT_TOP_Y)).disableBackground().markInput().entries(ingredients.get(0)));
        widgets.add(Widgets.createSlot(new Point(innerX + CompatUtil.SLOT_IN_X, innerY + CompatUtil.SLOT_BOTTOM_Y)).disableBackground().markInput().entries(ingredients.get(1)));
        widgets.add(Widgets.createSlot(new Point(innerX + CompatUtil.SLOT_OUT_X, innerY + CompatUtil.SLOT_OUT_Y)).disableBackground().markOutput().entries(output));

        String text = CompatUtil.formatChance(recipeDisplay.getHolder().value().chance());
        widgets.add(Widgets.createLabel(new Point(innerX + CompatUtil.CHANCE_X, innerY + CompatUtil.CHANCE_Y), Component.literal(text)).noShadow().color(0x000000));

        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return CompatUtil.BG_HEIGHT + 2 * PADDING;
    }

    @Override
    public int getDisplayWidth(LogicAssemblerRecipeDisplay display) {
        return CompatUtil.BG_WIDTH + 2 * PADDING;
    }
}
