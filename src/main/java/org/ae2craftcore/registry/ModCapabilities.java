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