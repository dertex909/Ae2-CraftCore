package org.ae2craftcore.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ae2craftcore.Ae2craftcore;

import java.util.function.Supplier;

public final class AttachmentRegistry {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Ae2craftcore.MODID);

    public static final Supplier<DataComponentType<Double>> VESSEL_ENERGY = DATA_COMPONENTS.registerComponentType(
            "vessel_energy", builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE)
    );

    public static final Supplier<DataComponentType<Integer>> VESSEL_STATE = DATA_COMPONENTS.registerComponentType(
            "vessel_state", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final Supplier<DataComponentType<String>> CELL_UUID = DATA_COMPONENTS.registerComponentType(
            "cell_uuid", builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    );

    public static final Supplier<DataComponentType<Integer>> RECIPE_COUNT = DATA_COMPONENTS.registerComponentType(
            "recipe_count", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
        Ae2craftcore.LOGGER.info("AutoAttachmentRegistry: Data component types registered");
    }
}