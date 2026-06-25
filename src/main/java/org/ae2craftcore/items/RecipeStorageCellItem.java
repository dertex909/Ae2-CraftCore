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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
            var stored = cellStack.get(AttachmentRegistry.STORED_PATTERNS.get());
            if (stored != null) this.patterns.addAll(stored);
        }

        private void save() {
            cellStack.set(AttachmentRegistry.STORED_PATTERNS.get(), List.copyOf(this.patterns));
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
            this.save();
            if (host != null) host.saveChanges();
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