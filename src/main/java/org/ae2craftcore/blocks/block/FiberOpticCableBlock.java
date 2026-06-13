package org.ae2craftcore.blocks.block;

import net.minecraft.world.level.block.Block;
import org.ae2craftcore.registry.annotations.RegisterBlock;

@RegisterBlock(name = "fiber_optic_cable", resistance = 1.0f, noOcclusion = true)
public class FiberOpticCableBlock extends Block {
    public FiberOpticCableBlock(Properties properties) {
        super(properties);
    }
}