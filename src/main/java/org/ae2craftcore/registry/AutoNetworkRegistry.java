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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.registry.annotations.NetworkPayload;
import org.ae2craftcore.registry.annotations.PacketHandler;
import org.ae2craftcore.registry.annotations.PayloadDirection;
import org.ae2craftcore.registry.utils.RegistryScanHelper;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class AutoNetworkRegistry {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(Ae2craftcore.MODID).versioned("1");

        Ae2craftcore.LOGGER.info("AutoNetworkRegistry: Scanning for network payloads...");

        var payloads = new Reference2ObjectOpenHashMap<Class<? extends CustomPacketPayload>, NetworkPayload>();
        var handlers = new ObjectArrayList<Method>();

        for (var className : RegistryScanHelper.findAnnotatedClasses(NetworkPayload.class)) {
            var clazz = RegistryScanHelper.loadClass(className);
            if (clazz != null && CustomPacketPayload.class.isAssignableFrom(clazz))
                payloads.put(clazz.asSubclass(CustomPacketPayload.class), clazz.getAnnotation(NetworkPayload.class));
        }

        for (var className : RegistryScanHelper.findAnnotatedClasses(PacketHandler.class)) {
            var clazz = RegistryScanHelper.loadClass(className);
            if (clazz != null) for (var m : clazz.getDeclaredMethods())
                if (m.isAnnotationPresent(PacketHandler.class)) handlers.add(m);
        }

        payloads.forEach((packetClass, anno) -> {
            try {
                var handlerMethod = handlers.stream().filter(m -> m.getParameterCount() == 2
                        && m.getParameterTypes()[0].isAssignableFrom(packetClass)
                        && IPayloadContext.class.isAssignableFrom(m.getParameterTypes()[1])).findFirst().orElse(null);

                if (handlerMethod == null) {
                    Ae2craftcore.LOGGER.error("AutoNetworkRegistry: No handler for {}", packetClass.getSimpleName());
                    return;
                }

                handlerMethod.setAccessible(true);
                var lookup = MethodHandles.lookup();
                var handle = lookup.unreflect(handlerMethod);

                var site = LambdaMetafactory.metafactory(
                        lookup,
                        "handle",
                        MethodType.methodType(IPayloadHandler.class),
                        MethodType.methodType(void.class, CustomPacketPayload.class, IPayloadContext.class),
                        handle,
                        handle.type()
                );

                @SuppressWarnings("unchecked")
                var handler = (IPayloadHandler<CustomPacketPayload>) site.getTarget().invokeExact();

                @SuppressWarnings("unchecked")
                var rawType = (CustomPacketPayload.Type<CustomPacketPayload>) packetClass.getField("TYPE").get(null);
                @SuppressWarnings("unchecked")
                var rawCodec = (StreamCodec<? super FriendlyByteBuf, CustomPacketPayload>) packetClass.getField("STREAM_CODEC").get(null);

                if (anno.direction() == PayloadDirection.TO_CLIENT) {
                    registrar.playToClient(rawType, rawCodec, handler);
                } else {
                    registrar.playToServer(rawType, rawCodec, handler);
                }

                Ae2craftcore.LOGGER.info("AutoNetworkRegistry: Registered {} -> {}::{}",
                        packetClass.getSimpleName(), handlerMethod.getDeclaringClass().getSimpleName(), handlerMethod.getName());
            } catch (Throwable e) {
                Ae2craftcore.LOGGER.error("AutoNetworkRegistry: Failed to register {}: {}", packetClass.getSimpleName(), e.getMessage());
            }
        });
    }
}