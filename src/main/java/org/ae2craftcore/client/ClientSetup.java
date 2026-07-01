package org.ae2craftcore.client;

import net.minecraft.client.RecipeBookCategories;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.client.screen.*;
import org.ae2craftcore.registry.ModMenuTypes;
import org.ae2craftcore.registry.ModRecipeTypes;

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
}