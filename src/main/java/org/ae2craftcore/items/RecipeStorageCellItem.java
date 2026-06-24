package org.ae2craftcore.items;

import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.ae2craftcore.recipe.RecipeStorageCellData;
import org.ae2craftcore.registry.annotations.RegisterItem;
import org.ae2craftcore.registry.AttachmentRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@RegisterItem(name = "recipe_storage_cell", stacksTo = 1)
public class RecipeStorageCellItem extends Item {

    public RecipeStorageCellItem(Properties properties) {
        super(properties);
    }

    public static UUID getOrAndInitUuid(ItemStack stack) {
        if (stack.has(AttachmentRegistry.CELL_UUID.get())) try {
            String uuidStr = stack.get(AttachmentRegistry.CELL_UUID.get());
            if (uuidStr != null) return UUID.fromString(uuidStr);
        } catch (Exception ignored) {
        }
        var uuid = UUID.randomUUID();
        stack.set(AttachmentRegistry.CELL_UUID.get(), uuid.toString());
        return uuid;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        var uuid = getOrAndInitUuid(stack);
        int count = stack.getOrDefault(AttachmentRegistry.RECIPE_COUNT.get(), 0);
        tooltipComponents.add(Component.literal("§7ID: §b" + uuid.toString().substring(0, 8) + "..."));
        tooltipComponents.add(Component.literal("§7Recipes: §e" + count + " §8/ §7128"));
        tooltipComponents.add(Component.literal("§8Holds encoded machine recipes in World Saved Data."));
    }

    public static void addRecipeToCell(ItemStack stack, ServerLevel level, String recipeId) {
        var uuid = getOrAndInitUuid(stack);
        var data = RecipeStorageCellData.get(level);
        data.addRecipe(uuid, recipeId);
        stack.set(AttachmentRegistry.RECIPE_COUNT.get(), data.getRecipes(uuid).size());
    }

    public static void removeRecipeFromCell(ItemStack stack, ServerLevel level, String recipeId) {
        var uuid = getOrAndInitUuid(stack);
        var data = RecipeStorageCellData.get(level);
        data.removeRecipe(uuid, recipeId);
        stack.set(AttachmentRegistry.RECIPE_COUNT.get(), data.getRecipes(uuid).size());
    }

    public static List<String> getRecipesFromCell(ItemStack stack, ServerLevel level) {
        var uuid = getOrAndInitUuid(stack);
        var data = RecipeStorageCellData.get(level);
        return data.getRecipes(uuid);
    }

    public static class RecipeStorageCell implements StorageCell {
        public RecipeStorageCell(ItemStack stack) {
            getOrAndInitUuid(stack);
        }

        @Override
        public CellState getStatus() {
            return CellState.EMPTY;
        }

        @Override
        public double getIdleDrain() {
            return 0;
        }

        @Override
        public void persist() {
        }

        @Override
        public Component getDescription() {
            return Component.literal("Recipe Storage Cell");
        }
    }

    public static class RecipeCellHandler implements ICellHandler {
        @Override
        public boolean isCell(ItemStack is) {
            return is.getItem() instanceof RecipeStorageCellItem;
        }

        @Override
        public @Nullable StorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
            if (isCell(is)) return new RecipeStorageCell(is);
            return null;
        }
    }
}