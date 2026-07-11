/*
 * Ae2 CraftCore
 * Copyright (C) 2026 dertex909
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.registry.annotations.RegisterItem;
import org.ae2craftcore.registry.utils.RegistryScanHelper;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;

public class AutoItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Ae2craftcore.MODID);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        scanAndRegister();
        Ae2craftcore.LOGGER.info("AutoItemRegistry: Item registration initialized");
    }

    public static void scanAndRegister() {
        Ae2craftcore.LOGGER.info("AutoItemRegistry: Scanning for annotated items...");
        for (var className : RegistryScanHelper.findAnnotatedClasses(RegisterItem.class, RegisterItem.List.class))
            registerClass(className);
    }

    private static void registerClass(String className) {
        try {
            var clazz = RegistryScanHelper.loadClass(className);
            if (clazz == null || !Item.class.isAssignableFrom(clazz)) return;
            var annotations = clazz.getAnnotationsByType(RegisterItem.class);
            for (var anno : annotations) registerSingleItem(clazz, anno);
        } catch (Exception e) {
            Ae2craftcore.LOGGER.error("Failed to auto-register item class: {}", className, e);
        }
    }

    @FunctionalInterface
    private interface ItemFactory {
        Item create() throws Throwable;
    }

    private static void registerSingleItem(Class<?> clazz, RegisterItem anno) {
        String name = anno.name();

        try {
            ItemFactory factory;
            try {
                factory = resolveFactory(clazz, anno, name);
            } catch (Throwable e) {
                Ae2craftcore.LOGGER.error("Failed to resolve factory for item '{}': {}", name, e.getMessage());
                return;
            }

            var holder = ITEMS.register(name, () -> {
                try {
                    return factory.create();
                } catch (Throwable e) {
                    throw new RuntimeException("Failed to instantiate item: " + clazz.getName(), e);
                }
            });

            injectHolder(clazz, holder, anno);
            Ae2craftcore.LOGGER.info("Auto-registered item: '{}'", name);

        } catch (Exception e) {
            Ae2craftcore.LOGGER.error("Failed to register item '{}': {}", name, e.getMessage());
        }
    }

    private static ItemFactory resolveFactory(Class<?> clazz, RegisterItem anno, String name) throws Throwable {
        var lookup = MethodHandles.lookup();

        try {
            var m = clazz.getDeclaredMethod("create", RegisterItem.class);
            if (Modifier.isStatic(m.getModifiers())) {
                m.setAccessible(true);
                var handle = lookup.unreflect(m);
                return () -> (Item) handle.invoke(anno);
            }
        } catch (NoSuchMethodException ignored) {
        }

        try {
            var handle = lookup.unreflectConstructor(clazz.getConstructor(Item.Properties.class));
            return () -> {
                var props = buildProperties(anno, name);
                return (Item) handle.invoke(props);
            };
        } catch (NoSuchMethodException ignored) {
        }

        if (!anno.tier().isEmpty()) {
            var handle = lookup.unreflectConstructor(clazz.getConstructor(String.class));
            return () -> (Item) handle.invoke(anno.tier());
        }

        throw new NoSuchMethodException(clazz.getName() + ": no compatible constructor found");
    }

    private static Item.Properties buildProperties(RegisterItem anno, String name) {
        var props = new Item.Properties();
        props.stacksTo(anno.stacksTo());
        if (anno.durability() > 0) props.durability(anno.durability());
        if (anno.fireResistant()) props.fireResistant();
        if (!anno.rarity().equals("COMMON")) try {
            props.rarity(Rarity.valueOf(anno.rarity()));
        } catch (IllegalArgumentException e) {
            Ae2craftcore.LOGGER.warn("Unknown rarity '{}' for item '{}', using COMMON", anno.rarity(), name);
        }
        if (!anno.craftRemainder().isEmpty()) BuiltInRegistries.ITEM.getOptional(ResourceLocation.tryParse(
                anno.craftRemainder())).ifPresent(props::craftRemainder);
        return props;
    }

    private static void injectHolder(Class<?> clazz, DeferredHolder<Item, ?> holder, RegisterItem anno) {
        String name = anno.name().toUpperCase();

        for (var field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;

            String fieldName = field.getName();
            boolean match = fieldName.equals("HOLDER") || fieldName.equals(name);

            if (match) try {
                field.setAccessible(true);
                MethodHandles.lookup().unreflectSetter(field).invoke(holder);
                return;
            } catch (Throwable e) {
                Ae2craftcore.LOGGER.error("Failed to inject holder into field {} of {}", fieldName, clazz.getName());
            }
        }
    }
}