package org.ae2craftcore.blocks.block;

import net.minecraft.world.level.block.Block;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

@RegisterBlock(name = "shielded_mesh", strength = 3.0f, resistance = 3.0f, requiresCorrectTool = true, noOcclusion = true)
public class ShieldedMeshBlock extends Block {
    public static DeferredHolder<Block, ShieldedMeshBlock> HOLDER;

    public ShieldedMeshBlock(Properties properties) {
        super(properties);
    }
}