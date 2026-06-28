package org.ae2craftcore.items;

import appeng.api.networking.security.IActionHost;
import appeng.api.networking.IGrid;
import appeng.api.storage.MEStorage;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.api.stacks.AEItemKey;
import appeng.api.crafting.IPatternDetails;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
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
import org.ae2craftcore.util.ICellContainerHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@RegisterItem(name = "recipe_storage_cell", stacksTo = 1)
public class RecipeStorageCellItem extends Item {

    public static final Set<RecipeStorageCell> ACTIVE_CELLS = Collections.synchronizedSet(new HashSet<>());

    private static void pruneCells() {
        synchronized (ACTIVE_CELLS) {
            ACTIVE_CELLS.removeIf(cell -> {
                if (cell == null) return true;
                if (cell.host instanceof BlockEntity be) return be.isRemoved();
                return false;
            });
        }
    }

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
        pruneCells();
        synchronized (ACTIVE_CELLS) {
            for (var cell : ACTIVE_CELLS) if (cell != null && cell.belongsToGrid(grid)) list.addAll(cell.getPatterns());
        }
        return list;
    }

    public static List<IPatternDetails> getPatternsForGrid(IGrid grid, Level level) {
        if (!isQuantumComputerValidForGrid(grid, level)) return List.of();
        var list = new ArrayList<IPatternDetails>();
        pruneCells();
        synchronized (ACTIVE_CELLS) {
            for (var cell : ACTIVE_CELLS) {
                if (cell != null && cell.belongsToGrid(grid)) for (var patternStack : cell.getPatterns()) {
                    var details = AEPatternDecoder.INSTANCE.decodePattern(AEItemKey.of(patternStack), level);
                    if (details != null) list.add(details);
                }
            }
        }
        return list;
    }

    public static List<RecipeStorageCell> getCellsForGrid(IGrid grid) {
        var list = new ArrayList<RecipeStorageCell>();
        if (grid == null) return list;
        pruneCells();
        synchronized (ACTIVE_CELLS) {
            for (var cell : ACTIVE_CELLS) {
                if (cell == null) continue;
                if (cell.belongsToGrid(grid)) list.add(cell);
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

        public void addPattern(ItemStack pattern) {
            this.patterns.add(pattern.copyWithCount(1));
            this.persist();
        }

        public boolean removePattern(ItemStack pattern) {
            var toRemove = new ArrayList<ItemStack>();
            for (var p : this.patterns) if (ItemStack.isSameItemSameComponents(p, pattern)) toRemove.add(p);
            if (!toRemove.isEmpty()) {
                this.patterns.removeAll(toRemove);
                this.persist();
                return true;
            }
            return false;
        }

        public boolean belongsToGrid(IGrid grid) {
            if (grid == null) return false;

            var directGrid = getGrid();
            if (directGrid == grid) return true;

            if (host instanceof ICellContainerHost containerHost) {
                if (containerHost.ae2craftcore$getGrid() == grid && containerHost.ae2craftcore$containsCell(this.cellStack)) {
                    return true;
                }
            }

            for (var drive : grid.getMachines(DriveBlockEntity.class)) {
                if (drive instanceof ICellContainerHost containerHost) {
                    if (containerHost.ae2craftcore$containsCell(this.cellStack)) return true;
                }
            }

            for (var chest : grid.getMachines(MEChestBlockEntity.class)) {
                if (chest instanceof ICellContainerHost containerHost) {
                    if (containerHost.ae2craftcore$containsCell(this.cellStack)) return true;
                }
            }

            return false;
        }

        public IGrid getGrid() {
            if (host instanceof IActionHost actionHost) {
                var node = actionHost.getActionableNode();
                if (node != null) return node.getGrid();
            }
            if (host instanceof ICellContainerHost containerHost) return containerHost.ae2craftcore$getGrid();
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