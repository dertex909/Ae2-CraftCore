/*
 * Ae2 CraftCore
 * Copyright (C) 2025-2026 dertex909
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.ae2craftcore.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.blockentity.MeMachineInterfaceBlockEntity;
import org.ae2craftcore.blocks.menu.LogicAssemblerMenu;
import org.ae2craftcore.blocks.menu.MeMachineInterfaceMenu;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.parts.RecipeTerminalPart;

import static appeng.menu.implementations.MenuTypeBuilder.create;
import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, Ae2craftcore.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<LogicAssemblerMenu>> LOGIC_ASSEMBLER = MENU_TYPES.register(
            "logic_assembler", () -> IMenuTypeExtension.create((containerId, inv, buf) -> new LogicAssemblerMenu(containerId, inv)));

    public static final DeferredHolder<MenuType<?>, MenuType<MeMachineInterfaceMenu>> ME_MACHINE_INTERFACE = MENU_TYPES.register(
            "me_machine_interface", () -> create(MeMachineInterfaceMenu::new, MeMachineInterfaceBlockEntity.class)
                    .withMenuTitle(MeMachineInterfaceBlockEntity::getDisplayName)
                    .buildUnregistered(fromNamespaceAndPath(Ae2craftcore.MODID, "me_machine_interface")));

    public static final DeferredHolder<MenuType<?>, MenuType<RecipeTerminalMenu>> RECIPE_TERMINAL = MENU_TYPES.register(
            "recipe_terminal", () -> create(RecipeTerminalMenu::new, RecipeTerminalPart.class).buildUnregistered(fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal")));

    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
        Ae2craftcore.LOGGER.info("Menu types registered");
    }
}