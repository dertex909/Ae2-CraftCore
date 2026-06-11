package org.ae2craftcore.items;

import appeng.items.materials.UpgradeCardItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.annotations.RegisterItem;

@RegisterItem(name = "luck_card_1", stacksTo = 16)
@RegisterItem(name = "luck_card_2", stacksTo = 16)
@RegisterItem(name = "luck_card_3", stacksTo = 16)
@RegisterItem(name = "luck_card_4", stacksTo = 16)
public class LuckUpgradeCard extends UpgradeCardItem {

    public static DeferredHolder<Item, LuckUpgradeCard> LUCK_CARD_1;
    public static DeferredHolder<Item, LuckUpgradeCard> LUCK_CARD_2;
    public static DeferredHolder<Item, LuckUpgradeCard> LUCK_CARD_3;
    public static DeferredHolder<Item, LuckUpgradeCard> LUCK_CARD_4;

    public LuckUpgradeCard(Properties properties) {
        super(properties);
    }
}