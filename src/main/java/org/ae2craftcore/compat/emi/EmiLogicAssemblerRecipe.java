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

    public EmiLogicAssemblerRecipe(RecipeHolder<LogicAssemblerRecipe> holder) {
        super(CATEGORY, holder.id(), 120, 62);
        this.holder = holder;

        LogicAssemblerRecipe recipe = holder.value();
        this.inputs.add(EmiIngredient.of(recipe.getTop()));
        this.inputs.add(EmiIngredient.of(recipe.getBottom()));
        this.outputs.add(EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level.registryAccess())));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        ResourceLocation background = ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "textures/gui/container/logic_assembler.png");

        // Обрезка: x=30, y=15, width=120, height=62.
        widgets.addTexture(background, 0, 0, 120, 62, 30, 15);

        // Стрелка прогресса: x=105, y=24, u=197, v=0, width=6, height=18
        widgets.addAnimatedTexture(background, 105, 24, 6, 18, 197, 0, 2000, false, true, false);

        LogicAssemblerRecipe recipe = holder.value();

        // Слоты:
        // Верхний входной слот: x = 9, y = 8
        widgets.addSlot(EmiIngredient.of(recipe.getTop()), 9, 8).drawBack(false);
        // Нижний входной слот: x = 9, y = 40
        widgets.addSlot(EmiIngredient.of(recipe.getBottom()), 9, 40).drawBack(false);
        // Выходной слот: x = 83, y = 25
        widgets.addSlot(EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level.registryAccess())), 83, 25).drawBack(false);

        // Шанс крафта
        int chance = (int) (recipe.getChance() * 100);
        String text = chance + "%";
        var font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        widgets.addText(Component.literal(text), 92 - (textWidth >> 1), 11, 0x000000, false);
    }
}
