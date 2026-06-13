package org.ae2craftcore.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.blockentity.SfpModuleBlockEntity;
import org.ae2craftcore.registry.annotations.NetworkPayload;
import org.ae2craftcore.registry.annotations.PacketHandler;
import org.ae2craftcore.registry.annotations.PayloadDirection;
import org.jetbrains.annotations.NotNull;

@NetworkPayload(direction = PayloadDirection.TO_SERVER)
public record SfpChannelPacket(BlockPos pos, int channels) implements CustomPacketPayload {
    public static final Type<SfpChannelPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "sfp_channel"));

    public static final StreamCodec<FriendlyByteBuf, SfpChannelPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull SfpChannelPacket decode(FriendlyByteBuf buf) {
            return new SfpChannelPacket(buf.readBlockPos(), buf.readInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, SfpChannelPacket value) {
            buf.writeBlockPos(value.pos());
            buf.writeInt(value.channels());
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @PacketHandler
    public static void handle(SfpChannelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            var level = player.level();
            var pos = packet.pos();
            var be = level.getBlockEntity(pos);
            if (be instanceof SfpModuleBlockEntity sfp) sfp.setChannels(packet.channels());
        });
    }
}