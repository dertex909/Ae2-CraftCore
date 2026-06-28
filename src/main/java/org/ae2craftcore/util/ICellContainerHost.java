package org.ae2craftcore.util;

import appeng.api.networking.IGrid;
import net.minecraft.world.item.ItemStack;

public interface ICellContainerHost {
    IGrid ae2craftcore$getGrid();

    boolean ae2craftcore$containsCell(ItemStack stack);
}