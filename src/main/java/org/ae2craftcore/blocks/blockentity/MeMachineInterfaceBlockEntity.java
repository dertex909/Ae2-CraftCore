package org.ae2craftcore.blocks.blockentity;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.api.inventories.InternalInventory;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.helpers.IPriorityHost;
import appeng.menu.ISubMenu;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.me.helpers.MachineSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.ae2craftcore.blocks.block.MeMachineInterfaceBlock;
import org.ae2craftcore.blocks.menu.MeMachineInterfaceMenu;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.ae2craftcore.services.IRecipeCacheService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static appeng.api.config.Actionable.MODULATE;
import static appeng.menu.MenuOpener.returnTo;
import static net.minecraft.core.component.DataComponents.CUSTOM_DATA;
import static org.ae2craftcore.network.packet.RecipeTerminalSavePacket.RECIPEMACHINEGROUP;
import static org.ae2craftcore.registry.ModMenuTypes.ME_MACHINE_INTERFACE;

@RegisterBlockEntity(name = "me_machine_interface", blocks = {MeMachineInterfaceBlock.class})
public class MeMachineInterfaceBlockEntity extends AENetworkedPoweredBlockEntity implements ICraftingProvider, MenuProvider, InternalInventoryHost, IPriorityHost, IConfigurableObject {
    public static BlockEntityType<MeMachineInterfaceBlockEntity> TYPE;

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 9);

    private Direction machineDirection = Direction.NORTH;
    private String customName = "Recipe";
    private int priority = 0;
    private final ConfigManager configManager = new ConfigManager(this::setChanged);

    public MeMachineInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        this.configManager.registerSetting(Settings.BLOCKING_MODE, YesNo.NO);
        this.configManager.registerSetting(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE);
        this.configManager.registerSetting(Settings.PATTERN_ACCESS_TERMINAL, YesNo.YES);
        this.getMainNode().addService(ICraftingProvider.class, this).setFlags(GridFlags.REQUIRE_CHANNEL).setIdlePowerUsage(100);
        this.setInternalMaxPower(1000);
        this.setPowerSides(getGridConnectableSides(getOrientation()));
    }

    public void updateAdjacentMachine() {
        if (this.level == null || this.level.isClientSide) return;

        if (!this.customName.equals("Recipe")) return;
        Direction foundDir = null;
        String foundName = "Recipe";

        for (var dir : Direction.values()) {
            var targetPos = this.worldPosition.relative(dir);
            if (!this.level.isLoaded(targetPos)) continue;
            var handler = this.level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, dir.getOpposite());
            if (handler != null) {
                foundDir = dir;
                var state = this.level.getBlockState(targetPos);
                foundName = state.getBlock().getName().getString();
                break;
            }
        }

        var nextDir = foundDir != null ? foundDir : Direction.NORTH;
        if (foundDir != null) {
            this.machineDirection = nextDir;
            this.customName = foundName;

            this.setChanged();
            var state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);

            this.onGridConnectableSidesChanged();
            this.setPowerSides(getGridConnectableSides(getOrientation()));
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        if (this.level == null || this.level.isClientSide) return List.of();
        this.updateAdjacentMachine();
        var grid = this.getMainNode().getGrid();
        if (grid == null) return List.of();

        var cacheService = grid.getService(IRecipeCacheService.class);
        if (cacheService == null) return List.of();

        var allPatterns = cacheService.getCachedPatterns(this.level);
        var filtered = new ArrayList<IPatternDetails>();

        for (var pattern : allPatterns) {
            var definition = pattern.getDefinition();
            boolean matchesGroup = true;
            if (definition != null) {
                var stack = definition.toStack();
                var customData = stack.get(CUSTOM_DATA);
                if (customData != null) {
                    var tag = customData.copyTag();
                    if (tag.contains(RECIPEMACHINEGROUP)) {
                        String patternGroup = tag.getString(RECIPEMACHINEGROUP);
                        matchesGroup = patternGroup.equalsIgnoreCase(this.getInterfaceName());
                    }
                }
            }
            if (matchesGroup) filtered.add(pattern);
        }
        return filtered;
    }

    public boolean isBlocking() {
        return this.configManager.getSetting(Settings.BLOCKING_MODE) == YesNo.YES;
    }

    public LockCraftingMode getLockCraftingMode() {
        return this.configManager.getSetting(Settings.LOCK_CRAFTING_MODE);
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (this.level == null || this.level.isClientSide) return false;

        var lockMode = getLockCraftingMode();
        if (lockMode != LockCraftingMode.NONE) {
            boolean hasSignal = this.level.hasNeighborSignal(this.worldPosition);
            if (lockMode == LockCraftingMode.LOCK_WHILE_HIGH && hasSignal) return false;
            if (lockMode == LockCraftingMode.LOCK_WHILE_LOW && !hasSignal) return false;
            if (lockMode == LockCraftingMode.LOCK_UNTIL_PULSE && !hasSignal) return false;
        }

        for (var dir : Direction.values()) {
            var targetPos = this.worldPosition.relative(dir);
            if (!this.level.isLoaded(targetPos)) continue;
            var side = dir.getOpposite();

            var craftingMachine = ICraftingMachine.of(this.level, targetPos, side);
            if (craftingMachine != null && craftingMachine.acceptsPlans()) {
                if (craftingMachine.pushPattern(patternDetails, inputHolder, side)) return true;
                continue;
            }

            var itemHandler = this.level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, side);
            var fluidHandler = this.level.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, side);

            if (itemHandler == null && fluidHandler == null) continue;

            if (this.isBlocking() && itemHandler != null) {
                boolean hasItems = false;
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    if (!itemHandler.getStackInSlot(i).isEmpty()) {
                        hasItems = true;
                        break;
                    }
                }
                if (hasItems) continue;
            }

            ItemStack[] simulatedSlots = null;
            if (itemHandler != null) {
                simulatedSlots = new ItemStack[itemHandler.getSlots()];
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    simulatedSlots[i] = itemHandler.getStackInSlot(i).copy();
                }
            }

            var simulatedFluids = new ArrayList<FluidStack>();
            boolean canPush = true;

            for (var counter : inputHolder) {
                for (var key : counter.keySet()) {
                    long amount = counter.get(key);
                    if (key instanceof AEItemKey itemKey) {
                        if (itemHandler == null) {
                            canPush = false;
                            break;
                        }
                        var stackToInsert = itemKey.toStack((int) amount);
                        if (!simulateInsertionInArray(simulatedSlots, stackToInsert, itemHandler)) {
                            canPush = false;
                            break;
                        }
                    } else if (key instanceof AEFluidKey fluidKey) {
                        if (fluidHandler == null) {
                            canPush = false;
                            break;
                        }
                        var fluidStack = fluidKey.toStack((int) amount);

                        int alreadySimulated = 0;
                        for (var f : simulatedFluids) {
                            if (f.getFluid() == fluidStack.getFluid()) alreadySimulated += f.getAmount();
                        }

                        var testStack = new FluidStack(fluidStack.getFluid(), fluidStack.getAmount() + alreadySimulated);
                        int inserted = fluidHandler.fill(testStack, IFluidHandler.FluidAction.SIMULATE);
                        if (inserted < testStack.getAmount()) {
                            canPush = false;
                            break;
                        }
                        simulatedFluids.add(fluidStack);
                    }
                }
                if (!canPush) break;
            }

            if (canPush) {
                for (var counter : inputHolder) {
                    for (var key : counter.keySet()) {
                        long amount = counter.get(key);
                        if (key instanceof AEItemKey itemKey && itemHandler != null) {
                            var stack = itemKey.toStack((int) amount);
                            ItemHandlerHelper.insertItemStacked(itemHandler, stack, false);
                        } else if (key instanceof AEFluidKey fluidKey && fluidHandler != null) {
                            var fluidStack = fluidKey.toStack((int) amount);
                            fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                        }
                    }
                }
                return true;
            }
        }

        return false;
    }

    private boolean simulateInsertionInArray(ItemStack[] slots, ItemStack stack, IItemHandler handler) {
        var remaining = stack.copy();

        for (int i = 0; i < slots.length; i++) {
            var existing = slots[i];
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, remaining) && handler.isItemValid(i, remaining)) {
                int maxStackSize = Math.min(existing.getMaxStackSize(), handler.getSlotLimit(i));
                int space = maxStackSize - existing.getCount();
                if (space > 0) {
                    int toAdd = Math.min(space, remaining.getCount());
                    existing.grow(toAdd);
                    remaining.shrink(toAdd);
                    if (remaining.isEmpty()) return true;
                }
            }
        }

        for (int i = 0; i < slots.length; i++) {
            var existing = slots[i];
            if (existing.isEmpty() && handler.isItemValid(i, remaining)) {
                int maxStackSize = Math.min(remaining.getMaxStackSize(), handler.getSlotLimit(i));
                if (remaining.getCount() <= maxStackSize) {
                    slots[i] = remaining.copy();
                    return true;
                } else {
                    slots[i] = remaining.copyWithCount(maxStackSize);
                    remaining.shrink(maxStackSize);
                }
            }
        }

        return remaining.isEmpty();
    }

    @Override
    public boolean isClientSide() {
        return this.level == null || this.level.isClientSide();
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.setChanged();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (this.level == null || this.level.isClientSide) return;

        var stack = inv.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            var grid = this.getMainNode().getGrid();
            if (grid != null) {
                var storage = grid.getStorageService().getInventory();
                var actionSource = new MachineSource(this.getMainNode()::getNode);
                var key = AEItemKey.of(stack);

                if (key != null) {
                    long inserted = storage.insert(key, stack.getCount(), MODULATE, actionSource);

                    if (inserted >= stack.getCount()) {
                        inv.setItemDirect(slot, ItemStack.EMPTY);
                    } else if (inserted > 0) {
                        stack.shrink((int) inserted);
                        inv.setItemDirect(slot, stack);
                    }
                }
            }
        }
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

    public Direction getMachineDirection() {
        return this.machineDirection;
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
        return new MeMachineInterfaceMenu(containerId, playerInventory, this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        var tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        if (this.level != null && !this.level.isClientSide) ICraftingProvider.requestUpdate(this.getMainNode());
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

    private static final String MACHINEDIRECTION = "MD";
    private static final String CUSTOMNAME = "CN";
    private static final String PRIORITY_KEY = "priority";

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(MACHINEDIRECTION, this.machineDirection.ordinal());
        tag.putString(CUSTOMNAME, this.customName);
        tag.putInt(PRIORITY_KEY, this.priority);
        this.configManager.writeToNBT(tag, registries);
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        this.machineDirection = Direction.values()[tag.getInt(MACHINEDIRECTION)];
        this.customName = tag.getString(CUSTOMNAME);
        if (tag.contains(PRIORITY_KEY)) this.priority = tag.getInt(PRIORITY_KEY);
        this.configManager.readFromNBT(tag, registries);
        this.setPowerSides(getGridConnectableSides(getOrientation()));
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(int newValue) {
        this.priority = newValue;
        this.setChanged();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.configManager;
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        returnTo(ME_MACHINE_INTERFACE.get(), player, subMenu.getLocator());
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(this.getItemFromBlockEntity());
    }
}