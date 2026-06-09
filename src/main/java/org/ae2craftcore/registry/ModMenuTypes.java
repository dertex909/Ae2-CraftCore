package org.ae2craftcore.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ae2craftcore.Ae2craftcore;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, Ae2craftcore.MODID);


    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
        Ae2craftcore.LOGGER.info("Menu types registered");
    }
}