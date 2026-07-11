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

package org.ae2craftcore.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ae2craftcore.Ae2craftcore;

import java.util.List;
import java.util.function.Supplier;

public final class AttachmentRegistry {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Ae2craftcore.MODID);

    public static final Supplier<DataComponentType<Integer>> RECIPE_COUNT = DATA_COMPONENTS.registerComponentType(
            "recipe_count", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final Supplier<DataComponentType<Integer>> MACHINE_COUNT = DATA_COMPONENTS.registerComponentType(
            "machine_count", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final Supplier<DataComponentType<List<ItemStack>>> STORED_PATTERNS = DATA_COMPONENTS.registerComponentType(
            "stored_patterns", builder -> builder.persistent(ItemStack.CODEC.listOf()).networkSynchronized(ItemStack.LIST_STREAM_CODEC)
    );

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
        Ae2craftcore.LOGGER.info("AutoAttachmentRegistry: Data component types registered");
    }
}