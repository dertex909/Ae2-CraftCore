package org.ae2craftcore.blocks.blockentity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.ae2craftcore.blocks.block.MeMachineInterfaceBlock;
import org.ae2craftcore.items.RecipeStorageCellItem;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@RegisterBlockEntity(name = "me_machine_interface", blocks = {MeMachineInterfaceBlock.class})
public class MeMachineInterfaceBlockEntity extends AENetworkedPoweredBlockEntity implements ICraftingProvider {
    public static BlockEntityType<MeMachineInterfaceBlockEntity> TYPE;

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 0);

    private Direction machineDirection = Direction.NORTH;
    private String customName = "Recipe";

    public MeMachineInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        this.getMainNode().setFlags().setIdlePowerUsage(10);
        this.setInternalMaxPower(1000);
        this.setPowerSides(getGridConnectableSides(getOrientation()));
    }

    public void updateAdjacentMachine() {
        if (this.level == null || this.level.isClientSide) return;

        Direction foundDir = null;
        String foundName = "Recipe";

        for (var dir : Direction.values()) {
            var targetPos = this.worldPosition.relative(dir);
            var handler = this.level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, dir.getOpposite());
            if (handler != null) {
                foundDir = dir;
                var state = this.level.getBlockState(targetPos);
                foundName = state.getBlock().getName().getString() + " recipe";
                break;
            }
        }

        this.machineDirection = foundDir != null ? foundDir : Direction.NORTH;
        this.customName = foundName;

        this.setChanged();
        var state = this.getBlockState();
        this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
    }

    private boolean canMachineProcessPattern(BlockState machineState, IPatternDetails pattern, Level level) {
        if (level == null || machineState == null || pattern == null || this.machineDirection == null) return false;

        var targetPos = this.worldPosition.relative(this.machineDirection);
        var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, this.machineDirection.getOpposite());
        if (handler == null) return false;

        var inputs = pattern.getInputs();
        if (inputs.length == 0) return false;

        var primaryInput = inputs[0];
        var possible = primaryInput.getPossibleInputs();
        if (possible.length == 0) return false;

        var inputStack = possible[0];
        if (inputStack.what() instanceof AEItemKey itemKey) {
            var testStack = itemKey.toStack((int) inputStack.amount());
            for (int i = 0; i < handler.getSlots(); i++) if (handler.isItemValid(i, testStack)) return true;
        }
        return false;
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        if (this.level == null || this.level.isClientSide) return List.of();
        var grid = this.getMainNode().getGrid();
        if (grid == null) return List.of();

        var allPatterns = RecipeStorageCellItem.getPatternsForGrid(grid, this.level);
        var filtered = new ArrayList<IPatternDetails>();
        var machinePos = this.worldPosition.relative(this.machineDirection);
        var machineState = this.level.getBlockState(machinePos);
        if (!machineState.isAir()) for (var pattern : allPatterns) {
            if (this.canMachineProcessPattern(machineState, pattern, this.level)) filtered.add(pattern);
        }
        return filtered;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (this.level == null || this.machineDirection == null) return false;

        var targetPos = this.worldPosition.relative(this.machineDirection);
        var handler = this.level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, this.machineDirection.getOpposite());
        if (handler == null) return false;

        for (var counter : inputHolder) {
            for (var key : counter.keySet()) {
                long amount = counter.get(key);
                if (key instanceof AEItemKey itemKey) {
                    var stack = itemKey.toStack((int) amount);
                    var remaining = ItemHandlerHelper.insertItemStacked(handler, stack, true);
                    if (!remaining.isEmpty()) return false;
                }
            }
        }

        for (var counter : inputHolder) {
            for (var key : counter.keySet()) {
                long amount = counter.get(key);
                if (key instanceof AEItemKey itemKey) {
                    var stack = itemKey.toStack((int) amount);
                    ItemHandlerHelper.insertItemStacked(handler, stack, false);
                }
            }
        }
        return true;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.complementOf(EnumSet.of(this.machineDirection));
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        if (dir == this.machineDirection) return AECableType.NONE;
        return AECableType.COVERED;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal(this.customName);
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        onGridConnectableSidesChanged();
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return this.getBlockState().getBlock().asItem();
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("MachineDirection", this.machineDirection.ordinal());
        tag.putString("CustomName", this.customName);
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        this.machineDirection = Direction.values()[tag.getInt("MachineDirection")];
        this.customName = tag.getString("CustomName");
    }
}