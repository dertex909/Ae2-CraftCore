package org.ae2craftcore.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.registry.AttachmentRegistry;
import org.ae2craftcore.registry.annotations.NetworkPayload;
import org.ae2craftcore.registry.annotations.PacketHandler;
import org.ae2craftcore.registry.annotations.PayloadDirection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@NetworkPayload(direction = PayloadDirection.TO_SERVER)
public record RecipeTerminalDeleteRecipePacket(int index) implements CustomPacketPayload {

    public static final Type<RecipeTerminalDeleteRecipePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_delete_recipe"));

    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalDeleteRecipePacket> STREAM_CODEC = StreamCodec.of((buf, value) -> buf.writeInt(value.index()), buf -> new RecipeTerminalDeleteRecipePacket(buf.readInt())
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
                var cellStack = menu.getSlot(12).getItem();
                if (cellStack.isEmpty()) return;

                var currentList = cellStack.get(AttachmentRegistry.STORED_PATTERNS.get());
                if (currentList == null || packet.index() < 0 || packet.index() >= currentList.size()) return;

                var newList = new ArrayList<>(currentList);
                newList.remove(packet.index());

                cellStack.set(AttachmentRegistry.STORED_PATTERNS.get(), List.copyOf(newList));
                cellStack.set(AttachmentRegistry.RECIPE_COUNT.get(), newList.size());

                var uniqueTypes = new java.util.HashSet<net.minecraft.world.item.Item>();
                for (var p : newList) uniqueTypes.add(p.getItem());
                cellStack.set(AttachmentRegistry.MACHINE_COUNT.get(), uniqueTypes.size());

                menu.getSlot(12).setChanged();
            }
        });
    }
}