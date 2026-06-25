package org.ae2craftcore.items;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.IGrid;
import appeng.api.storage.MEStorage;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.crafting.IPatternDetails;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AEPatternDecoder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.ae2craftcore.registry.annotations.RegisterItem;
import org.ae2craftcore.registry.AttachmentRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@RegisterItem(name = "recipe_storage_cell", stacksTo = 1)
public class RecipeStorageCellItem extends Item {

    public static final Map<IGrid, Set<RecipeStorageCell>> GRID_CELLS = Collections.synchronizedMap(new WeakHashMap<>());

    public RecipeStorageCellItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        int count = stack.getOrDefault(AttachmentRegistry.RECIPE_COUNT.get(), 0);
        int machineCount = stack.getOrDefault(AttachmentRegistry.MACHINE_COUNT.get(), 0);
        tooltipComponents.add(Component.literal("§7Recipes (Patterns): §e" + count + " §8/ §7128"));
        tooltipComponents.add(Component.literal("§7Machines: §e" + machineCount + " §8/ §716"));
    }

    public static List<IPatternDetails> getPatternsForGrid(IGrid grid, Level level) {
        var list = new ArrayList<IPatternDetails>();
        synchronized (GRID_CELLS) {
            for (var cells : GRID_CELLS.values()) {
                for (var cell : cells) {
                    if (cell.getGrid() == grid) for (var patternStack : cell.getPatterns()) {
                        var details = AEPatternDecoder.INSTANCE.decodePattern(AEItemKey.of(patternStack), level);
                        if (details != null) list.add(details);
                    }
                }
            }
        }
        return list;
    }

    public static class RecipeStorageCell implements StorageCell, MEStorage {
        private final ItemStack cellStack;
        private final ISaveProvider host;
        private final List<ItemStack> patterns = new ArrayList<>();

        public RecipeStorageCell(ItemStack cellStack, ISaveProvider host) {
            this.cellStack = cellStack;
            this.host = host;
            this.load();
        }

        private void load() {
            this.patterns.clear();
            var tag = cellStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
            if (tag.contains("StoredPatterns", 9)) {
                var list = tag.getList("StoredPatterns", 10);
                HolderLookup.Provider registries = null;
                if (host instanceof BlockEntity b && b.getLevel() != null) registries = b.getLevel().registryAccess();

                for (int i = 0; i < list.size(); i++) {
                    var itemTag = list.getCompound(i);
                    ItemStack patternStack;
                    if (registries != null) {
                        patternStack = ItemStack.parseOptional(registries, itemTag);
                    } else {
                        patternStack = ItemStack.parse(RegistryAccess.EMPTY, itemTag).orElse(ItemStack.EMPTY);
                    }
                    if (!patternStack.isEmpty()) this.patterns.add(patternStack);
                }
            }
        }

        private void save() {
            var tag = cellStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            var list = new ListTag();
            HolderLookup.Provider registries = null;
            if (host instanceof BlockEntity be && be.getLevel() != null) registries = be.getLevel().registryAccess();
            for (var pattern : this.patterns) {
                CompoundTag itemTag;
                itemTag = (CompoundTag) pattern.save(Objects.requireNonNullElse(registries, RegistryAccess.EMPTY));
                list.add(itemTag);
            }
            tag.put("StoredPatterns", list);
            cellStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            cellStack.set(AttachmentRegistry.RECIPE_COUNT.get(), this.patterns.size());
            var uniqueTypes = new HashSet<Item>();
            for (var p : this.patterns) uniqueTypes.add(p.getItem());
            cellStack.set(AttachmentRegistry.MACHINE_COUNT.get(), uniqueTypes.size());
        }

        public List<ItemStack> getPatterns() {
            return Collections.unmodifiableList(this.patterns);
        }

        public IGrid getGrid() {
            if (host instanceof IActionHost actionHost) {
                var node = actionHost.getActionableNode();
                if (node != null) return node.getGrid();
            }
            return null;
        }

        @Override
        public CellState getStatus() {
            int count = this.patterns.size();
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

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof AEItemKey itemKey)) return amount;
            var stack = itemKey.toStack();
            if (!stack.is(AEItems.BLANK_PATTERN.get())) return amount;
            if (this.patterns.size() >= 128) return amount;

            if (mode == Actionable.MODULATE) {
                this.patterns.add(stack);
                this.persist();
            }
            return 0;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof AEItemKey itemKey)) return 0;
            var stack = itemKey.toStack();

            var found = ItemStack.EMPTY;
            for (var p : this.patterns) {
                if (ItemStack.isSameItemSameComponents(p, stack)) {
                    found = p;
                    break;
                }
            }
            if (found.isEmpty()) return 0;

            if (mode == Actionable.MODULATE) {
                this.patterns.remove(found);
                this.persist();
            }
            return 1;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            for (ItemStack p : this.patterns) out.add(Objects.requireNonNull(AEItemKey.of(p)), 1);
        }
    }

    public static class RecipeCellHandler implements ICellHandler {
        @Override
        public boolean isCell(ItemStack is) {
            return is.getItem() instanceof RecipeStorageCellItem;
        }

        @Override
        public @Nullable StorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
            if (isCell(is)) {
                var cell = new RecipeStorageCell(is, host);
                if (host instanceof IActionHost actionHost) {
                    var node = actionHost.getActionableNode();
                    if (node != null) {
                        var grid = node.getGrid();
                        if (grid != null) {
                            GRID_CELLS.computeIfAbsent(grid, g -> Collections.newSetFromMap(new WeakHashMap<>())).add(cell);
                        }
                    }
                }
                return cell;
            }
            return null;
        }
    }
}