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
import net.minecraft.core.RegistryAccess;
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
        var texture = ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "textures/gui/container/logic_assembler.png");
        this.background = guiHelper.createDrawable(texture, 30, 15, 120, 62);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(LogicAssemblerBlock.HOLDER.get()));
        this.title = Component.translatable("block.ae2craftcore.logic_assembler");
        var progressStatic = guiHelper.createDrawable(texture, 197, 0, 6, 18);
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

    private static RegistryAccess getRegistryAccess() {
        var mc = Minecraft.getInstance();
        if (mc.level != null) return mc.level.registryAccess();
        if (mc.getConnection() != null) return mc.getConnection().registryAccess();
        return RegistryAccess.EMPTY;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<LogicAssemblerRecipe> holder, @NotNull IFocusGroup focuses) {
        var recipe = holder.value();

        builder.addInputSlot(9, 8).addIngredients(recipe.getTop());
        builder.addInputSlot(9, 40).addIngredients(recipe.getBottom());
        builder.addOutputSlot(83, 25).addItemStack(recipe.getResultItem(getRegistryAccess()));
    }

    @Override
    public void draw(RecipeHolder<LogicAssemblerRecipe> holder, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics, 0, 0);
        progress.draw(guiGraphics, 105, 24);

        int chance = (int) (holder.value().getChance() * 100);
        String text = chance + "%";
        var font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        guiGraphics.drawString(font, text, 92 - (textWidth >> 1), 11, 0x000000, false);
    }
}
