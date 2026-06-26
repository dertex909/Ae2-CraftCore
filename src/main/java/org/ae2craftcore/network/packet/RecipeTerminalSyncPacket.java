package org.ae2craftcore.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
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
public record RecipeTerminalSyncPacket(List<ItemStack> patterns, List<String> groups, boolean isQuantumValid)
        implements CustomPacketPayload {

    public static final Type<RecipeTerminalSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_sync"));

    @SuppressWarnings("unused")
    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalSyncPacket> STREAM_CODEC = StreamCodec.of((buf, value) -> {
        var registryBuf = (RegistryFriendlyByteBuf) buf;
        registryBuf.writeBoolean(value.isQuantumValid());
        registryBuf.writeInt(value.patterns().size());
        for (var stack : value.patterns()) ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuf, stack);
        registryBuf.writeInt(value.groups().size());
        for (var g : value.groups()) registryBuf.writeUtf(g);
    }, buf -> {
        var registryBuf = (RegistryFriendlyByteBuf) buf;
        boolean isQuantumValid = registryBuf.readBoolean();
        int size = registryBuf.readInt();
        var list = new ArrayList<ItemStack>();
        for (int i = 0; i < size; i++) list.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(registryBuf));
        int groupSize = registryBuf.readInt();
        var groupsList = new ArrayList<String>();
        for (int i = 0; i < groupSize; i++) groupsList.add(registryBuf.readUtf());
        return new RecipeTerminalSyncPacket(list, groupsList, isQuantumValid);
    });

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @PacketHandler
    public static void handle(RecipeTerminalSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu instanceof RecipeTerminalMenu menu) if (context.flow().isClientbound()) {
                menu.setClientRecipes(packet.patterns());
                menu.setClientGroups(packet.groups());
                menu.setQuantumValid();
                if (menu.getSelectedGroup().isEmpty() && !packet.groups().isEmpty()) {
                    String firstGroup = packet.groups().getFirst();
                    menu.setSelectedGroup(firstGroup);
                    PacketDistributor.sendToServer(new RecipeTerminalSelectGroupPacket(firstGroup));
                }
            }
        });
    }
}