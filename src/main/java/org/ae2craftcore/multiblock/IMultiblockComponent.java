package org.ae2craftcore.multiblock;

import org.ae2craftcore.blocks.blockentity.CryostatBlockEntity;

public interface IMultiblockComponent {
    void updateMultiblockState(CryostatBlockEntity core, boolean isValid);
}