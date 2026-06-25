package org.ae2craftcore.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.*;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, Ae2craftcore.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<LogicAssemblerMenu>> LOGIC_ASSEMBLER = MENU_TYPES.register(
            "logic_assembler", () -> IMenuTypeExtension.create((containerId, inv, buf) -> new LogicAssemblerMenu(containerId, inv)));

    public static final DeferredHolder<MenuType<?>, MenuType<MultiblockMonitorMenu>> MULTIBLOCK_MONITOR = MENU_TYPES.register(
            "multiblock_monitor", () -> IMenuTypeExtension.create((containerId, inv, buf) -> new MultiblockMonitorMenu(containerId)));

    public static final DeferredHolder<MenuType<?>, MenuType<PicInjectorMenu>> PIC_INJECTOR = MENU_TYPES.register(
            "pic_injector", () -> IMenuTypeExtension.create((containerId, inv, buf) -> new PicInjectorMenu(containerId, inv)));

    public static final DeferredHolder<MenuType<?>, MenuType<CryostatMenu>> CRYOSTAT = MENU_TYPES.register(
            "cryostat", () -> IMenuTypeExtension.create((containerId, inv, buf) -> new CryostatMenu(containerId, inv)));

    public static final DeferredHolder<MenuType<?>, MenuType<MeMachineInterfaceMenu>> ME_MACHINE_INTERFACE = MENU_TYPES.register(
            "me_machine_interface", () -> IMenuTypeExtension.create(MeMachineInterfaceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RecipeTerminalMenu>> RECIPE_TERMINAL = MENU_TYPES.register(
            "recipe_terminal", () -> IMenuTypeExtension.create(RecipeTerminalMenu::new));

    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
        Ae2craftcore.LOGGER.info("Menu types registered");
    }
}