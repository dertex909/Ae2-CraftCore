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
            }, buf -> new MeMachineInterfaceSyncPacket(buf.readBlockPos(), buf.readUtf())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @PacketHandler
    public static void handle(MeMachineInterfaceSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            var be = level.getBlockEntity(packet.pos());
            if (be instanceof MeMachineInterfaceBlockEntity inter) inter.setCustomName(packet.customName());
        });
    }
}