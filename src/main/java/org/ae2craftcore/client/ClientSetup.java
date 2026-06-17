package org.ae2craftcore.client;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.client.renderer.FiberOpticCableBakedModel;
import org.ae2craftcore.client.screen.*;
import org.ae2craftcore.registry.ModMenuTypes;
import org.ae2craftcore.registry.ModRecipeTypes;

import java.util.ArrayList;

@EventBusSubscriber(modid = Ae2craftcore.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.LOGIC_ASSEMBLER.get(), LogicAssemblerScreen::new);
        event.register(ModMenuTypes.PIC_INJECTOR.get(), PicInjectorScreen::new);
        event.register(ModMenuTypes.CRYOSTAT.get(), CryostatScreen::new);
        event.register(ModMenuTypes.MULTIBLOCK_MONITOR.get(), MultiblockMonitorScreen::new);
    }

    @SubscribeEvent
    public static void registerRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        event.registerRecipeCategoryFinder(ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get(), recipe -> RecipeBookCategories.CRAFTING_MISC);
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        var model = event.getModels();
        var keysToReplace = new ArrayList<ModelResourceLocation>();

        for (var loc : model.keySet()) {
            if (loc.id().getNamespace().equals(Ae2craftcore.MODID) && loc.id().getPath().equals("fiber_optic_cable")) {
                if (!loc.variant().equals("inventory")) keysToReplace.add(loc);
            }
        }

        for (var loc : keysToReplace) {
            var original = model.get(loc);
            if (original != null) model.put(loc, new FiberOpticCableBakedModel(original));
        }
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(FiberOpticCableBakedModel.SIDE_MODEL_RL);
        event.register(FiberOpticCableBakedModel.CORE_END_MODEL_RL);
    }
}