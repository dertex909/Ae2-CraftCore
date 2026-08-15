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

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.blockentity.AEBaseBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.blockentity.MeMachineInterfaceBlockEntity;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.compat.extendedAE.ExtendedAeCompat;
import org.ae2craftcore.items.RecipeStorageCellItem;
import org.ae2craftcore.registry.annotations.NetworkPayload;
import org.ae2craftcore.registry.annotations.PacketHandler;
import org.ae2craftcore.registry.annotations.PayloadDirection;
import org.ae2craftcore.services.IRecipeCacheService;
import org.jetbrains.annotations.NotNull;

@NetworkPayload(direction = PayloadDirection.TO_SERVER)
public record RecipeTerminalDeleteRecipePacket(ItemStack patternToDelete) implements CustomPacketPayload {

    public static final Type<RecipeTerminalDeleteRecipePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_delete_recipe"));

    @SuppressWarnings("unused")
    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalDeleteRecipePacket> STREAM_CODEC = StreamCodec.of((buf, value) -> {
        var registryBuf = (RegistryFriendlyByteBuf) buf;
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuf, value.patternToDelete());
    }, buf -> {
        var registryBuf = (RegistryFriendlyByteBuf) buf;
        return new RecipeTerminalDeleteRecipePacket(ItemStack.OPTIONAL_STREAM_CODEC.decode(registryBuf));
    });

    @PacketHandler
    public static void handle(RecipeTerminalDeleteRecipePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();

            if (!(player.containerMenu instanceof RecipeTerminalMenu menu)) return;

            var part = menu.getPart();
            if (part == null) return;

            var node = part.getGridNode();
            if (node == null) return;

            var grid = node.getGrid();
            if (grid == null) return;

            var cacheService = grid.getService(IRecipeCacheService.class);
            if (cacheService != null) cacheService.invalidate();

            deletePattern:
            for (var drive : RecipeStorageCellItem.getDrives(grid)) {
                for (int i = 0; i < drive.getCellCount(); i++) {
                    var recipeCell = RecipeStorageCellItem.getRecipeCell(drive, i);
                    if (recipeCell != null && recipeCell.deletePattern(packet.patternToDelete())) {
                        if (drive instanceof AEBaseBlockEntity be) be.markForUpdate();
                        break deletePattern;
                    }
                }
            }

            for (var machine : grid.getMachines(MeMachineInterfaceBlockEntity.class)) {
                ICraftingProvider.requestUpdate(machine.getMainNode());
            }
            ExtendedAeCompat.requestUpdateForMatrixAssemblers(grid);

            menu.syncRecipesToClient();
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}