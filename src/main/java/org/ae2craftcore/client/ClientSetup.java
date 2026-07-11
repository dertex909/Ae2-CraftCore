/*
 * Ae2 CraftCore
 * Copyright (C) 2025-2026 dertex909
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.ae2craftcore.client;

import appeng.api.client.StorageCellModels;
import appeng.client.render.StaticItemColor;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import org.ae2craftcore.client.screen.LogicAssemblerScreen;
import org.ae2craftcore.client.screen.MeMachineInterfaceScreen;
import org.ae2craftcore.client.screen.RecipeTerminalScreen;
import org.ae2craftcore.items.RecipeStorageCellItem;
import org.ae2craftcore.registry.ModMenuTypes;

import static appeng.api.util.AEColor.TRANSPARENT;
import static net.minecraft.core.registries.BuiltInRegistries.ITEM;
import static net.minecraft.world.item.Items.AIR;
import static net.neoforged.api.distmarker.Dist.CLIENT;
import static org.ae2craftcore.Ae2craftcore.MODID;
import static org.ae2craftcore.registry.ModRecipeTypes.LOGIC_ASSEMBLING_TYPE;

@EventBusSubscriber(modid = MODID, value = CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            StorageCellModels.registerModel(RecipeStorageCellItem.RECIPE_STORAGE_CELL_1K.get(), ResourceLocation.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_1k"));
            StorageCellModels.registerModel(RecipeStorageCellItem.RECIPE_STORAGE_CELL_4K.get(), ResourceLocation.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_4k"));
            StorageCellModels.registerModel(RecipeStorageCellItem.RECIPE_STORAGE_CELL_16K.get(), ResourceLocation.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_16k"));
            StorageCellModels.registerModel(RecipeStorageCellItem.RECIPE_STORAGE_CELL_64K.get(), ResourceLocation.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_64k"));
            StorageCellModels.registerModel(RecipeStorageCellItem.RECIPE_STORAGE_CELL_256K.get(), ResourceLocation.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_256k"));
        });
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_1k")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_4k")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_16k")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_64k")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_256k")));
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.LOGIC_ASSEMBLER.get(), LogicAssemblerScreen::new);
        event.register(ModMenuTypes.ME_MACHINE_INTERFACE.get(), MeMachineInterfaceScreen::new);
        event.register(ModMenuTypes.RECIPE_TERMINAL.get(), RecipeTerminalScreen::new);
    }

    @SubscribeEvent
    public static void registerRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        event.registerRecipeCategoryFinder(LOGIC_ASSEMBLING_TYPE.get(), recipe -> RecipeBookCategories.CRAFTING_MISC);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        var recipeTerminalItem = ITEM.get(ResourceLocation.fromNamespaceAndPath(MODID, "recipe_terminal"));
        if (recipeTerminalItem != AIR) {
            var baseColor = new StaticItemColor(TRANSPARENT);
            event.register((stack, tintIndex) -> {
                int color = baseColor.getColor(stack, tintIndex);
                return color == -1 ? -1 : (color | 0xFF000000);
            }, recipeTerminalItem);
        }
    }
}