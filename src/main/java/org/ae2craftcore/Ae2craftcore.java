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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.ae2craftcore;

import appeng.api.parts.PartModels;
import appeng.items.parts.PartModelsHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.ae2craftcore.parts.RecipeTerminalPart;
import org.ae2craftcore.registry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Ae2craftcore.MODID)
public class Ae2craftcore {
    public static final String MODID = "ae2craftcore";
    public static final String MOD_NAME = "Ae2 CraftCore";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public Ae2craftcore(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("=== {} v{} ===", MOD_NAME, modContainer.getModInfo().getVersion());

        PartModels.registerModels(PartModelsHelper.createModels(RecipeTerminalPart.class));

        AutoBlockRegistry.register(modEventBus);
        AutoItemRegistry.register(modEventBus);
        AttachmentRegistry.register(modEventBus);
        AutoCreativeTabsRegistry.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        AutoBlockEntityRegistry.register(modEventBus);
        modEventBus.addListener(AutoNetworkRegistry::registerPayloads);
        modEventBus.addListener(Ae2Setup::commonSetup);
    }
}