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

package org.ae2craftcore.items;

import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.networking.IGrid;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.blockentity.AEBaseInvBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.AttachmentRegistry;
import org.ae2craftcore.registry.annotations.RegisterItem;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.Locale.ROOT;
import static net.minecraft.core.component.DataComponents.CUSTOM_DATA;
import static org.ae2craftcore.network.packet.RecipeTerminalSavePacket.RECIPEMACHINEGROUP;

@RegisterItem(name = "recipe_storage_cell_1k", stacksTo = 1)
@RegisterItem(name = "recipe_storage_cell_4k", stacksTo = 1)
@RegisterItem(name = "recipe_storage_cell_16k", stacksTo = 1)
@RegisterItem(name = "recipe_storage_cell_64k", stacksTo = 1)
@RegisterItem(name = "recipe_storage_cell_256k", stacksTo = 1)

@EventBusSubscriber(modid = "ae2craftcore", value = Dist.CLIENT)
public class RecipeStorageCellItem extends Item {

    private static final Style NORMAL_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false);
    private static final Style NUMBER_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0x886eff)).withItalic(false);
    public static DeferredHolder<Item, RecipeStorageCellItem> RECIPE_STORAGE_CELL_1K;
    public static DeferredHolder<Item, RecipeStorageCellItem> RECIPE_STORAGE_CELL_4K;
    public static DeferredHolder<Item, RecipeStorageCellItem> RECIPE_STORAGE_CELL_16K;
    public static DeferredHolder<Item, RecipeStorageCellItem> RECIPE_STORAGE_CELL_64K;
    public static DeferredHolder<Item, RecipeStorageCellItem> RECIPE_STORAGE_CELL_256K;

    public RecipeStorageCellItem(Properties properties) {
        super(properties);
    }

    public static Tier getTierForStack(ItemStack stack) {
        if (stack.isEmpty()) return Tier.CELL_64K;
        var item = stack.getItem();
        if (RECIPE_STORAGE_CELL_1K != null && item == RECIPE_STORAGE_CELL_1K.get()) return Tier.CELL_1K;
        if (RECIPE_STORAGE_CELL_4K != null && item == RECIPE_STORAGE_CELL_4K.get()) return Tier.CELL_4K;
        if (RECIPE_STORAGE_CELL_16K != null && item == RECIPE_STORAGE_CELL_16K.get()) return Tier.CELL_16K;
        if (RECIPE_STORAGE_CELL_64K != null && item == RECIPE_STORAGE_CELL_64K.get()) return Tier.CELL_64K;
        if (RECIPE_STORAGE_CELL_256K != null && item == RECIPE_STORAGE_CELL_256K.get()) return Tier.CELL_256K;
        return Tier.CELL_64K;
    }

    public static boolean canAddPattern(ItemStack cellStack, ItemStack patternToAdd) {
        if (!(cellStack.getItem() instanceof RecipeStorageCellItem)) return false;
        var tier = getTierForStack(cellStack);
        var storedList = cellStack.get(AttachmentRegistry.STORED_PATTERNS.get());
        var patterns = storedList != null ? new ArrayList<>(storedList) : new ArrayList<ItemStack>();
        if (patterns.size() >= tier.getMaxRecipes()) return false;
        var uniqueGroups = new HashSet<String>();
        for (var p : patterns) uniqueGroups.add(getRecipeGroup(p));
        uniqueGroups.add(getRecipeGroup(patternToAdd));
        return uniqueGroups.size() <= tier.getMaxMachines();
    }

    private static String getRecipeGroup(ItemStack p) {
        var customData = p.get(CUSTOM_DATA);
        if (customData != null) {
            var tag = customData.copyTag();
            if (tag.contains(RECIPEMACHINEGROUP)) return tag.getStringOr(RECIPEMACHINEGROUP, "").toLowerCase(ROOT);
        }
        return "";
    }

    public static void updateCellStats(ItemStack cellStack, List<ItemStack> patterns) {
        cellStack.set(AttachmentRegistry.STORED_PATTERNS.get(), List.copyOf(patterns));
        cellStack.set(AttachmentRegistry.RECIPE_COUNT.get(), patterns.size());

        var uniqueGroups = new HashSet<String>();
        for (var p : patterns) {
            var customData = p.get(CUSTOM_DATA);
            String group = "";
            if (customData != null) {
                var tag = customData.copyTag();
                if (tag.contains(RECIPEMACHINEGROUP)) group = tag.getStringOr(RECIPEMACHINEGROUP, "");
            }
            uniqueGroups.add(group.toLowerCase(ROOT));
        }
        cellStack.set(AttachmentRegistry.MACHINE_COUNT.get(), uniqueGroups.size());
    }

    public static Style colorFromRatio(double ratio, boolean oneIsGreen) {
        double p = ratio;
        if (!oneIsGreen) p = 1 - p;

        int r = (int) (255d * (Math.clamp(2 - 2 * p, 0, 1)));
        int g = (int) (255d * (Math.clamp(2 * p, 0, 1)));
        int rgb = 0xFF000000 + (r << 16) + (g << 8);

        return Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(rgb));
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();

        if (stack.getItem() instanceof RecipeStorageCellItem) {
            int count = stack.getOrDefault(AttachmentRegistry.RECIPE_COUNT.get(), 0);
            int machineCount = stack.getOrDefault(AttachmentRegistry.MACHINE_COUNT.get(), 0);

            var tier = getTierForStack(stack);
            var tooltip = event.getToolTip();

            double recipeRatio = tier.getMaxRecipes() > 0 ? (double) count / tier.getMaxRecipes() : 0.0;
            double machineRatio = tier.getMaxMachines() > 0 ? (double) machineCount / tier.getMaxMachines() : 0.0;

            var recipeCountComp = Component.literal(String.valueOf(count)).withStyle(colorFromRatio(recipeRatio, false));
            var recipeMaxComp = Component.literal(String.valueOf(tier.getMaxRecipes())).withStyle(NUMBER_STYLE);

            var machineCountComp = Component.literal(String.valueOf(machineCount)).withStyle(colorFromRatio(machineRatio, false));
            var machineMaxComp = Component.literal(String.valueOf(tier.getMaxMachines())).withStyle(NUMBER_STYLE);

            tooltip.add(Component.translatable("tooltip.ae2craftcore.recipes_used", recipeCountComp, recipeMaxComp)
                    .withStyle(NORMAL_STYLE));

            tooltip.add(Component.translatable("tooltip.ae2craftcore.machines_used", machineCountComp, machineMaxComp)
                    .withStyle(NORMAL_STYLE));
        }
    }

    @Nullable
    public static RecipeStorageCell getRecipeCell(IChestOrDrive drive, int slot) {
        if (drive == null || slot < 0 || slot >= drive.getCellCount()) return null;

        var cell = drive.getOriginalCellInventory(slot);
        if (cell instanceof RecipeStorageCell rc) return rc;

        var meStorage = drive.getCellInventory(slot);
        if (meStorage instanceof RecipeStorageCell rc) return rc;

        var item = drive.getCellItem(slot);
        if (item instanceof RecipeStorageCellItem) {
            var stack = ItemStack.EMPTY;
            ISaveProvider saveProvider = null;

            if (drive instanceof AEBaseInvBlockEntity invBe) {
                saveProvider = invBe::saveChanges;
                var inv = invBe.getInternalInventory();
                if (inv != null && slot < inv.size()) stack = inv.getStackInSlot(slot);
            } else if (drive instanceof BlockEntity be) {
                saveProvider = be::setChanged;
            }

            if (!stack.isEmpty() && stack.getItem() instanceof RecipeStorageCellItem) {
                var directCell = StorageCells.getCellInventory(stack, saveProvider);
                if (directCell instanceof RecipeStorageCell rc) return rc;
            }
        }

        return null;
    }

    public static Set<IChestOrDrive> getDrives(IGrid grid) {
        if (grid == null) return Collections.emptySet();
        var drives = new java.util.LinkedHashSet<IChestOrDrive>();
        for (var node : grid.getNodes()) if (node.getOwner() instanceof IChestOrDrive drive) drives.add(drive);
        return drives;
    }

    public static List<ItemStack> getAllPatternsForGrid(IGrid grid) {
        var list = new ArrayList<ItemStack>();
        if (grid == null) return list;

        var drives = getDrives(grid);
        for (var drive : drives) {
            int cellCount = drive.getCellCount();
            for (int i = 0; i < cellCount; i++) {
                var recipeCell = getRecipeCell(drive, i);
                if (recipeCell != null) {
                    var patterns = recipeCell.getStoredPatterns();
                    list.addAll(patterns);
                }
            }
        }
        return list;
    }

    public enum Tier {
        CELL_1K("recipe_storage_cell_1k", 2, 8),
        CELL_4K("recipe_storage_cell_4k", 6, 32),
        CELL_16K("recipe_storage_cell_16k", 16, 128),
        CELL_64K("recipe_storage_cell_64k", 32, 256),
        CELL_256K("recipe_storage_cell_256k", 64, 1024);

        private final String name;
        private final int maxMachines;
        private final int maxRecipes;

        Tier(String name, int maxMachines, int maxRecipes) {
            this.name = name;
            this.maxMachines = maxMachines;
            this.maxRecipes = maxRecipes;
        }

        public String getName() {
            return name;
        }

        public int getMaxMachines() {
            return maxMachines;
        }

        public int getMaxRecipes() {
            return maxRecipes;
        }
    }

    public static class RecipeCellHandler implements ICellHandler {

        @Override
        public boolean isCell(ItemStack is) {
            return !is.isEmpty() && is.getItem() instanceof RecipeStorageCellItem;
        }

        @Override
        public @Nullable StorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
            if (isCell(is)) return new RecipeStorageCell(is, host);
            return null;
        }
    }

    public static class RecipeStorageCell implements StorageCell {
        private final ItemStack cellStack;
        private final ISaveProvider host;
        private List<ItemStack> patterns;

        public RecipeStorageCell(ItemStack cellStack, ISaveProvider host) {
            this.cellStack = cellStack;
            this.host = host;
            this.load();
        }

        public void load() {
            var stored = cellStack.get(AttachmentRegistry.STORED_PATTERNS.get());
            if (stored != null && !stored.isEmpty()) {
                this.patterns = new ArrayList<>(stored);
            } else {
                this.patterns = new ArrayList<>();
            }
        }

        public ItemStack getCellStack() {
            return this.cellStack;
        }

        public List<ItemStack> getStoredPatterns() {
            if (this.patterns == null) load();
            return Collections.unmodifiableList(this.patterns);
        }

        public int getRecipeCount() {
            return this.patterns != null ? this.patterns.size() : 0;
        }

        public int getMachineCount() {
            return this.cellStack.getOrDefault(AttachmentRegistry.MACHINE_COUNT.get(), 0);
        }

        public boolean canAddPattern(ItemStack patternToAdd) {
            return RecipeStorageCellItem.canAddPattern(this.cellStack, patternToAdd);
        }

        public boolean addPattern(ItemStack patternToAdd) {
            if (!canAddPattern(patternToAdd)) return false;
            if (this.patterns == null) load();
            this.patterns.add(patternToAdd.copyWithCount(1));
            updateCellStats(this.cellStack, this.patterns);
            this.persist();
            return true;
        }

        public boolean deletePattern(ItemStack patternToDelete) {
            if (this.patterns == null) load();
            var toRemove = new ArrayList<ItemStack>();
            for (var p : this.patterns) if (ItemStack.isSameItemSameComponents(p, patternToDelete)) toRemove.add(p);
            if (!toRemove.isEmpty()) {
                this.patterns.removeAll(toRemove);
                updateCellStats(this.cellStack, this.patterns);
                this.persist();
                return true;
            }
            return false;
        }

        @Override
        public CellState getStatus() {
            int count = this.patterns != null ? this.patterns.size() : 0;
            var tier = getTierForStack(cellStack);
            if (count == 0) return CellState.EMPTY;
            if (count >= tier.getMaxRecipes()) return CellState.FULL;
            return CellState.NOT_EMPTY;
        }

        @Override
        public double getIdleDrain() {
            return 1.0;
        }

        @Override
        public void persist() {
            if (this.host != null) this.host.saveChanges();
        }

        @Override
        public Component getDescription() {
            return cellStack.getHoverName();
        }
    }
}