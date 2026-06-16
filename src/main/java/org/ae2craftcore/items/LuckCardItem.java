package org.ae2craftcore.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.annotations.RegisterItem;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import appeng.items.materials.UpgradeCardItem;

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

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}