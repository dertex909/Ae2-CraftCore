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
public record RecipeTerminalUpdateSettingsPacket(boolean substitute, boolean substituteFluids)
        implements CustomPacketPayload {

    public static final Type<RecipeTerminalUpdateSettingsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_update_settings"));

    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalUpdateSettingsPacket> STREAM_CODEC = StreamCodec.of((buf, value) -> {
        buf.writeBoolean(value.substitute());
        buf.writeBoolean(value.substituteFluids());
    }, buf -> new RecipeTerminalUpdateSettingsPacket(buf.readBoolean(), buf.readBoolean()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @PacketHandler
    public static void handle(RecipeTerminalUpdateSettingsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu instanceof RecipeTerminalMenu menu) {
                var part = menu.getPart();
                if (part != null) {
                    part.getLogic().setSubstitution(packet.substitute());
                    part.getLogic().setFluidSubstitution(packet.substituteFluids());
                }
            }
        });
    }
}