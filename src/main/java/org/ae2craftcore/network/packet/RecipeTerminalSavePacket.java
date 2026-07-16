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
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.crafting.RecipeAccess;
import appeng.parts.encoding.EncodingMode;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
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
import static org.ae2craftcore.registry.AttachmentRegistry.STORED_PATTERNS;

@NetworkPayload(direction = PayloadDirection.TO_SERVER)
public record RecipeTerminalSavePacket(String groupName, String modeName, String stonecuttingRecipeId,
                                       boolean substitutionsEnabled, boolean fluidSubstitutionsEnabled)
        implements CustomPacketPayload {

    public static final Type<RecipeTerminalSavePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_save"));
    public static final String RECIPEMACHINEGROUP = "RecMacG";
    @SuppressWarnings("unused")
    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalSavePacket> STREAM_CODEC = StreamCodec.of((buf, value) -> {
        buf.writeUtf(value.groupName());
        buf.writeUtf(value.modeName());
        buf.writeUtf(value.stonecuttingRecipeId());
        buf.writeBoolean(value.substitutionsEnabled());
        buf.writeBoolean(value.fluidSubstitutionsEnabled());
    }, buf -> new RecipeTerminalSavePacket(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readBoolean()));

    public RecipeTerminalSavePacket(String groupName, EncodingMode mode, @Nullable ResourceKey<Recipe<?>> stonecuttingRecipeId, boolean substitutionsEnabled, boolean fluidSubstitutionsEnabled) {
        this(groupName, mode.name(), stonecuttingRecipeId != null ? stonecuttingRecipeId.identifier().toString() : "", substitutionsEnabled, fluidSubstitutionsEnabled);
    }

    @PacketHandler
    public static void handle(RecipeTerminalSavePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu instanceof RecipeTerminalMenu menu) try {
                var encodedPattern = encodePattern(menu, player, parseMode(packet.modeName()), packet.stonecuttingRecipeId(), packet.substitutionsEnabled(), packet.fluidSubstitutionsEnabled());
                if (encodedPattern == null || encodedPattern.isEmpty()) return;

                var customData = encodedPattern.getOrDefault(CUSTOM_DATA, CustomData.EMPTY);
                var tag = customData.copyTag();
                tag.putString(RECIPEMACHINEGROUP, packet.groupName());
                encodedPattern.set(CUSTOM_DATA, CustomData.of(tag));

                var part = menu.getPart();
                if (part == null) return;
                var node = part.getGridNode();
                if (node == null) return;
                var grid = node.getGrid();
                if (grid == null) return;

                var cacheService = grid.getService(IRecipeCacheService.class);
                if (cacheService != null) cacheService.invalidate();

                boolean saved = false;

                for (var drive : grid.getMachines(DriveBlockEntity.class)) {
                    var inv = drive.getInternalInventory();
                    if (inv != null) for (int i = 0; i < inv.size(); i++) {
                        var stack = inv.getStackInSlot(i);
                        if (stack.getItem() instanceof RecipeStorageCellItem) {
                            if (RecipeStorageCellItem.canAddPattern(stack, encodedPattern)) {
                                var storedList = stack.get(STORED_PATTERNS.get());
                                var patterns = storedList != null ? new ArrayList<>(storedList) : new ArrayList<ItemStack>();
                                patterns.add(encodedPattern.copyWithCount(1));

                                RecipeStorageCellItem.updateCellStats(stack, patterns);

                                inv.setItemDirect(i, stack);
                                drive.saveChanges();

                                saved = true;
                                break;
                            }
                        }
                    }
                    if (saved) break;
                }

                if (!saved) {
                    for (var chest : grid.getMachines(MEChestBlockEntity.class)) {
                        var inv = chest.getInternalInventory();
                        if (inv != null) for (int i = 0; i < inv.size(); i++) {
                            var stack = inv.getStackInSlot(i);
                            if (stack.getItem() instanceof RecipeStorageCellItem) {
                                if (RecipeStorageCellItem.canAddPattern(stack, encodedPattern)) {
                                    var storedList = stack.get(STORED_PATTERNS.get());
                                    var patterns = storedList != null ? new ArrayList<>(storedList) : new ArrayList<ItemStack>();
                                    patterns.add(encodedPattern.copyWithCount(1));

                                    RecipeStorageCellItem.updateCellStats(stack, patterns);

                                    inv.setItemDirect(i, stack);
                                    chest.saveChanges();

                                    saved = true;
                                    break;
                                }
                            }
                        }
                        if (saved) break;
                    }
                }

                if (!saved) {
                    player.sendOverlayMessage(Component.literal("§cNo active Recipe Storage Cells with available space found in the network!"));
                }

                for (var machine : grid.getMachines(MeMachineInterfaceBlockEntity.class)) {
                    ICraftingProvider.requestUpdate(machine.getMainNode());
                }
                ExtendedAeCompat.requestUpdateForMatrixAssemblers(grid);

                menu.syncRecipesToClient();
            } catch (Exception e) {
                Ae2craftcore.LOGGER.error("Failed to encode and save virtual recipe: ", e);
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

    private static ItemStack getRealItemStack(@Nullable GenericStack stack) {
        if (stack == null) return ItemStack.EMPTY;
        if (stack.what() instanceof AEItemKey itemKey) return itemKey.toStack((int) stack.amount());
        return ItemStack.EMPTY;
    }

    @Nullable
    private static ItemStack encodeCraftingPattern(RecipeTerminalMenu menu, Player player, boolean substitutionsEnabled, boolean fluidSubstitutionsEnabled) {
        var ingredients = new ItemStack[9];
        var craftingGrid = NonNullList.withSize(9, ItemStack.EMPTY);
        var hasInput = false;
        var inputInv = menu.getPart().getLogic().getEncodedInputInv();

        for (int i = 0; i < 9; i++) {
            var stack = inputInv.getStack(i);
            if (stack == null) {
                ingredients[i] = ItemStack.EMPTY;
                continue;
            }

            var realStack = getRealItemStack(stack);
            ingredients[i] = realStack;
            craftingGrid.set(i, realStack);
            if (!realStack.isEmpty()) hasInput = true;
        }

        if (!hasInput) return null;

        var level = player.level();
        var input = CraftingInput.of(3, 3, craftingGrid);
        var recipe = RecipeAccess.getRecipesFor(level, RecipeType.CRAFTING, input).findFirst().orElse(null);
        if (recipe == null) return null;

        var result = recipe.value().assemble(input);
        if (result.isEmpty()) return null;

        return PatternDetailsHelper.encodeCraftingPattern(recipe, ingredients, result, substitutionsEnabled, fluidSubstitutionsEnabled);
    }

    @Nullable
    private static ItemStack encodeProcessingPattern(RecipeTerminalMenu menu) {
        var logic = menu.getPart().getLogic();
        var inputInv = logic.getEncodedInputInv();
        var outputInv = logic.getEncodedOutputInv();

        var inputs = new ArrayList<GenericStack>();
        var hasInput = false;
        for (int i = 0; i < inputInv.size(); i++) {
            var stack = inputInv.getStack(i);
            if (stack != null) {
                inputs.add(stack);
                hasInput = true;
            }
        }
        if (!hasInput) return null;

        var outputs = new ArrayList<GenericStack>();
        var hasOutput = false;
        for (int i = 0; i < outputInv.size(); i++) {
            var stack = outputInv.getStack(i);
            if (stack != null) {
                outputs.add(stack);
                hasOutput = true;
            }
        }
        if (!hasOutput) return null;

        return PatternDetailsHelper.encodeProcessingPattern(inputs, outputs);
    }

    @Nullable
    private static ItemStack encodeSmithingTablePattern(RecipeTerminalMenu menu, Player player, boolean substitutionsEnabled) {
        var inputInv = menu.getPart().getLogic().getEncodedInputInv();
        var templateStack = inputInv.getStack(0);
        var baseStack = inputInv.getStack(1);
        var additionStack = inputInv.getStack(2);
        if (templateStack == null || baseStack == null || additionStack == null) return null;

        var templateKey = templateStack.what() instanceof AEItemKey k ? k : null;
        var baseKey = baseStack.what() instanceof AEItemKey k ? k : null;
        var additionKey = additionStack.what() instanceof AEItemKey k ? k : null;
        if (templateKey == null || baseKey == null || additionKey == null) return null;

        var input = new SmithingRecipeInput(templateKey.toStack(), baseKey.toStack(), additionKey.toStack());
        var level = player.level();
        var recipe = RecipeAccess.getRecipesFor(level, RecipeType.SMITHING, input).findFirst().orElse(null);
        if (recipe == null) return null;

        var outputStack = recipe.value().assemble(input);
        var output = AEItemKey.of(outputStack);
        if (output == null) return null;

        return PatternDetailsHelper.encodeSmithingTablePattern(recipe, templateKey, baseKey, additionKey, output, substitutionsEnabled);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static ItemStack encodeStonecuttingPattern(RecipeTerminalMenu menu, Player player, String stonecuttingRecipeId) {
        var inputInv = menu.getPart().getLogic().getEncodedInputInv();
        var inputStack = inputInv.getStack(0);
        if (inputStack == null) return null;

        var inputKey = inputStack.what() instanceof AEItemKey k ? k : null;
        if (inputKey == null) return null;

        var level = player.level();
        var recipeInput = new SingleRecipeInput(inputKey.toStack());

        RecipeHolder<StonecutterRecipe> recipe = null;
        var parsedId = Identifier.tryParse(stonecuttingRecipeId);
        var recipeKey = parsedId == null ? null : ResourceKey.create(Registries.RECIPE, parsedId);

        if (recipeKey != null) {
            if (level instanceof ServerLevel serverLevel) {
                var recipeManager = serverLevel.getServer().getRecipeManager();
                var opt = recipeManager.byKey(recipeKey);
                if (opt.isPresent()) recipe = (RecipeHolder<StonecutterRecipe>) opt.get();
            }
        } else {
            recipe = RecipeAccess.getRecipesFor(level, RecipeType.STONECUTTING, recipeInput).findFirst().orElse(null);
        }

        if (recipe == null) return null;

        var output = AEItemKey.of(recipe.value().assemble(recipeInput));
        if (output == null) return null;

        return PatternDetailsHelper.encodeStonecuttingPattern(recipe, inputKey, output, false);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}