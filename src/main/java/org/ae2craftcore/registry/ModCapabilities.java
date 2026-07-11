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

import appeng.api.AECapabilities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.blockentity.LogicAssemblerBlockEntity;
import org.ae2craftcore.blocks.blockentity.MeMachineInterfaceBlockEntity;

@EventBusSubscriber(modid = Ae2craftcore.MODID)
public class ModCapabilities {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, LogicAssemblerBlockEntity.TYPE, (be, context) -> be);
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, MeMachineInterfaceBlockEntity.TYPE, (be, context) -> be);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, LogicAssemblerBlockEntity.TYPE, SidedInvWrapper::new);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MeMachineInterfaceBlockEntity.TYPE, (be, context) -> be.getInternalInventory().toItemHandler());
    }
}