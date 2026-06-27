package org.ae2craftcore.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ClickType;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.registry.annotations.NetworkPayload;
import org.ae2craftcore.registry.annotations.PacketHandler;
import org.ae2craftcore.registry.annotations.PayloadDirection;
import org.jetbrains.annotations.NotNull;

@NetworkPayload(direction = PayloadDirection.TO_SERVER)
public record RecipeTerminalPhantomClickPacket(int slotId, int button, ClickType clickType)
        implements CustomPacketPayload {

    public static final Type<RecipeTerminalPhantomClickPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_phantom_click"));

    @SuppressWarnings("unused")
    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalPhantomClickPacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeInt(value.slotId());
                buf.writeInt(value.button());
                buf.writeEnum(value.clickType());
            },
            buf -> new RecipeTerminalPhantomClickPacket(buf.readInt(), buf.readInt(), buf.readEnum(ClickType.class))
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @PacketHandler
    public static void handle(RecipeTerminalPhantomClickPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu instanceof RecipeTerminalMenu menu) {
                menu.handlePhantomClick(packet.slotId(), packet.button(), packet.clickType());
            }
        });
    }
}