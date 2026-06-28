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
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.crafting.pattern.AEPatternDecoder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.ae2craftcore.registry.annotations.RegisterItem;
import org.ae2craftcore.registry.AttachmentRegistry;
import org.ae2craftcore.blocks.blockentity.SfpModuleBlockEntity;
import org.ae2craftcore.blocks.blockentity.OpticalInterfaceBlockEntity;
import org.ae2craftcore.blocks.blockentity.CryostatBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@RegisterItem(name = "recipe_storage_cell", stacksTo = 1)
public class RecipeStorageCellItem extends Item {

    public static final Set<RecipeStorageCell> ACTIVE_CELLS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

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

    public static boolean isQuantumComputerValidForGrid(IGrid grid, Level level) {
        if (grid == null || level == null) return false;
        var machines = grid.getMachines(SfpModuleBlockEntity.class);
        if (machines == null || machines.isEmpty()) return false;
        for (var sfp : machines) {
            if (sfp.hasActiveConnection()) {
                var interfacePos = sfp.getConnectedInterfacePos();
                if (interfacePos != null && level.isLoaded(interfacePos)) {
                    var be = level.getBlockEntity(interfacePos);
                    if (be instanceof OpticalInterfaceBlockEntity opt && opt.hasLinkedCore()) {
                        var corePos = opt.getLinkedCorePos();
                        if (corePos != null && level.isLoaded(corePos)) {
                            var coreBe = level.getBlockEntity(corePos);
                            if (coreBe instanceof CryostatBlockEntity cryostat) {
                                if (cryostat.isStructureValid() && cryostat.areInventoriesValid() && cryostat.isMePowered()) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static List<ItemStack> getAllPatternsForGrid(IGrid grid) {
        var list = new ArrayList<ItemStack>();
        if (grid == null) return list;
        synchronized (ACTIVE_CELLS) {
            for (var cell : ACTIVE_CELLS) if (cell != null && cell.getGrid() == grid) list.addAll(cell.getPatterns());
        }
        return list;
    }

    public static List<IPatternDetails> getPatternsForGrid(IGrid grid, Level level) {
        if (!isQuantumComputerValidForGrid(grid, level)) return List.of();
        var list = new ArrayList<IPatternDetails>();
        synchronized (ACTIVE_CELLS) {
            for (var cell : ACTIVE_CELLS) {
                if (cell != null && cell.getGrid() == grid) for (var patternStack : cell.getPatterns()) {
                    var details = AEPatternDecoder.INSTANCE.decodePattern(AEItemKey.of(patternStack), level);
                    if (details != null) list.add(details);
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
            ACTIVE_CELLS.add(this);
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

        public Level getLevel() {
            if (host instanceof IActionHost actionHost) {
                var node = actionHost.getActionableNode();
                if (node != null) return node.getLevel();
            }
            if (host instanceof BlockEntity be) return be.getLevel();
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
            if (amount <= 0) return 0;
            if (!(what instanceof AEItemKey itemKey)) return 0;
            var stack = itemKey.toStack();
            if (!PatternDetailsHelper.isEncodedPattern(stack)) return 0;
            if (this.patterns.size() >= 128) return 0;

            if (mode == Actionable.MODULATE) {
                this.patterns.add(stack.copyWithCount(1));
                this.persist();
            }
            return 1;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (amount <= 0) return 0;
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