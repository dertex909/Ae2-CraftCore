package org.ae2craftcore.client;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.client.renderer.FiberOpticCableBakedModel;
import org.ae2craftcore.client.screen.LogicAssemblerScreen;
import org.ae2craftcore.client.screen.SfpModuleScreen;
import org.ae2craftcore.registry.ModMenuTypes;
import org.ae2craftcore.registry.ModRecipeTypes;

import java.util.HashMap;

@EventBusSubscriber(modid = Ae2craftcore.MODID, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.LOGIC_ASSEMBLER.get(), LogicAssemblerScreen::new);
        event.register(ModMenuTypes.SFP_MODULE.get(), SfpModuleScreen::new);
    }

    @SubscribeEvent
    public static void registerRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        event.registerRecipeCategoryFinder(ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get(), recipe -> RecipeBookCategories.CRAFTING_MISC);
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        var models = event.getModels();
        for (var entry : new HashMap<>(models).entrySet()) {
            var loc = entry.getKey();
            if (loc.id().getNamespace().equals(Ae2craftcore.MODID) && loc.id().getPath().equals("fiber_optic_cable")) {
                models.put(loc, new FiberOpticCableBakedModel(entry.getValue()));
            }
        }
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "block/fiber_optic_cable_side"), "standalone"));
        event.register(new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "block/fiber_optic_cable_core_end"), "standalone"));
    }
}