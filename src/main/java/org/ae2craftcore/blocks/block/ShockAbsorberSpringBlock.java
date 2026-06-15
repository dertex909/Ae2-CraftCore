package org.ae2craftcore.blocks.block;

import net.minecraft.world.level.block.Block;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

@RegisterBlock(name = "shock_absorbing_spring", strength = 6.0f, resistance = 0.5f, requiresCorrectTool = true)
public class ShockAbsorberSpringBlock extends Block {
    public static DeferredHolder<Block, ShockAbsorberSpringBlock> HOLDER;

    public ShockAbsorberSpringBlock(Properties properties) {
        super(properties);
    }
}