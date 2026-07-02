package org.ae2craftcore.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.registry.annotations.NetworkPayload;
import org.ae2craftcore.registry.annotations.PacketHandler;
import org.ae2craftcore.registry.annotations.PayloadDirection;
import org.jetbrains.annotations.NotNull;

@NetworkPayload(direction = PayloadDirection.TO_SERVER)
public record RecipeTerminalCycleOutputsPacket() implements CustomPacketPayload {
    public static final Type<RecipeTerminalCycleOutputsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_cycle_outputs"));

    @SuppressWarnings("unused")
    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalCycleOutputsPacket> STREAM_CODEC = StreamCodec.unit(new RecipeTerminalCycleOutputsPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @PacketHandler
    public static void handle(RecipeTerminalCycleOutputsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu instanceof RecipeTerminalMenu menu) menu.cycleProcessingOutputs();
        });
    }
}
