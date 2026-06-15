package org.ae2craftcore.registry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import org.ae2craftcore.registry.utils.RegistryScanHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;

public class AutoBlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Ae2craftcore.MODID);
    private static final Reference2ObjectOpenHashMap<Class<?>, ObjectArrayList<DeferredHolder<Block, Block>>> REGISTERED_BLOCKS = new Reference2ObjectOpenHashMap<>();

    public static ObjectArrayList<DeferredHolder<Block, Block>> getHoldersFor(Class<?> clazz) {
        return REGISTERED_BLOCKS.getOrDefault(clazz, new ObjectArrayList<>());
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        scanAndRegister();
        Ae2craftcore.LOGGER.info("AutoBlockRegistry: Block registration initialized");
    }

    public static void scanAndRegister() {
        Ae2craftcore.LOGGER.info("AutoBlockRegistry: Scanning for annotated blocks...");
        for (var className : RegistryScanHelper.findAnnotatedClasses(RegisterBlock.class, RegisterBlock.List.class))
            registerClass(className);
    }

    private static void registerClass(String className) {
        try {
            var clazz = RegistryScanHelper.loadClass(className);
            if (clazz == null || !Block.class.isAssignableFrom(clazz)) return;
            var annotations = clazz.getAnnotationsByType(RegisterBlock.class);
            for (var anno : annotations) registerSingleBlock(clazz, anno);
        } catch (Exception e) {
            Ae2craftcore.LOGGER.error("Failed to auto-register block class: {}", className, e);
        }
    }

    private static void registerSingleBlock(Class<?> clazz, RegisterBlock anno) {
        String name = anno.name();

        try {
            var props = BlockBehaviour.Properties.of();
            props.strength(anno.strength(), anno.resistance() < 0 ? anno.strength() : anno.resistance());
            props.sound(getSoundType(anno.sound()));
            if (anno.requiresCorrectTool()) props.requiresCorrectToolForDrops();
            if (anno.noOcclusion()) props.noOcclusion();
            if (anno.dynamicShape()) props.dynamicShape();
            if (anno.lightLevel() > 0) props.lightLevel(state -> anno.lightLevel());
            props.friction(anno.friction());

            var lookup = MethodHandles.lookup();
            var holder = BLOCKS.register(name, () -> {
                try {
                    MethodHandle handle;
                    if (anno.tier() >= 0) {
                        var ctor = clazz.getConstructor(BlockBehaviour.Properties.class, int.class);
                        handle = lookup.unreflectConstructor(ctor);
                        return (Block) handle.invoke(props, anno.tier());
                    } else {
                        var ctor = clazz.getConstructor(BlockBehaviour.Properties.class);
                        handle = lookup.unreflectConstructor(ctor);
                        return (Block) handle.invoke(props);
                    }
                } catch (NoSuchMethodException e) {
                    Ae2craftcore.LOGGER.error("Block class {} must have a constructor (Properties) or (Properties, int)!", clazz.getName());
                    throw new RuntimeException(e);
                } catch (Throwable e) {
                    throw new RuntimeException("Failed to instantiate block: " + clazz.getName(), e);
                }
            });

            if (anno.hasItem()) {
                AutoItemRegistry.ITEMS.register(name, () -> new BlockItem(holder.get(), new Item.Properties()));
            }

            REGISTERED_BLOCKS.computeIfAbsent(clazz, k -> new ObjectArrayList<>()).add(holder);
            injectHolder(clazz, holder, anno);

            Ae2craftcore.LOGGER.info("Auto-registered block: '{}'", name);

        } catch (Exception e) {
            Ae2craftcore.LOGGER.error("Failed to register block '{}': {}", name, e.getMessage());
        }
    }

    private static void injectHolder(Class<?> clazz, DeferredHolder<Block, ?> holder, RegisterBlock anno) {
        String name = anno.name().toUpperCase();
        String tierName = anno.tier() >= 0 ? "TIER_" + anno.tier() : "";

        for (var field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;

            String fieldName = field.getName();
            boolean match = fieldName.equals("HOLDER") || fieldName.equals(name) ||
                    (!tierName.isEmpty() && fieldName.equals(tierName));

            if (match) try {
                field.setAccessible(true);
                var handle = MethodHandles.lookup().unreflectSetter(field);
                handle.invoke(holder);
                return;
            } catch (Throwable e) {
                Ae2craftcore.LOGGER.error("Failed to inject holder into field {} of class {}", fieldName, clazz.getName());
            }
        }
    }

    private static SoundType getSoundType(String sound) {
        return switch (sound.toLowerCase()) {
            case "stone" -> SoundType.STONE;
            case "metal" -> SoundType.METAL;
            case "wood" -> SoundType.WOOD;
            case "glass" -> SoundType.GLASS;
            case "sand" -> SoundType.SAND;
            case "gravel" -> SoundType.GRAVEL;
            case "grass" -> SoundType.GRASS;
            case "chain" -> SoundType.CHAIN;
            default -> SoundType.EMPTY;
        };
    }
}