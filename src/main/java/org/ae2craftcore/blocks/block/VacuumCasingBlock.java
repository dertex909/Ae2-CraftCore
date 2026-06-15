package org.ae2craftcore.blocks.block;

import net.minecraft.world.level.block.Block;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

@RegisterBlock(name = "vacuum_casing", strength = 3.0f, resistance = 3.0f, requiresCorrectTool = true)
public class VacuumCasingBlock extends Block {
    public static DeferredHolder<Block, VacuumCasingBlock> HOLDER;

    public VacuumCasingBlock(Properties properties) {
        super(properties);
    }
}