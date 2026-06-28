package org.ae2craftcore.mixin;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.blockentity.storage.DriveBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.ae2craftcore.util.ICellContainerHost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = DriveBlockEntity.class, remap = false)
public abstract class DriveBlockEntityMixin implements ICellContainerHost {
    @Shadow
    public abstract InternalInventory getInternalInventory();

    @Override
    public IGrid ae2craftcore$getGrid() {
        if ((Object) this instanceof IActionHost actionHost) {
            var node = actionHost.getActionableNode();
            return node != null ? node.getGrid() : null;
        }
        return null;
    }

    @Override
    public boolean ae2craftcore$containsCell(ItemStack stack) {
        var inv = getInternalInventory();
        if (inv != null) for (int i = 0; i < inv.size(); i++) {
            if (ItemStack.isSameItemSameComponents(inv.getStackInSlot(i), stack)) return true;
        }
        return false;
    }
}