package org.ae2craftcore.items;

import appeng.api.networking.IGrid;
import appeng.api.storage.MEStorage;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.api.stacks.AEItemKey;
import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.crafting.pattern.AEPatternDecoder;
import net.minecraft.ChatFormatting;
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

    public RecipeStorageCellItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        int count = stack.getOrDefault(AttachmentRegistry.RECIPE_COUNT.get(), 0);
        int machineCount = stack.getOrDefault(AttachmentRegistry.MACHINE_COUNT.get(), 0);

        tooltipComponents.add(Component.literal("Recipes (Patterns): ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(count)).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" / 128").withStyle(ChatFormatting.DARK_GRAY)));

        tooltipComponents.add(Component.literal("Machines: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(machineCount)).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" / 16").withStyle(ChatFormatting.DARK_GRAY)));
    }

    public static List<ItemStack> getAllPatternsForGrid(IGrid grid) {
        var list = new ArrayList<ItemStack>();
        if (grid == null) return list;

        for (var host : grid.getMachines(IChestOrDrive.class)) {
            if (host instanceof AEBaseInvBlockEntity invHost) {
                collectPatternsFromInventory(invHost.getInternalInventory(), list);
            }
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

    public static List<IPatternDetails> getPatternsForGrid(IGrid grid, Level level) {
        var patterns = getAllPatternsForGrid(grid);
        if (patterns.isEmpty()) return Collections.emptyList();

        var list = new ArrayList<IPatternDetails>(patterns.size());
        for (var patternStack : patterns) {
            var details = AEPatternDecoder.INSTANCE.decodePattern(AEItemKey.of(patternStack), level);
            if (details != null) list.add(details);
        }
        return list;
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