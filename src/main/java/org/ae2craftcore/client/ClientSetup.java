package org.ae2craftcore.client;

import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.client.screen.*;
import org.ae2craftcore.registry.ModMenuTypes;
import org.ae2craftcore.registry.ModRecipeTypes;

import static net.minecraft.world.item.Items.AIR;

@EventBusSubscriber(modid = Ae2craftcore.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.LOGIC_ASSEMBLER.get(), LogicAssemblerScreen::new);
        event.register(ModMenuTypes.ME_MACHINE_INTERFACE.get(), MeMachineInterfaceScreen::new);
        event.register(ModMenuTypes.RECIPE_TERMINAL.get(), RecipeTerminalScreen::new);
    }

    @SubscribeEvent
    public static void registerRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        event.registerRecipeCategoryFinder(ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get(), recipe -> RecipeBookCategories.CRAFTING_MISC);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        var recipeTerminalItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal"));
        if (recipeTerminalItem != AIR) {
            var baseColor = new StaticItemColor(AEColor.TRANSPARENT);
            event.register((stack, tintIndex) -> {
                int color = baseColor.getColor(stack, tintIndex);
                return color == -1 ? -1 : (color | 0xFF000000);
            }, recipeTerminalItem);
        }
    }
}