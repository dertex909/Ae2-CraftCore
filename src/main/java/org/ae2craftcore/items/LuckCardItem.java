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

package org.ae2craftcore.items;

import appeng.items.materials.UpgradeCardItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.annotations.RegisterItem;

@RegisterItem(name = "luck_card_1", stacksTo = 16)
@RegisterItem(name = "luck_card_2", stacksTo = 16)
@RegisterItem(name = "luck_card_3", stacksTo = 16)
@RegisterItem(name = "luck_card_4", stacksTo = 16)
public class LuckCardItem extends UpgradeCardItem {

    public static DeferredHolder<Item, LuckCardItem> LUCK_CARD_1;
    public static DeferredHolder<Item, LuckCardItem> LUCK_CARD_2;
    public static DeferredHolder<Item, LuckCardItem> LUCK_CARD_3;
    public static DeferredHolder<Item, LuckCardItem> LUCK_CARD_4;

    public LuckCardItem(Properties properties) {
        super(properties);
    }
}