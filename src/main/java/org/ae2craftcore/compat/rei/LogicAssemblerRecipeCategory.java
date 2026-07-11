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
import net.minecraft.resources.ResourceLocation;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;

import java.util.ArrayList;
import java.util.List;

public class LogicAssemblerRecipeCategory implements DisplayCategory<LogicAssemblerRecipeDisplay> {
    private static final int PADDING = 5;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 62;

    public static final CategoryIdentifier<LogicAssemblerRecipeDisplay> ID = CategoryIdentifier.of(
            ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "logic_assembling")
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
        var texture = ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "textures/gui/container/logic_assembler.png");

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.wrapRenderer(bounds, new BackgroundRenderer(getDisplayWidth(recipeDisplay), getDisplayHeight())));

        var innerX = bounds.x + PADDING;
        var innerY = bounds.y + PADDING;

        widgets.add(Widgets.createTexturedWidget(texture, innerX, innerY, 30, 15, WIDTH, HEIGHT));
        widgets.add(Widgets.wrapRenderer(bounds, new LogicAssemblerProgressBar(texture, innerX + 105, innerY + 24, 6, 18, 197, 0)));

        var ingredients = recipeDisplay.getInputEntries();
        var output = recipeDisplay.getOutputEntries().getFirst();

        widgets.add(Widgets.createSlot(new Point(innerX + 9, innerY + 8)).disableBackground().markInput().entries(ingredients.get(0)));
        widgets.add(Widgets.createSlot(new Point(innerX + 9, innerY + 40)).disableBackground().markInput().entries(ingredients.get(1)));
        widgets.add(Widgets.createSlot(new Point(innerX + 83, innerY + 25)).disableBackground().markOutput().entries(output));

        int chance = (int) (recipeDisplay.getHolder().value().getChance() * 100);
        String text = chance + "%";
        widgets.add(Widgets.createLabel(new Point(innerX + 92, innerY + 11), Component.literal(text)).noShadow().color(0x000000));

        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT + 2 * PADDING;
    }

    @Override
    public int getDisplayWidth(LogicAssemblerRecipeDisplay display) {
        return WIDTH + 2 * PADDING;
    }
}
