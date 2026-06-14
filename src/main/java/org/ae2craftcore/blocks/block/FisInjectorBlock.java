package org.ae2craftcore.blocks.block;

import net.minecraft.world.level.block.Block;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

@RegisterBlock(name = "fis_injector", strength = 3.0f, resistance = 3.0f, requiresCorrectTool = true)
public class FisInjectorBlock extends Block {
    public static DeferredHolder<Block, FisInjectorBlock> HOLDER;

    public FisInjectorBlock(Properties properties) {
        super(properties);
    }
}