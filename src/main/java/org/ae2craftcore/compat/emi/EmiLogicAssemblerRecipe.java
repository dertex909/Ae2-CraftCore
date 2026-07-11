package org.ae2craftcore.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
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
    private final EmiIngredient topInput;
    private final EmiIngredient bottomInput;
    private final EmiStack outputStack;

    public EmiLogicAssemblerRecipe(RecipeHolder<LogicAssemblerRecipe> holder) {
        super(CATEGORY, holder.id(), 120, 62);
        this.holder = holder;

        var recipe = holder.value();

        this.topInput = EmiIngredient.of(recipe.getTop());
        this.bottomInput = EmiIngredient.of(recipe.getBottom());
        this.outputStack = EmiStack.of(recipe.getResultItem(getRegistryAccess()));

        this.inputs.add(this.topInput);
        this.inputs.add(this.bottomInput);
        this.outputs.add(this.outputStack);
    }

    private static RegistryAccess getRegistryAccess() {
        var mc = Minecraft.getInstance();
        if (mc.level != null) return mc.level.registryAccess();
        if (mc.getConnection() != null) return mc.getConnection().registryAccess();
        return RegistryAccess.EMPTY;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var background = ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "textures/gui/container/logic_assembler.png");

        widgets.addTexture(background, 0, 0, 120, 62, 30, 15);
        widgets.addAnimatedTexture(background, 105, 24, 6, 18, 197, 0, 2000, false, true, false);

        var recipe = holder.value();

        widgets.addSlot(this.topInput, 9, 8).drawBack(false);
        widgets.addSlot(this.bottomInput, 9, 40).drawBack(false);
        widgets.addSlot(this.outputStack, 83, 25).drawBack(false);

        int chance = (int) (recipe.getChance() * 100);
        String text = chance + "%";
        var font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        widgets.addText(Component.literal(text), 92 - (textWidth >> 1), 11, 0x000000, false);
    }
}
