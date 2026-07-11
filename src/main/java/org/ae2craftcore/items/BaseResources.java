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

package org.ae2craftcore.items;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.annotations.RegisterItem;

@RegisterItem(name = "quantum_processor_1")
@RegisterItem(name = "quantum_processor_2")
@RegisterItem(name = "quantum_processor_3")
@RegisterItem(name = "quantum_processor_4")
@RegisterItem(name = "quantum_processor_5")
@RegisterItem(name = "quantum_processor_6")
@RegisterItem(name = "quantum_scrap")

@RegisterItem(name = "recipe_cell_component_1k")
@RegisterItem(name = "recipe_cell_component_4k")
@RegisterItem(name = "recipe_cell_component_16k")
@RegisterItem(name = "recipe_cell_component_64k")
@RegisterItem(name = "recipe_cell_component_256k")
@RegisterItem(name = "recipe_cell_housing")
public class BaseResources extends Item {

    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_1;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_2;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_3;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_4;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_5;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_6;
    public static DeferredHolder<Item, BaseResources> QUANTUM_SCRAP;

    public static DeferredHolder<Item, BaseResources> RECIPE_CELL_COMPONENT_1K;
    public static DeferredHolder<Item, BaseResources> RECIPE_CELL_COMPONENT_4K;
    public static DeferredHolder<Item, BaseResources> RECIPE_CELL_COMPONENT_16K;
    public static DeferredHolder<Item, BaseResources> RECIPE_CELL_COMPONENT_64K;
    public static DeferredHolder<Item, BaseResources> RECIPE_CELL_COMPONENT_256K;
    public static DeferredHolder<Item, BaseResources> RECIPE_CELL_HOUSING;

    public BaseResources(Properties properties) {
        super(properties);
    }
}