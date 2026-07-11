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

package org.ae2craftcore.registry;

import appeng.api.networking.GridServices;
import appeng.api.storage.StorageCells;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.items.LuckCardItem;
import org.ae2craftcore.items.RecipeStorageCellItem;
import org.ae2craftcore.services.IRecipeCacheService;
import org.ae2craftcore.services.RecipeCacheService;

public class Ae2Setup {
    public static void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            StorageCells.addCellHandler(new RecipeStorageCellItem.RecipeCellHandler());

            Upgrades.add(AEItems.SPEED_CARD, LogicAssemblerBlock.HOLDER.get(), 4);
            Upgrades.add(LuckCardItem.LUCK_CARD_1.get(), LogicAssemblerBlock.HOLDER.get(), 1);
            Upgrades.add(LuckCardItem.LUCK_CARD_2.get(), LogicAssemblerBlock.HOLDER.get(), 1);
            Upgrades.add(LuckCardItem.LUCK_CARD_3.get(), LogicAssemblerBlock.HOLDER.get(), 1);
            Upgrades.add(LuckCardItem.LUCK_CARD_4.get(), LogicAssemblerBlock.HOLDER.get(), 1);

            GridServices.register(IRecipeCacheService.class, RecipeCacheService.class);
        });
    }
}