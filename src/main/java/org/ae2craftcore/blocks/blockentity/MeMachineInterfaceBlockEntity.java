package org.ae2craftcore.blocks.blockentity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridFlags;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.ae2craftcore.blocks.block.MeMachineInterfaceBlock;
import org.ae2craftcore.blocks.menu.MeMachineInterfaceMenu;
import org.ae2craftcore.items.RecipeStorageCellItem;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@RegisterBlockEntity(name = "me_machine_interface", blocks = {MeMachineInterfaceBlock.class})
public class MeMachineInterfaceBlockEntity extends AENetworkedPoweredBlockEntity implements ICraftingProvider, MenuProvider {
    public static BlockEntityType<MeMachineInterfaceBlockEntity> TYPE;

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 0);

    private Direction machineDirection = Direction.NORTH;
    private String customName = "Recipe";

    public MeMachineInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        this.getMainNode().addService(ICraftingProvider.class, this).setFlags(GridFlags.REQUIRE_CHANNEL).setIdlePowerUsage(100);
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

        var oldDir = this.machineDirection;
        this.machineDirection = foundDir != null ? foundDir : Direction.NORTH;
        this.customName = foundName;

        this.setChanged();
        var state = this.getBlockState();
        this.level.sendBlockUpdated(this.worldPosition, state, state, 3);

        if (oldDir != this.machineDirection) {
            this.onGridConnectableSidesChanged();
            this.setPowerSides(getGridConnectableSides(getOrientation()));
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        if (this.level == null || this.level.isClientSide) return List.of();
        var grid = this.getMainNode().getGrid();
        if (grid == null) return List.of();

        var allPatterns = RecipeStorageCellItem.getPatternsForGrid(grid, this.level);
        var filtered = new ArrayList<IPatternDetails>();

        for (var pattern : allPatterns) {
            var definition = pattern.getDefinition();
            boolean matchesGroup = true;
            if (definition != null) {
                var stack = definition.toStack();
                var customData = stack.get(DataComponents.CUSTOM_DATA);
                if (customData != null) {
                    var tag = customData.copyTag();
                    if (tag.contains("RecipeMachineGroup")) {
                        String patternGroup = tag.getString("RecipeMachineGroup");
                        matchesGroup = patternGroup.equalsIgnoreCase(this.getInterfaceName());
                    }
                }
            }
            if (matchesGroup) filtered.add(pattern);
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
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.ae2craftcore.me_machine_interface");
    }

    public String getInterfaceName() {
        return this.customName;
    }

    public void setCustomName(String name) {
        this.customName = name;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            var state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
            ICraftingProvider.requestUpdate(this.getMainNode());
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new MeMachineInterfaceMenu(containerId, this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        var tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        return tag;
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
        this.setPowerSides(getGridConnectableSides(getOrientation()));
    }
}