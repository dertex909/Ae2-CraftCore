package org.ae2craftcore.items;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.annotations.RegisterItem;

@RegisterItem(name = "quantum_processor_press", stacksTo = 1)
public class QuantumProcessorPressItem extends Item {

    public static DeferredHolder<Item, QuantumProcessorPressItem> QUANTUM_PROCESSOR_PRESS;

    public QuantumProcessorPressItem(Properties properties) {
        super(properties);
    }
}