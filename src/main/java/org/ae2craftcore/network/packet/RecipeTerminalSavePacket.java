package org.ae2craftcore.network.packet;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.me.helpers.PlayerSource;
import appeng.parts.encoding.EncodingMode;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
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
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.registry.annotations.NetworkPayload;
import org.ae2craftcore.registry.annotations.PacketHandler;
import org.ae2craftcore.registry.annotations.PayloadDirection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static appeng.api.config.Actionable.MODULATE;

@NetworkPayload(direction = PayloadDirection.TO_SERVER)
public record RecipeTerminalSavePacket(String groupName, String modeName, String stonecuttingRecipeId)
        implements CustomPacketPayload {

    public static final Type<RecipeTerminalSavePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_save"));

    public RecipeTerminalSavePacket(String groupName, EncodingMode mode, @Nullable ResourceLocation stonecuttingRecipeId) {
        this(groupName, mode.name(), stonecuttingRecipeId != null ? stonecuttingRecipeId.toString() : "");
    }

    @SuppressWarnings("unused")
    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalSavePacket> STREAM_CODEC = StreamCodec.of((buf, value) -> {
        buf.writeUtf(value.groupName());
        buf.writeUtf(value.modeName());
        buf.writeUtf(value.stonecuttingRecipeId());
    }, buf -> new RecipeTerminalSavePacket(buf.readUtf(), buf.readUtf(), buf.readUtf()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @PacketHandler
    public static void handle(RecipeTerminalSavePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu instanceof RecipeTerminalMenu menu) try {
                var encodedPattern = encodePattern(menu, player, parseMode(packet.modeName()), packet.stonecuttingRecipeId());
                if (encodedPattern == null || encodedPattern.isEmpty()) return;

                var customData = encodedPattern.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                var tag = customData.copyTag();
                tag.putString("RecipeMachineGroup", packet.groupName());
                encodedPattern.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

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

                var key = AEItemKey.of(encodedPattern);
                inv.insert(key, 1, MODULATE, new PlayerSource(player));

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
    private static ItemStack encodePattern(RecipeTerminalMenu menu, Player player, EncodingMode mode, String stonecuttingRecipeId) {
        return switch (mode) {
            case CRAFTING -> encodeCraftingPattern(menu, player);
            case PROCESSING -> encodeProcessingPattern(menu);
            case SMITHING_TABLE -> encodeSmithingTablePattern(menu, player);
            case STONECUTTING -> encodeStonecuttingPattern(menu, player, stonecuttingRecipeId);
        };
    }

    @Nullable
    private static ItemStack encodeCraftingPattern(RecipeTerminalMenu menu, Player player) {
        var ingredients = new ItemStack[9];
        var craftingGrid = NonNullList.withSize(9, ItemStack.EMPTY);
        var hasInput = false;

        for (int i = 0; i < ingredients.length; i++) {
            var stack = menu.getPhantomContainer().getItem(i);
            if (stack.isEmpty()) {
                ingredients[i] = ItemStack.EMPTY;
                continue;
            }

            var ingredient = stack.copy();
            ingredient.setCount(1);
            ingredients[i] = ingredient;
            craftingGrid.set(i, ingredient);
            hasInput = true;
        }

        if (!hasInput) return null;

        var level = player.level();
        var input = CraftingInput.of(3, 3, craftingGrid);
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElse(null);
        if (recipe == null) return null;

        var result = recipe.value().assemble(input, level.registryAccess());
        if (result.isEmpty()) return null;

        return PatternDetailsHelper.encodeCraftingPattern(recipe, ingredients, result, false, true);
    }

    @Nullable
    private static ItemStack encodeProcessingPattern(RecipeTerminalMenu menu) {
        var inputs = new GenericStack[9];
        var hasInput = false;
        for (int i = 0; i < inputs.length; i++) {
            var stack = menu.getPhantomContainer().getItem(i);
            inputs[i] = GenericStack.fromItemStack(stack);
            if (inputs[i] != null) hasInput = true;
        }
        if (!hasInput) return null;

        var outputs = new GenericStack[3];
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = GenericStack.fromItemStack(menu.getPhantomContainer().getItem(9 + i));
        }
        if (outputs[0] == null) return null;

        return PatternDetailsHelper.encodeProcessingPattern(Arrays.asList(inputs), Arrays.asList(outputs));
    }

    @Nullable
    private static ItemStack encodeSmithingTablePattern(RecipeTerminalMenu menu, Player player) {
        var templateStack = menu.getPhantomContainer().getItem(0);
        var baseStack = menu.getPhantomContainer().getItem(1);
        var additionStack = menu.getPhantomContainer().getItem(2);
        if (templateStack.isEmpty() || baseStack.isEmpty() || additionStack.isEmpty()) return null;

        var template = AEItemKey.of(templateStack);
        var base = AEItemKey.of(baseStack);
        var addition = AEItemKey.of(additionStack);
        if (template == null || base == null || addition == null) return null;

        var input = new SmithingRecipeInput(template.toStack(), base.toStack(), addition.toStack());
        var level = player.level();
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, level).orElse(null);
        if (recipe == null) return null;

        var outputStack = recipe.value().assemble(input, level.registryAccess());
        var output = AEItemKey.of(outputStack);
        if (output == null) return null;

        return PatternDetailsHelper.encodeSmithingTablePattern(recipe, template, base, addition, output, false);
    }

    @Nullable
    private static ItemStack encodeStonecuttingPattern(RecipeTerminalMenu menu, Player player, String stonecuttingRecipeId) {
        var inputStack = menu.getPhantomContainer().getItem(0);
        if (inputStack.isEmpty()) return null;

        var input = AEItemKey.of(inputStack);
        if (input == null) return null;

        var level = player.level();
        var recipeInput = new SingleRecipeInput(input.toStack());
        var recipeId = stonecuttingRecipeId.isEmpty() ? null : ResourceLocation.tryParse(stonecuttingRecipeId);
        var recipe = recipeId != null
                ? level.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, recipeInput, level, recipeId).orElse(null)
                : level.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, recipeInput, level).stream().findFirst().orElse(null);
        if (recipe == null) return null;

        var output = AEItemKey.of(recipe.value().getResultItem(level.registryAccess()));
        if (output == null) return null;

        return PatternDetailsHelper.encodeStonecuttingPattern(recipe, input, output, false);
    }
}