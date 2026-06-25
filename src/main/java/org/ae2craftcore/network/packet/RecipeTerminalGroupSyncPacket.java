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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@NetworkPayload(direction = PayloadDirection.TO_CLIENT)
public record RecipeTerminalGroupSyncPacket(List<GroupInfo> groups) implements CustomPacketPayload {

    public static final Type<RecipeTerminalGroupSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_group_sync"));

    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalGroupSyncPacket> STREAM_CODEC = StreamCodec.of((buf, value) -> {
        buf.writeInt(value.groups().size());
        for (var group : value.groups()) {
            buf.writeUtf(group.name());
            buf.writeInt(group.count());
        }
    }, buf -> {
        int size = buf.readInt();
        var groups = new ArrayList<GroupInfo>();
        for (int i = 0; i < size; i++) groups.add(new GroupInfo(buf.readUtf(), buf.readInt()));
        return new RecipeTerminalGroupSyncPacket(groups);
    });

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @PacketHandler
    public static void handle(RecipeTerminalGroupSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu instanceof RecipeTerminalMenu menu) {
                if (context.flow().isClientbound()) ClientPacketHandler.handleGroupSync(menu, packet.groups());
            }
        });
    }

    public record GroupInfo(String name, int count) {
    }

    private static class ClientPacketHandler {
        public static void handleGroupSync(RecipeTerminalMenu menu, List<GroupInfo> groups) {
            menu.setSelectedGroup(menu.getSelectedGroup());
            RecipeTerminalMenuExt.setGroups(menu, groups);
        }
    }

    public static class RecipeTerminalMenuExt {
        private static final Map<RecipeTerminalMenu, List<GroupInfo>> MENU_GROUPS = new WeakHashMap<>();

        public static void setGroups(RecipeTerminalMenu menu, List<GroupInfo> groups) {
            MENU_GROUPS.put(menu, groups);
        }

        public static List<GroupInfo> getGroups(RecipeTerminalMenu menu) {
            return MENU_GROUPS.getOrDefault(menu, List.of());
        }
    }
}