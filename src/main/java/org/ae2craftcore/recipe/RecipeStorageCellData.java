package org.ae2craftcore.recipe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RecipeStorageCellData extends SavedData {
    private final HashMap<UUID, List<String>> cellRecipes = new HashMap<>();

    public RecipeStorageCellData() {
    }

    public static RecipeStorageCellData load(CompoundTag tag, HolderLookup.Provider registries) {
        var data = new RecipeStorageCellData();
        var cellsTag = tag.getCompound("cells");
        for (var key : cellsTag.getAllKeys()) {
            try {
                var uuid = UUID.fromString(key);
                var list = cellsTag.getList(key, Tag.TAG_STRING);
                var recipes = new ArrayList<String>();
                for (int i = 0; i < list.size(); i++) recipes.add(list.getString(i));
                data.cellRecipes.put(uuid, recipes);
            } catch (Exception ignored) {
            }
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        var cellsTag = new CompoundTag();
        for (var entry : cellRecipes.entrySet()) {
            var list = new ListTag();
            for (var recipe : entry.getValue()) list.add(StringTag.valueOf(recipe));
            cellsTag.put(entry.getKey().toString(), list);
        }
        tag.put("cells", cellsTag);
        return tag;
    }

    public List<String> getRecipes(UUID uuid) {
        return cellRecipes.computeIfAbsent(uuid, k -> new ArrayList<>());
    }

    public void addRecipe(UUID uuid, String recipeId) {
        var recipes = getRecipes(uuid);
        if (!recipes.contains(recipeId)) {
            recipes.add(recipeId);
            setDirty();
        }
    }

    public void removeRecipe(UUID uuid, String recipeId) {
        var recipes = cellRecipes.get(uuid);
        if (recipes != null && recipes.remove(recipeId)) setDirty();
    }

    public static RecipeStorageCellData get(net.minecraft.server.level.ServerLevel level) {
        var storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(RecipeStorageCellData::new, RecipeStorageCellData::load,
                DataFixTypes.SAVED_DATA_COMMAND_STORAGE), "ae2craftcore_recipe_storage_cells");
    }
}