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
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.parts.encoding.EncodingMode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static net.minecraft.core.component.DataComponents.CUSTOM_DATA;

@NetworkPayload(direction = PayloadDirection.TO_SERVER)
public record RecipeTerminalSavePacket(
        String groupName,
        String modeName,
        String stonecuttingRecipeId,
        boolean substitutionsEnabled,
        boolean fluidSubstitutionsEnabled
) implements CustomPacketPayload {

    public static final Type<RecipeTerminalSavePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_save"));
    public static final String RECIPEMACHINEGROUP = "RecMacG";

    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalSavePacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeUtf(value.groupName());
                buf.writeUtf(value.modeName());
                buf.writeUtf(value.stonecuttingRecipeId());
                buf.writeBoolean(value.substitutionsEnabled());
                buf.writeBoolean(value.fluidSubstitutionsEnabled());
            },
            buf -> new RecipeTerminalSavePacket(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readBoolean())
    );
    private static final Component NO_SPACE_MESSAGE = Component.literal("No active Recipe Storage Cells with available space found in the network!").withStyle(ChatFormatting.RED);

    public RecipeTerminalSavePacket(String groupName, EncodingMode mode, @Nullable ResourceLocation stonecuttingRecipeId, boolean substitutionsEnabled, boolean fluidSubstitutionsEnabled) {
        this(groupName, mode.name(), stonecuttingRecipeId != null ? stonecuttingRecipeId.toString() : "", substitutionsEnabled, fluidSubstitutionsEnabled);
    }

    @PacketHandler
    public static void handle(RecipeTerminalSavePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (!(player.containerMenu instanceof RecipeTerminalMenu menu)) return;

            var part = menu.getPart();
            if (part == null) return;

            var node = part.getGridNode();
            if (node == null) return;

            var grid = node.getGrid();
            if (grid == null) return;

            try {
                var encodedPattern = encodePattern(menu, player, parseMode(packet.modeName()), packet.stonecuttingRecipeId(), packet.substitutionsEnabled(), packet.fluidSubstitutionsEnabled());
                if (encodedPattern == null || encodedPattern.isEmpty()) return;

                var customData = encodedPattern.getOrDefault(CUSTOM_DATA, CustomData.EMPTY);
                var tag = customData.copyTag();
                tag.putString(RECIPEMACHINEGROUP, packet.groupName());
                encodedPattern.set(CUSTOM_DATA, CustomData.of(tag));

                var cacheService = grid.getService(IRecipeCacheService.class);
                if (cacheService != null) cacheService.invalidate();

                boolean saved = false;

                saveSearch:
                for (var drive : RecipeStorageCellItem.getDrives(grid)) {
                    for (int i = 0; i < drive.getCellCount(); i++) {
                        var recipeCell = RecipeStorageCellItem.getRecipeCell(drive, i);
                        if (recipeCell != null && recipeCell.addPattern(encodedPattern)) {
                            if (drive instanceof AEBaseBlockEntity be) be.markForUpdate();
                            saved = true;
                            break saveSearch;
                        }
                    }
                }

                if (!saved) player.displayClientMessage(NO_SPACE_MESSAGE, true);

                for (var machine : grid.getMachines(MeMachineInterfaceBlockEntity.class)) {
                    ICraftingProvider.requestUpdate(machine.getMainNode());
                }
                ExtendedAeCompat.requestUpdateForMatrixAssemblers(grid);

                menu.syncRecipesToClient();
            } catch (Exception e) {
                Ae2craftcore.LOGGER.error("Failed to encode and save virtual recipe for player {}", player.getName().getString(), e);
            }
        });
    }

    private static EncodingMode parseMode(String modeName) {
        try {
            return EncodingMode.valueOf(modeName);
        } catch (IllegalArgumentException ignored) {
            return EncodingMode.PROCESSING;
        }
    }

    @Nullable
    private static ItemStack encodePattern(RecipeTerminalMenu menu, Player player, EncodingMode mode, String stonecuttingRecipeId, boolean substitutionsEnabled, boolean fluidSubstitutionsEnabled) {
        return switch (mode) {
            case CRAFTING -> encodeCraftingPattern(menu, player, substitutionsEnabled, fluidSubstitutionsEnabled);
            case PROCESSING -> encodeProcessingPattern(menu);
            case SMITHING_TABLE -> encodeSmithingTablePattern(menu, player, substitutionsEnabled);
            case STONECUTTING -> encodeStonecuttingPattern(menu, player, stonecuttingRecipeId);
        };
    }

    @Nullable
    private static AEItemKey extractItemKey(@Nullable GenericStack stack) {
        return (stack != null && stack.what() instanceof AEItemKey itemKey) ? itemKey : null;
    }

    private static ItemStack getRealItemStack(@Nullable GenericStack stack) {
        var itemKey = extractItemKey(stack);
        return itemKey != null ? itemKey.toStack((int) stack.amount()) : ItemStack.EMPTY;
    }

    @Nullable
    private static ItemStack encodeCraftingPattern(RecipeTerminalMenu menu, Player player, boolean substitutionsEnabled, boolean fluidSubstitutionsEnabled) {
        var part = menu.getPart();
        if (part == null) return null;

        var ingredients = new ItemStack[9];
        var craftingGrid = NonNullList.withSize(9, ItemStack.EMPTY);
        var hasInput = false;
        var inputInv = part.getLogic().getEncodedInputInv();

        for (int i = 0; i < 9; i++) {
            var stack = inputInv.getStack(i);
            var realStack = getRealItemStack(stack);
            ingredients[i] = realStack;
            craftingGrid.set(i, realStack);
            if (!realStack.isEmpty()) hasInput = true;
        }

        if (!hasInput) return null;

        var level = player.level();
        var input = CraftingInput.of(3, 3, craftingGrid);
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElse(null);
        if (recipe == null) return null;

        var result = recipe.value().assemble(input, level.registryAccess());
        if (result.isEmpty()) return null;

        return PatternDetailsHelper.encodeCraftingPattern(recipe, ingredients, result, substitutionsEnabled, fluidSubstitutionsEnabled);
    }

    @Nullable
    private static ItemStack encodeProcessingPattern(RecipeTerminalMenu menu) {
        var part = menu.getPart();
        if (part == null) return null;

        var logic = part.getLogic();
        var inputInv = logic.getEncodedInputInv();
        var outputInv = logic.getEncodedOutputInv();

        var inputs = new ArrayList<GenericStack>();
        for (int i = 0; i < inputInv.size(); i++) {
            var stack = inputInv.getStack(i);
            if (stack != null) inputs.add(stack);
        }
        if (inputs.isEmpty()) return null;

        var outputs = new ArrayList<GenericStack>();
        for (int i = 0; i < outputInv.size(); i++) {
            var stack = outputInv.getStack(i);
            if (stack != null) outputs.add(stack);
        }
        if (outputs.isEmpty()) return null;

        return PatternDetailsHelper.encodeProcessingPattern(inputs, outputs);
    }

    @Nullable
    private static ItemStack encodeSmithingTablePattern(RecipeTerminalMenu menu, Player player, boolean substitutionsEnabled) {
        var part = menu.getPart();
        if (part == null) return null;

        var inputInv = part.getLogic().getEncodedInputInv();
        var templateKey = extractItemKey(inputInv.getStack(0));
        var baseKey = extractItemKey(inputInv.getStack(1));
        var additionKey = extractItemKey(inputInv.getStack(2));

        if (templateKey == null || baseKey == null || additionKey == null) return null;

        var input = new SmithingRecipeInput(templateKey.toStack(), baseKey.toStack(), additionKey.toStack());
        var level = player.level();
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, level).orElse(null);
        if (recipe == null) return null;

        var outputStack = recipe.value().assemble(input, level.registryAccess());
        var output = AEItemKey.of(outputStack);
        if (output == null) return null;

        return PatternDetailsHelper.encodeSmithingTablePattern(recipe, templateKey, baseKey, additionKey, output, substitutionsEnabled);
    }

    @Nullable
    private static ItemStack encodeStonecuttingPattern(RecipeTerminalMenu menu, Player player, String stonecuttingRecipeId) {
        var part = menu.getPart();
        if (part == null) return null;

        var inputInv = part.getLogic().getEncodedInputInv();
        var inputKey = extractItemKey(inputInv.getStack(0));
        if (inputKey == null) return null;

        var level = player.level();
        var recipeInput = new SingleRecipeInput(inputKey.toStack());
        var recipeId = stonecuttingRecipeId.isEmpty() ? null : ResourceLocation.tryParse(stonecuttingRecipeId);

        var recipe = recipeId != null
                ? level.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, recipeInput, level, recipeId).orElse(null)
                : level.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, recipeInput, level).stream().findFirst().orElse(null);
        if (recipe == null) return null;

        var output = AEItemKey.of(recipe.value().getResultItem(level.registryAccess()));
        if (output == null) return null;

        return PatternDetailsHelper.encodeStonecuttingPattern(recipe, inputKey, output, false);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
