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

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.AESmithingTablePattern;
import appeng.crafting.pattern.AEStonecuttingPattern;
import appeng.parts.encoding.EncodingMode;
import appeng.util.ConfigInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.registry.annotations.NetworkPayload;
import org.ae2craftcore.registry.annotations.PacketHandler;
import org.ae2craftcore.registry.annotations.PayloadDirection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@NetworkPayload(direction = PayloadDirection.TO_SERVER)
public record RecipeTerminalLoadRecipePacket(ItemStack patternToLoad) implements CustomPacketPayload {

    public static final Type<RecipeTerminalLoadRecipePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_load_recipe"));

    @SuppressWarnings("unused")
    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalLoadRecipePacket> STREAM_CODEC = StreamCodec.of((buf, value) -> {
        var registryBuf = (RegistryFriendlyByteBuf) buf;
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuf, value.patternToLoad());
    }, buf -> {
        var registryBuf = (RegistryFriendlyByteBuf) buf;
        return new RecipeTerminalLoadRecipePacket(ItemStack.OPTIONAL_STREAM_CODEC.decode(registryBuf));
    });

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @PacketHandler
    public static void handle(RecipeTerminalLoadRecipePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu instanceof RecipeTerminalMenu menu) {
                var part = menu.getPart();
                if (part == null) return;
                var logic = part.getLogic();
                if (logic == null) return;

                var details = PatternDetailsHelper.decodePattern(packet.patternToLoad(), player.level());
                switch (details) {
                    case null -> {
                        return;
                    }
                    case AECraftingPattern craftingPattern -> {
                        logic.setMode(EncodingMode.CRAFTING);
                        logic.setSubstitution(craftingPattern.canSubstitute());
                        logic.setFluidSubstitution(craftingPattern.canSubstituteFluids());
                        fillInventoryFromSparseStacks(logic.getEncodedInputInv(), craftingPattern.getSparseInputs());
                        fillInventoryFromSparseStacks(logic.getEncodedOutputInv(), craftingPattern.getSparseOutputs());
                    }
                    case AEProcessingPattern processingPattern -> {
                        logic.setMode(EncodingMode.PROCESSING);
                        fillInventoryFromSparseStacks(logic.getEncodedInputInv(), processingPattern.getSparseInputs());
                        fillInventoryFromSparseStacks(logic.getEncodedOutputInv(), processingPattern.getSparseOutputs());
                    }
                    case AESmithingTablePattern smithingTablePattern -> {
                        logic.setMode(EncodingMode.SMITHING_TABLE);
                        logic.setSubstitution(smithingTablePattern.canSubstitute());
                        logic.getEncodedInputInv().clear();
                        logic.getEncodedInputInv().setStack(0, new GenericStack(smithingTablePattern.getTemplate(), 1));
                        logic.getEncodedInputInv().setStack(1, new GenericStack(smithingTablePattern.getBase(), 1));
                        logic.getEncodedInputInv().setStack(2, new GenericStack(smithingTablePattern.getAddition(), 1));
                        logic.getEncodedOutputInv().clear();
                    }
                    case AEStonecuttingPattern stonecuttingPattern -> {
                        logic.setMode(EncodingMode.STONECUTTING);
                        logic.setStonecuttingRecipeId(stonecuttingPattern.getRecipeId());
                        logic.setSubstitution(stonecuttingPattern.canSubstitute());
                        logic.getEncodedInputInv().clear();
                        logic.getEncodedInputInv().setStack(0, new GenericStack(stonecuttingPattern.getInput(), 1));
                        logic.getEncodedOutputInv().clear();
                    }
                    default -> {
                    }
                }

                menu.setProcessingScrollOffset(0);
                menu.broadcastChanges();
            }
        });
    }

    private static void fillInventoryFromSparseStacks(ConfigInventory inv, List<GenericStack> stacks) {
        inv.beginBatch();
        try {
            for (int i = 0; i < inv.size(); i++) inv.setStack(i, i < stacks.size() ? stacks.get(i) : null);
        } finally {
            inv.endBatch();
        }
    }
}