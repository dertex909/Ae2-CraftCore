package org.ae2craftcore.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ae2craftcore.Ae2craftcore;

public final class ModSounds {

    private ModSounds() {
    }

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Ae2craftcore.MODID);

    /*
    public static final DeferredHolder<SoundEvent, SoundEvent> BUTTON_CLICK =
            SOUND_EVENTS.register("button_click",
                    () -> SoundEvent.createVariableRangeEvent(id("button_click")));
    */

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, path);
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
        Ae2craftcore.LOGGER.debug("Sounds registered");
    }
}