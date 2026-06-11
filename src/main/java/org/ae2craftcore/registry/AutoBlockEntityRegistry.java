package org.ae2craftcore.registry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.ae2craftcore.registry.utils.RegistryScanHelper;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

public class AutoBlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Ae2craftcore.MODID);

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
        scanAndRegister();
        Ae2craftcore.LOGGER.info("AutoBlockEntityRegistry: Block Entity registration initialized");
    }

    private static void scanAndRegister() {
        Ae2craftcore.LOGGER.info("AutoBlockEntityRegistry: Scanning for annotated block entities...");
        for (String className : RegistryScanHelper.findAnnotatedClasses(RegisterBlockEntity.class))
            registerClass(className);
    }

    private static void registerClass(String className) {
        try {
            var clazz = RegistryScanHelper.loadClass(className);
            if (clazz == null || !BlockEntity.class.isAssignableFrom(clazz)) return;
            var anno = clazz.getAnnotation(RegisterBlockEntity.class);
            if (anno == null) return;

            String name = anno.name();
            var blockClasses = anno.blocks();

            ObjectArrayList<Supplier<Block>> blockSuppliers = new ObjectArrayList<>();
            for (var blockClass : blockClasses) {
                var holders = AutoBlockRegistry.getHoldersFor(blockClass);
                blockSuppliers.addAll(holders);
            }

            if (blockSuppliers.isEmpty()) {
                Ae2craftcore.LOGGER.error("Block Entity '{}' has no associated blocks registered!", name);
                return;
            }

            var ctor = clazz.getConstructor(BlockPos.class, BlockState.class);
            var lookup = MethodHandles.lookup();
            var handle = lookup.unreflectConstructor(ctor);

            var site = LambdaMetafactory.metafactory(
                    lookup,
                    "create",
                    MethodType.methodType(BlockEntityType.BlockEntitySupplier.class),
                    MethodType.methodType(BlockEntity.class, BlockPos.class, BlockState.class),
                    handle,
                    handle.type()
            );

            var supplier = (BlockEntityType.BlockEntitySupplier<?>) site.getTarget().invokeExact();

            BLOCK_ENTITIES.register(name, () -> {
                Block[] blocks = blockSuppliers.stream().map(Supplier::get).toArray(Block[]::new);
                @SuppressWarnings("DataFlowIssue")
                var type = BlockEntityType.Builder.of(supplier, blocks).build(null);
                injectType(clazz, type);

                return type;
            });

            Ae2craftcore.LOGGER.info("Auto-registered block entity: '{}'", name);
        } catch (Throwable e) {
            Ae2craftcore.LOGGER.error("Failed to auto-register block entity class: {}", className, e);
        }
    }

    private static void injectType(Class<?> clazz, BlockEntityType<?> type) {
        for (var field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (!field.getName().equals("TYPE")) continue;
            try {
                field.setAccessible(true);
                field.set(null, type);
                return;
            } catch (Throwable e) {
                Ae2craftcore.LOGGER.error("Failed to inject TYPE into block entity class: {}", clazz.getName(), e);
            }
        }
    }
}