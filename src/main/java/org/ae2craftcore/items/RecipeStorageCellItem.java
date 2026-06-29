package org.ae2craftcore.items;

import appeng.api.networking.IGrid;
import appeng.api.storage.MEStorage;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.api.stacks.AEItemKey;
import appeng.api.crafting.IPatternDetails;
import appeng.crafting.pattern.AEPatternDecoder;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@RegisterItem(name = "recipe_storage_cell", stacksTo = 1)
public class RecipeStorageCellItem extends Item {

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

        for (var drive : grid.getMachines(DriveBlockEntity.class)) {
            var inv = drive.getInternalInventory();
            if (inv != null) for (int i = 0; i < inv.size(); i++) {
                var stack = inv.getStackInSlot(i);
                if (stack.getItem() instanceof RecipeStorageCellItem) {
                    var stored = stack.get(AttachmentRegistry.STORED_PATTERNS.get());
                    if (stored != null) list.addAll(stored);
                }
            }
        }

        for (var chest : grid.getMachines(MEChestBlockEntity.class)) {
            var inv = chest.getInternalInventory();
            if (inv != null) for (int i = 0; i < inv.size(); i++) {
                var stack = inv.getStackInSlot(i);
                if (stack.getItem() instanceof RecipeStorageCellItem) {
                    var stored = stack.get(AttachmentRegistry.STORED_PATTERNS.get());
                    if (stored != null) list.addAll(stored);
                }
            }
        }

        return list;
    }

    public static List<IPatternDetails> getPatternsForGrid(IGrid grid, Level level) {
        if (!isQuantumComputerValidForGrid(grid, level)) return List.of();
        var list = new ArrayList<IPatternDetails>();
        for (var patternStack : getAllPatternsForGrid(grid)) {
            var details = AEPatternDecoder.INSTANCE.decodePattern(AEItemKey.of(patternStack), level);
            if (details != null) list.add(details);
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