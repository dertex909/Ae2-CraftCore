/*
 * Ae2 CraftCore
 * Copyright (C) 2026 dertex909
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.ae2craftcore.blocks.blockentity;

import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.orientation.BlockOrientation;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.helpers.IPriorityHost;
import appeng.helpers.InterfaceLogicHost;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.me.helpers.MachineSource;
import appeng.menu.ISubMenu;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
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
import org.ae2craftcore.blocks.block.MeMachineInterfaceBlock;
import org.ae2craftcore.blocks.menu.MeMachineInterfaceMenu;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.ae2craftcore.services.IRecipeCacheService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static appeng.helpers.patternprovider.PatternProviderTarget.get;
import static appeng.menu.MenuOpener.returnTo;
import static net.minecraft.core.component.DataComponents.CUSTOM_DATA;
import static org.ae2craftcore.network.packet.RecipeTerminalSavePacket.RECIPEMACHINEGROUP;
import static org.ae2craftcore.registry.ModMenuTypes.ME_MACHINE_INTERFACE;

@RegisterBlockEntity(name = "me_machine_interface", blocks = {MeMachineInterfaceBlock.class})
public class MeMachineInterfaceBlockEntity extends AENetworkedPoweredBlockEntity implements ICraftingProvider, MenuProvider, InternalInventoryHost, IPriorityHost, IConfigurableObject {
    private static final String MACHINEDIRECTION = "MD";
    private static final String CUSTOMNAME = "CN";
    private static final String PRIORITY_KEY = "PRT";
    public static BlockEntityType<MeMachineInterfaceBlockEntity> TYPE;
    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 9);
    private final ConfigManager configManager = new ConfigManager(this::setChanged);
    private Direction machineDirection = Direction.NORTH;
    private String customName = "Unknown recipe";
    private int priority = 0;

    public MeMachineInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        this.configManager.registerSetting(Settings.BLOCKING_MODE, YesNo.NO);
        this.configManager.registerSetting(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE);
        this.configManager.registerSetting(Settings.PATTERN_ACCESS_TERMINAL, YesNo.YES);
        this.getMainNode().addService(ICraftingProvider.class, this).setFlags(GridFlags.REQUIRE_CHANNEL).setIdlePowerUsage(1000);
        this.setInternalMaxPower(10000);
        this.setPowerSides(getGridConnectableSides(getOrientation()));
    }

    public void updateAdjacentMachine() {
        if (this.level == null || this.isClientSide()) return;

        if (!this.customName.equals("Unknown recipe")) return;
        Direction foundDir = null;
        String foundName = "Unknown recipe";

        for (var dir : Direction.values()) {
            var targetPos = this.worldPosition.relative(dir);
            if (isValidMachine(targetPos, dir.getOpposite())) {
                foundDir = dir;
                foundName = this.level.getBlockState(targetPos).getBlock().getName().getString();
                break;
            }
        }

        if (foundDir != null) {
            this.machineDirection = foundDir;
            this.customName = foundName;

            this.syncBlock();

            this.onGridConnectableSidesChanged();
            this.setPowerSides(getGridConnectableSides(getOrientation()));
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        if (this.level == null || this.isClientSide()) return List.of();
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

    public LockCraftingMode getCraftingLockedReason() {
        if (this.level == null) return LockCraftingMode.NONE;
        var lockMode = getLockCraftingMode();
        if (lockMode != LockCraftingMode.NONE) {
            boolean hasSignal = this.level.hasNeighborSignal(this.worldPosition);
            if (lockMode == LockCraftingMode.LOCK_WHILE_HIGH && hasSignal) return LockCraftingMode.LOCK_WHILE_HIGH;
            if (lockMode == LockCraftingMode.LOCK_WHILE_LOW && !hasSignal) return LockCraftingMode.LOCK_WHILE_LOW;
            if (lockMode == LockCraftingMode.LOCK_UNTIL_PULSE && !hasSignal) return LockCraftingMode.LOCK_UNTIL_PULSE;
        }
        return LockCraftingMode.NONE;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (this.level == null || this.isClientSide()) return false;

        if (getCraftingLockedReason() != LockCraftingMode.NONE) return false;

        var source = new MachineSource(this);

        for (var dir : Direction.values()) {
            var targetPos = this.worldPosition.relative(dir);
            var side = dir.getOpposite();

            if (!isValidMachine(targetPos, side)) continue;

            var craftingMachine = ICraftingMachine.of(this.level, targetPos, side);
            if (craftingMachine != null && craftingMachine.acceptsPlans()) {
                if (craftingMachine.pushPattern(patternDetails, inputHolder, side)) return true;
                continue;
            }

            var target = get(this.level, targetPos, this.level.getBlockEntity(targetPos), side, source);
            if (target == null) continue;

            if (this.isBlocking()) {
                var patternInputs = new HashSet<AEKey>();
                for (var i : patternDetails.getInputs()) {
                    for (var possible : i.getPossibleInputs()) patternInputs.add(possible.what().dropSecondary());
                }
                if (target.containsPatternInput(patternInputs)) continue;
            }

            boolean canInsertAll = true;
            for (var inputList : inputHolder) {
                for (var i : inputList) {
                    long inserted = target.insert(i.getKey(), i.getLongValue(), Actionable.SIMULATE);
                    if (inserted < i.getLongValue()) {
                        canInsertAll = false;
                        break;
                    }
                }
                if (!canInsertAll) break;
            }

            if (canInsertAll) {
                for (var inputList : inputHolder) {
                    for (var i : inputList) target.insert(i.getKey(), i.getLongValue(), Actionable.MODULATE);
                }
                return true;
            }
        }

        return false;
    }

    private boolean isValidMachine(BlockPos pos, Direction side) {
        if (this.level == null || this.isClientSide() || !this.level.isLoaded(pos)) return false;
        var state = this.level.getBlockState(pos);
        if (state.isAir() || state.getBlock() instanceof MeMachineInterfaceBlock) return false;

        var be = this.level.getBlockEntity(pos);
        if (be != null) {
            if (be instanceof MeMachineInterfaceBlockEntity || be instanceof PatternProviderLogicHost
                    || be instanceof InterfaceLogicHost) return false;
            if (be instanceof IPartHost partHost) {
                var part = partHost.getPart(side);
                if (part instanceof PatternProviderLogicHost || part instanceof InterfaceLogicHost) return false;
            }
        }

        if (ICraftingMachine.of(this.level, pos, side) != null) return true;

        if (be != null) {
            if (this.level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, be, side) != null) return true;
            return this.level.getCapability(Capabilities.FluidHandler.BLOCK, pos, state, be, side) != null;
        }
        return false;
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
        if (this.level == null || this.isClientSide()) return;

        var stack = inv.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            var grid = this.getMainNode().getGrid();
            if (grid != null) {
                var storage = grid.getStorageService().getInventory();
                var actionSource = new MachineSource(this.getMainNode()::getNode);
                var key = AEItemKey.of(stack);

                if (key != null) {
                    long inserted = storage.insert(key, stack.getCount(), Actionable.MODULATE, actionSource);

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

    private void syncBlock() {
        this.setChanged();
        if (this.level != null) {
            var state = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    public void setCustomName(String name) {
        this.customName = name;
        this.syncBlock();
        if (!this.isClientSide()) ICraftingProvider.requestUpdate(this.getMainNode());
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
        if (!this.isClientSide()) ICraftingProvider.requestUpdate(this.getMainNode());
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