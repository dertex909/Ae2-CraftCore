package org.ae2craftcore.blocks.block;

import net.minecraft.world.level.block.Block;
import org.ae2craftcore.registry.annotations.RegisterBlock;

@RegisterBlock(
    name = "helium_4",
    strength = 3.0f,
    resistance = 3.0f,
    sound = "stone",
    requiresCorrectTool = true
)
public class Helium4Block extends Block {
    public Helium4Block(Properties properties) {
        super(properties);
    }
}