package org.ae2craftcore.items;

import appeng.api.networking.IGrid;
import appeng.api.storage.MEStorage;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.annotations.RegisterItem;
import org.ae2craftcore.registry.AttachmentRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.minecraft.core.component.DataComponents.CUSTOM_DATA;
import static org.ae2craftcore.network.packet.RecipeTerminalSavePacket.RECIPEMACHINEGROUP;

@RegisterItem(name = "recipe_storage_cell_1k", stacksTo = 1)
@RegisterItem(name = "recipe_storage_cell_4k", stacksTo = 1)
@RegisterItem(name = "recipe_storage_cell_16k", stacksTo = 1)
@RegisterItem(name = "recipe_storage_cell_64k", stacksTo = 1)
@RegisterItem(name = "recipe_storage_cell_256k", stacksTo = 1)
public class RecipeStorageCellItem extends Item {

    public static DeferredHolder<Item, RecipeStorageCellItem> RECIPE_STORAGE_CELL_1K;
    public static DeferredHolder<Item, RecipeStorageCellItem> RECIPE_STORAGE_CELL_4K;
    public static DeferredHolder<Item, RecipeStorageCellItem> RECIPE_STORAGE_CELL_16K;
    public static DeferredHolder<Item, RecipeStorageCellItem> RECIPE_STORAGE_CELL_64K;
    public static DeferredHolder<Item, RecipeStorageCellItem> RECIPE_STORAGE_CELL_256K;

    public enum Tier {
        CELL_1K("recipe_storage_cell_1k", 2, 16),
        CELL_4K("recipe_storage_cell_4k", 4, 32),
        CELL_16K("recipe_storage_cell_16k", 8, 64),
        CELL_64K("recipe_storage_cell_64k", 16, 128),
        CELL_256K("recipe_storage_cell_256k", 32, 256);

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
            if (tag.contains(RECIPEMACHINEGROUP)) return tag.getString(RECIPEMACHINEGROUP).toLowerCase(Locale.ROOT);
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
                if (tag.contains(RECIPEMACHINEGROUP)) group = tag.getString(RECIPEMACHINEGROUP);
            }
            uniqueGroups.add(group.toLowerCase(Locale.ROOT));
        }
        cellStack.set(AttachmentRegistry.MACHINE_COUNT.get(), uniqueGroups.size());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        int count = stack.getOrDefault(AttachmentRegistry.RECIPE_COUNT.get(), 0);
        int machineCount = stack.getOrDefault(AttachmentRegistry.MACHINE_COUNT.get(), 0);

        var tier = getTierForStack(stack);

        tooltipComponents.add(Component.literal("Recipes (Patterns): ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(count)).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" / " + tier.getMaxRecipes()).withStyle(ChatFormatting.DARK_GRAY)));

        tooltipComponents.add(Component.literal("Machines: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(machineCount)).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" / " + tier.getMaxMachines()).withStyle(ChatFormatting.DARK_GRAY)));
    }

    public static List<ItemStack> getAllPatternsForGrid(IGrid grid) {
        var list = new ArrayList<ItemStack>();
        if (grid == null) return list;

        for (var drive : grid.getMachines(DriveBlockEntity.class)) {
            collectPatternsFromInventory(drive.getInternalInventory(), list);
        }

        for (var chest : grid.getMachines(MEChestBlockEntity.class)) {
            collectPatternsFromInventory(chest.getInternalInventory(), list);
        }

        return list;
    }

    private static void collectPatternsFromInventory(InternalInventory inv, List<ItemStack> list) {
        if (inv != null) for (int i = 0; i < inv.size(); i++) {
            var stack = inv.getStackInSlot(i);
            if (stack.getItem() instanceof RecipeStorageCellItem) {
                var stored = stack.get(AttachmentRegistry.STORED_PATTERNS.get());
                if (stored != null) list.addAll(stored);
            }
        }
    }

    public static class RecipeStorageCell implements StorageCell, MEStorage {
        private final ItemStack cellStack;
        private final ISaveProvider host;
        private List<ItemStack> patterns;

        public RecipeStorageCell(ItemStack cellStack, ISaveProvider host) {
            this.cellStack = cellStack;
            this.host = host;
            this.load();
        }

        private void load() {
            var stored = cellStack.get(AttachmentRegistry.STORED_PATTERNS.get());
            if (stored != null && !stored.isEmpty()) {
                this.patterns = new ArrayList<>(stored);
            } else {
                this.patterns = Collections.emptyList();
            }
        }

        @Override
        public CellState getStatus() {
            int count = this.patterns.size();
            var tier = getTierForStack(cellStack);
            if (count == 0) {
                return CellState.EMPTY;
            } else if (count >= tier.getMaxRecipes()) {
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
            if (host != null) host.saveChanges();
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
            if (isCell(is)) return new RecipeStorageCell(is, host);
            return null;
        }
    }
}