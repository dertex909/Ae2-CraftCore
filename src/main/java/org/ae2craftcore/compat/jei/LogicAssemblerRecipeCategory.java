package org.ae2craftcore.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.recipe.LogicAssemblerRecipe;
import org.jetbrains.annotations.NotNull;

public class LogicAssemblerRecipeCategory implements IRecipeCategory<RecipeHolder<LogicAssemblerRecipe>> {
    public static final RecipeType<RecipeHolder<LogicAssemblerRecipe>> RECIPE_TYPE = RecipeType.createFromVanilla(org.ae2craftcore.registry.ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get());

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progress;
    private final Component title;

    public LogicAssemblerRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "textures/gui/container/logic_assembler.png");
        // Обрезка: x=30, y=15, width=120, height=62
        this.background = guiHelper.createDrawable(texture, 30, 15, 120, 62);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(LogicAssemblerBlock.HOLDER.get()));
        this.title = Component.translatable("block.ae2craftcore.logic_assembler");

        // Стрелка прогресса blit на текстуре x = 197, y = 0, width = 6, height = 18
        IDrawableStatic progressStatic = guiHelper.createDrawable(texture, 197, 0, 6, 18);
        this.progress = guiHelper.createAnimatedDrawable(progressStatic, 40, IDrawableAnimated.StartDirection.TOP, false);
    }

    @Override
    public @NotNull RecipeType<RecipeHolder<LogicAssemblerRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return title;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<LogicAssemblerRecipe> holder, @NotNull IFocusGroup focuses) {
        LogicAssemblerRecipe recipe = holder.value();

        // Относительные координаты слотов:
        // Верхний слот: x = 9, y = 8
        builder.addInputSlot(9, 8).addIngredients(recipe.getTop());

        // Нижний слот: x = 9, y = 40
        builder.addInputSlot(9, 40).addIngredients(recipe.getBottom());

        // Выходной слот: x = 83, y = 25
        builder.addOutputSlot(83, 25).addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
    }

    @Override
    public void draw(RecipeHolder<LogicAssemblerRecipe> holder, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Отрисовка стрелки на относительных координатах x = 105, y = 24
        progress.draw(guiGraphics, 105, 24);

        // Отрисовка шанса крафта
        int chance = (int) (holder.value().getChance() * 100);
        String text = chance + "%";
        var font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        guiGraphics.drawString(font, text, 92 - (textWidth >> 1), 11, 0x000000, false);
    }
}
