package org.ae2craftcore.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.compat.CompatUtil;
import org.ae2craftcore.recipe.LogicAssemblerRecipe;
import org.jetbrains.annotations.NotNull;

public class LogicAssemblerRecipeCategory implements IRecipeCategory<RecipeHolder<LogicAssemblerRecipe>> {
    public static final RecipeType<RecipeHolder<LogicAssemblerRecipe>> RECIPE_TYPE = RecipeType.createFromVanilla(org.ae2craftcore.registry.ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get());

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
        this.progress = guiHelper.createAnimatedDrawable(progressStatic, CompatUtil.PROGRESS_DURATION_MS / 50, IDrawableAnimated.StartDirection.TOP, false);
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

        builder.addInputSlot(CompatUtil.SLOT_TOP_X, CompatUtil.SLOT_TOP_Y).addIngredients(recipe.getTop());
        builder.addInputSlot(CompatUtil.SLOT_BOTTOM_X, CompatUtil.SLOT_BOTTOM_Y).addIngredients(recipe.getBottom());
        builder.addOutputSlot(CompatUtil.SLOT_OUT_X, CompatUtil.SLOT_OUT_Y).addItemStack(recipe.getResultItem(CompatUtil.getRegistryAccess()));
    }

    @Override
    public void draw(RecipeHolder<LogicAssemblerRecipe> holder, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);
        progress.draw(guiGraphics, CompatUtil.PROGRESS_X, CompatUtil.PROGRESS_Y);

        String text = CompatUtil.formatChance(holder.value().getChance());
        var font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        guiGraphics.drawString(font, text, CompatUtil.CHANCE_X - (textWidth >> 1), CompatUtil.CHANCE_Y, 0x000000, false);
    }
}
