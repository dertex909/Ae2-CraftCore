package org.ae2craftcore.registry;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;

public class Ae2Setup {
    public static void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> Upgrades.add(AEItems.SPEED_CARD, LogicAssemblerBlock.HOLDER.get(), 4));
    }
}