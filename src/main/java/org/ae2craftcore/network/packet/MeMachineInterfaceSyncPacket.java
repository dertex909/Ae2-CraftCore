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

package org.ae2craftcore.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.blockentity.MeMachineInterfaceBlockEntity;
import org.ae2craftcore.registry.annotations.NetworkPayload;
import org.ae2craftcore.registry.annotations.PacketHandler;
import org.ae2craftcore.registry.annotations.PayloadDirection;
import org.jetbrains.annotations.NotNull;

@NetworkPayload(direction = PayloadDirection.TO_SERVER)
public record MeMachineInterfaceSyncPacket(BlockPos pos, String customName) implements CustomPacketPayload {

    public static final Type<MeMachineInterfaceSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "me_machine_interface_sync"));

    @SuppressWarnings("unused")
    public static final StreamCodec<FriendlyByteBuf, MeMachineInterfaceSyncPacket> STREAM_CODEC = StreamCodec.of((buf, value) -> {
        buf.writeBlockPos(value.pos());
        buf.writeUtf(value.customName());
    }, buf -> new MeMachineInterfaceSyncPacket(buf.readBlockPos(), buf.readUtf()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @PacketHandler
    public static void handle(MeMachineInterfaceSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            if (level.isLoaded(packet.pos())) {
                var be = level.getBlockEntity(packet.pos());
                if (be instanceof MeMachineInterfaceBlockEntity inter) inter.setCustomName(packet.customName());
            }
        });
    }
}