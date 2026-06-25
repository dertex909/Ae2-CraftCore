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

@NetworkPayload(direction = PayloadDirection.TO_SERVER)
public record RecipeTerminalDeleteRecipePacket(ItemStack patternToDelete) implements CustomPacketPayload {

    public static final Type<RecipeTerminalDeleteRecipePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_delete_recipe"));

    @SuppressWarnings("unused")
    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalDeleteRecipePacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                var registryBuf = (RegistryFriendlyByteBuf) buf;
                ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuf, value.patternToDelete());
            },
            buf -> {
                var registryBuf = (RegistryFriendlyByteBuf) buf;
                return new RecipeTerminalDeleteRecipePacket(ItemStack.OPTIONAL_STREAM_CODEC.decode(registryBuf));
            }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @PacketHandler
    public static void handle(RecipeTerminalDeleteRecipePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu instanceof RecipeTerminalMenu menu) {
                var part = menu.getPart();
                if (part == null) return;
                var node = part.getGridNode();
                if (node == null) return;
                var grid = node.getGrid();
                if (grid == null) return;
                var storage = grid.getStorageService();
                if (storage == null) return;
                var inv = storage.getInventory();
                if (inv == null) return;

                var key = appeng.api.stacks.AEItemKey.of(packet.patternToDelete());
                inv.extract(key, 1, appeng.api.config.Actionable.MODULATE, new appeng.me.helpers.PlayerSource(player));

                menu.syncRecipesToClient();
            }
        });
    }
}