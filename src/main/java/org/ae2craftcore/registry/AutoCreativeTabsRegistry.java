package org.ae2craftcore.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ae2craftcore.Ae2craftcore;

public class AutoCreativeTabsRegistry {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, Ae2craftcore.MODID);

    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CELESTIAL_MECHANICS_TAB =
            CREATIVE_MODE_TABS.register("celestial_mechanics_tab", () -> CreativeModeTab.builder()
                    //.icon(() -> new ItemStack(SchemeItem.HOLDER.get()))
                    .title(Component.translatable("itemGroup.celestialmechanics"))
                    .displayItems((parameters, output) ->
                            AutoItemRegistry.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
        Ae2craftcore.LOGGER.info("Creative tabs registered");
    }
}