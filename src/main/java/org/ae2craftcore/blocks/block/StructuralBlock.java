package org.ae2craftcore.blocks.block;

import net.minecraft.world.level.block.Block;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

@RegisterBlock(name = "structural_block", strength = 3.0f, resistance = 3.0f, sound = "stone", requiresCorrectTool = true)
public class StructuralBlock extends Block {
    public static DeferredHolder<Block, StructuralBlock> HOLDER;

    public StructuralBlock(Properties properties) {
        super(properties);
    }
}