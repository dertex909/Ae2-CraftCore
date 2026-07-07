package org.ae2craftcore.registry;

import appeng.menu.implementations.MenuTypeBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.blockentity.MeMachineInterfaceBlockEntity;
import org.ae2craftcore.blocks.menu.*;
import org.ae2craftcore.parts.RecipeTerminalPart;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, Ae2craftcore.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<LogicAssemblerMenu>> LOGIC_ASSEMBLER = MENU_TYPES.register(
            "logic_assembler", () -> IMenuTypeExtension.create((containerId, inv, buf) -> new LogicAssemblerMenu(containerId, inv)));

    public static final DeferredHolder<MenuType<?>, MenuType<MeMachineInterfaceMenu>> ME_MACHINE_INTERFACE = MENU_TYPES.register(
            "me_machine_interface", () -> MenuTypeBuilder.create(MeMachineInterfaceMenu::new, MeMachineInterfaceBlockEntity.class)
                    .buildUnregistered(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "me_machine_interface")));

    public static final DeferredHolder<MenuType<?>, MenuType<RecipeTerminalMenu>> RECIPE_TERMINAL = MENU_TYPES.register(
            "recipe_terminal", () -> MenuTypeBuilder.create(RecipeTerminalMenu::new, RecipeTerminalPart.class).buildUnregistered(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal")));

    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
        Ae2craftcore.LOGGER.info("Menu types registered");
    }
}