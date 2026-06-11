package org.ae2craftcore.registry;

import appeng.api.AECapabilities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.blockentity.LogicAssemblerBlockEntity;

@EventBusSubscriber(modid = Ae2craftcore.MODID)
public class ModCapabilities {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, LogicAssemblerBlockEntity.TYPE, (be, context) -> be);
    }
}