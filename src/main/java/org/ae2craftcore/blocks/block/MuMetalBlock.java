package org.ae2craftcore.blocks.block;

import net.minecraft.world.level.block.Block;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

@RegisterBlock(name = "mu_metal", strength = 2.5f, resistance = 6.0f, sound = "chain", noOcclusion = true, requiresCorrectTool = true)
public class MuMetalBlock extends Block {
    public static DeferredHolder<Block, MuMetalBlock> HOLDER;

    public MuMetalBlock(Properties properties) {
        super(properties);
    }
}