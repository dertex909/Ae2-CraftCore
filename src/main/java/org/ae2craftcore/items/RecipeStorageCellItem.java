package org.ae2craftcore.items;

import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import org.ae2craftcore.registry.annotations.RegisterItem;
import org.ae2craftcore.registry.AttachmentRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@RegisterItem(name = "recipe_storage_cell", stacksTo = 1)
public class RecipeStorageCellItem extends Item {

    public RecipeStorageCellItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        int count = stack.getOrDefault(AttachmentRegistry.RECIPE_COUNT.get(), 0);
        int machineCount = stack.getOrDefault(AttachmentRegistry.MACHINE_COUNT.get(), 0);
        tooltipComponents.add(Component.literal("§7Recipes: §e" + count + " §8/ §7128"));
        tooltipComponents.add(Component.literal("§7Machines: §e" + machineCount + " §8/ §716"));
    }

    public static void addRecipeToCell(ItemStack stack, ServerLevel level, String recipeId) {
        var resLoc = ResourceLocation.tryParse(recipeId);
        if (resLoc == null) return;

        var recipeOpt = level.getRecipeManager().byKey(resLoc);
        if (recipeOpt.isEmpty()) return;

        var newType = recipeOpt.get().value().getType();
        var recipes = new ArrayList<>(stack.getOrDefault(AttachmentRegistry.RECIPES.get(), List.of()));
        if (recipes.contains(recipeId)) return;

        if (recipes.size() >= 128) return;

        var uniqueTypes = new HashSet<RecipeType<?>>();
        for (var rid : recipes) {
            var rLoc = ResourceLocation.tryParse(rid);
            if (rLoc != null) {
                var rOpt = level.getRecipeManager().byKey(rLoc);
                rOpt.ifPresent(recipeHolder -> uniqueTypes.add(recipeHolder.value().getType()));
            }
        }

        if (!uniqueTypes.contains(newType) && uniqueTypes.size() >= 16) return;

        recipes.add(recipeId);
        stack.set(AttachmentRegistry.RECIPES.get(), recipes);
        stack.set(AttachmentRegistry.RECIPE_COUNT.get(), recipes.size());
        uniqueTypes.add(newType);
        stack.set(AttachmentRegistry.MACHINE_COUNT.get(), uniqueTypes.size());
    }

    public static void removeRecipeFromCell(ItemStack stack, ServerLevel level, String recipeId) {
        var recipes = new ArrayList<>(stack.getOrDefault(AttachmentRegistry.RECIPES.get(), List.of()));
        if (recipes.remove(recipeId)) {
            stack.set(AttachmentRegistry.RECIPES.get(), recipes);
            stack.set(AttachmentRegistry.RECIPE_COUNT.get(), recipes.size());

            var uniqueTypes = new HashSet<RecipeType<?>>();
            for (var rid : recipes) {
                var rLoc = ResourceLocation.tryParse(rid);
                if (rLoc != null) {
                    var rOpt = level.getRecipeManager().byKey(rLoc);
                    rOpt.ifPresent(recipeHolder -> uniqueTypes.add(recipeHolder.value().getType()));
                }
            }
            stack.set(AttachmentRegistry.MACHINE_COUNT.get(), uniqueTypes.size());
        }
    }

    public static List<String> getRecipesFromCell(ItemStack stack) {
        return stack.getOrDefault(AttachmentRegistry.RECIPES.get(), List.of());
    }

    public static class RecipeStorageCell implements StorageCell {
        private final ItemStack stack;

        public RecipeStorageCell(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public CellState getStatus() {
            int count = stack.getOrDefault(AttachmentRegistry.RECIPE_COUNT.get(), 0);
            if (count == 0) {
                return CellState.EMPTY;
            } else if (count >= 128) {
                return CellState.TYPES_FULL;
            } else {
                return CellState.NOT_EMPTY;
            }
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