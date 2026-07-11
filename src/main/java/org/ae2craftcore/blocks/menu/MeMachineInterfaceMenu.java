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

package org.ae2craftcore.blocks.menu;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.ae2craftcore.blocks.blockentity.MeMachineInterfaceBlockEntity;
import org.jetbrains.annotations.NotNull;

import static org.ae2craftcore.registry.ModMenuTypes.ME_MACHINE_INTERFACE;

public class MeMachineInterfaceMenu extends AEBaseMenu {
    private final MeMachineInterfaceBlockEntity blockEntity;
    private final BlockPos blockPos;

    @GuiSync(1)
    public YesNo blockingMode = YesNo.NO;
    @GuiSync(2)
    public LockCraftingMode lockCraftingMode = LockCraftingMode.NONE;
    @GuiSync(3)
    public LockCraftingMode craftingLockedReason = LockCraftingMode.NONE;

    public MeMachineInterfaceMenu(int containerId, Inventory playerInventory, MeMachineInterfaceBlockEntity blockEntity) {
        super(ME_MACHINE_INTERFACE.get(), containerId, playerInventory, blockEntity);
        this.blockEntity = blockEntity;
        this.blockPos = blockEntity != null ? blockEntity.getBlockPos() : BlockPos.ZERO;
    }

    public MeMachineInterfaceBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public YesNo getBlockingMode() {
        return this.blockingMode;
    }

    public LockCraftingMode getLockCraftingMode() {
        return this.lockCraftingMode;
    }

    public LockCraftingMode getCraftingLockedReason() {
        return this.craftingLockedReason;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.blockEntity == null) return false;
        return player.level().getBlockEntity(this.blockPos) == this.blockEntity;
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            var host = getBlockEntity();
            if (host != null) {
                blockingMode = host.getConfigManager().getSetting(Settings.BLOCKING_MODE);
                lockCraftingMode = host.getConfigManager().getSetting(Settings.LOCK_CRAFTING_MODE);
                craftingLockedReason = host.getCraftingLockedReason();
            }
        }
        super.broadcastChanges();
    }
}