package org.ae2craftcore.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.registry.annotations.NetworkPayload;
import org.ae2craftcore.registry.annotations.PacketHandler;
import org.ae2craftcore.registry.annotations.PayloadDirection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@NetworkPayload(direction = PayloadDirection.TO_CLIENT)
public record RecipeTerminalSyncPacket(List<ItemStack> patterns) implements CustomPacketPayload {

    public static final Type<RecipeTerminalSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_sync"));

    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                var registryBuf = (RegistryFriendlyByteBuf) buf;
                registryBuf.writeInt(value.patterns().size());
                for (var stack : value.patterns()) ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuf, stack);
            },
            buf -> {
                var registryBuf = (RegistryFriendlyByteBuf) buf;
                int size = registryBuf.readInt();
                var list = new ArrayList<ItemStack>();
                for (int i = 0; i < size; i++) list.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(registryBuf));
                return new RecipeTerminalSyncPacket(list);
            }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @PacketHandler
    public static void handle(RecipeTerminalSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu instanceof RecipeTerminalMenu menu) {
                if (context.flow().isClientbound()) menu.setClientRecipes(packet.patterns());
            }
        });
    }
}