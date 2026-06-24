package org.ae2craftcore.registry;

import appeng.api.upgrades.Upgrades;
import appeng.api.storage.StorageCells;
import appeng.core.definitions.AEItems;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.items.LuckCardItem;
import org.ae2craftcore.items.RecipeStorageCellItem;

public class Ae2Setup {
    public static void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register custom recipe cell handler
            StorageCells.addCellHandler(new RecipeStorageCellItem.RecipeCellHandler());

            //LogicAssemblerBlock
            Upgrades.add(AEItems.SPEED_CARD, LogicAssemblerBlock.HOLDER.get(), 4);
            Upgrades.add(LuckCardItem.LUCK_CARD_1.get(), LogicAssemblerBlock.HOLDER.get(), 1);
            Upgrades.add(LuckCardItem.LUCK_CARD_2.get(), LogicAssemblerBlock.HOLDER.get(), 1);
            Upgrades.add(LuckCardItem.LUCK_CARD_3.get(), LogicAssemblerBlock.HOLDER.get(), 1);
            Upgrades.add(LuckCardItem.LUCK_CARD_4.get(), LogicAssemblerBlock.HOLDER.get(), 1);
        });
    }
}