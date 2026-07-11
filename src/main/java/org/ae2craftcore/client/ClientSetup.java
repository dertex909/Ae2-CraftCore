/*
 * Ae2 CraftCore
 * Copyright (C) 2026 dertex909
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
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.ae2craftcore.client.screen.LogicAssemblerScreen;
import org.ae2craftcore.client.screen.MeMachineInterfaceScreen;
import org.ae2craftcore.client.screen.RecipeTerminalScreen;
import org.ae2craftcore.items.RecipeStorageCellItem;
import org.ae2craftcore.registry.ModMenuTypes;

import static net.neoforged.api.distmarker.Dist.CLIENT;
import static org.ae2craftcore.Ae2craftcore.MODID;

@EventBusSubscriber(modid = MODID, value = CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            StorageCellModels.registerModel(RecipeStorageCellItem.RECIPE_STORAGE_CELL_1K.get(), Identifier.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_1k"));
            StorageCellModels.registerModel(RecipeStorageCellItem.RECIPE_STORAGE_CELL_4K.get(), Identifier.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_4k"));
            StorageCellModels.registerModel(RecipeStorageCellItem.RECIPE_STORAGE_CELL_16K.get(), Identifier.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_16k"));
            StorageCellModels.registerModel(RecipeStorageCellItem.RECIPE_STORAGE_CELL_64K.get(), Identifier.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_64k"));
            StorageCellModels.registerModel(RecipeStorageCellItem.RECIPE_STORAGE_CELL_256K.get(), Identifier.fromNamespaceAndPath(MODID, "block/drive/cells/recipe_storage_cell_256k"));
        });
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.LOGIC_ASSEMBLER.get(), LogicAssemblerScreen::new);
        event.register(ModMenuTypes.ME_MACHINE_INTERFACE.get(), MeMachineInterfaceScreen::new);
        event.register(ModMenuTypes.RECIPE_TERMINAL.get(), RecipeTerminalScreen::new);
    }
}