package org.ae2craftcore.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.LogicAssemblerMenu;
import org.ae2craftcore.blocks.menu.SfpModuleMenu;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, Ae2craftcore.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<LogicAssemblerMenu>> LOGIC_ASSEMBLER = MENU_TYPES.register(
            "logic_assembler", () -> IMenuTypeExtension.create((containerId, inv, buf) -> new LogicAssemblerMenu(containerId, inv)));

    public static final DeferredHolder<MenuType<?>, MenuType<SfpModuleMenu>> SFP_MODULE = MENU_TYPES.register(
            "sfp_module", () -> IMenuTypeExtension.create((containerId, inv, buf) -> new SfpModuleMenu(containerId)));

    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
        Ae2craftcore.LOGGER.info("Menu types registered");
    }
}