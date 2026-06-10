package org.ae2craftcore.items;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.annotations.RegisterItem;

@RegisterItem(name = "quantum_processor")
public class QuantumProcessor extends Item {

    public static DeferredHolder<Item, QuantumProcessor> QUANTUM_PROCESSOR;

    public QuantumProcessor(Properties properties) {
        super(properties);
    }
}