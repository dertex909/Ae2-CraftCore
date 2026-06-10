package org.ae2craftcore.items;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.annotations.RegisterItem;

@RegisterItem(name = "quantum_processor_300nm")
@RegisterItem(name = "quantum_processor_250nm")
@RegisterItem(name = "quantum_processor_200nm")
@RegisterItem(name = "quantum_processor_150nm")
@RegisterItem(name = "quantum_processor_100nm")
@RegisterItem(name = "quantum_processor_50nm")
public class QuantumProcessor extends Item {

    public static DeferredHolder<Item, QuantumProcessor> QUANTUM_PROCESSOR_300NM;
    public static DeferredHolder<Item, QuantumProcessor> QUANTUM_PROCESSOR_250NM;
    public static DeferredHolder<Item, QuantumProcessor> QUANTUM_PROCESSOR_200NM;
    public static DeferredHolder<Item, QuantumProcessor> QUANTUM_PROCESSOR_150NM;
    public static DeferredHolder<Item, QuantumProcessor> QUANTUM_PROCESSOR_100NM;
    public static DeferredHolder<Item, QuantumProcessor> QUANTUM_PROCESSOR_50NM;

    public QuantumProcessor(Properties properties) {
        super(properties);
    }
}