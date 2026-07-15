/*
 * Ae2 CraftCore
 * Copyright (C) 2026 dertex909
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.ae2craftcore.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
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
public record RecipeTerminalSyncPacket(List<ItemStack> patterns, List<MachineGroupInfo> groups)
        implements CustomPacketPayload {

    public static final Type<RecipeTerminalSyncPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_sync"));
    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalSyncPacket> STREAM_CODEC = StreamCodec.of((buf, value) -> {
        var registryBuf = (RegistryFriendlyByteBuf) buf;
        registryBuf.writeInt(value.patterns().size());
        for (var stack : value.patterns()) ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuf, stack);
        registryBuf.writeInt(value.groups().size());
        for (var g : value.groups()) {
            registryBuf.writeUtf(g.name());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuf, g.icon());
            registryBuf.writeInt(g.count());
        }
    }, buf -> {
        var registryBuf = (RegistryFriendlyByteBuf) buf;
        int size = registryBuf.readInt();
        var list = new ArrayList<ItemStack>();
        for (int i = 0; i < size; i++) list.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(registryBuf));
        int groupSize = registryBuf.readInt();
        var groupsList = new ArrayList<MachineGroupInfo>();
        for (int i = 0; i < groupSize; i++) {
            String name = registryBuf.readUtf();
            var icon = ItemStack.OPTIONAL_STREAM_CODEC.decode(registryBuf);
            int count = registryBuf.readInt();
            groupsList.add(new MachineGroupInfo(name, icon, count));
        }
        return new RecipeTerminalSyncPacket(list, groupsList);
    });

    @PacketHandler
    public static void handle(RecipeTerminalSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu instanceof RecipeTerminalMenu menu) if (context.flow().isClientbound()) {
                menu.setClientRecipes(packet.patterns());
                menu.setClientGroups(packet.groups());
                if (menu.getSelectedGroup().isEmpty() && !packet.groups().isEmpty()) {
                    String firstGroup = packet.groups().getFirst().name();
                    menu.setSelectedGroup(firstGroup);
                    ClientPacketDistributor.sendToServer(new RecipeTerminalSelectGroupPacket(firstGroup));
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record MachineGroupInfo(String name, ItemStack icon, int count) {
    }
}