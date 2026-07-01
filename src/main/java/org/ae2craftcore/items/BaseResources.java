package org.ae2craftcore.items;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.annotations.RegisterItem;

@RegisterItem(name = "quantum_processor_1")
@RegisterItem(name = "quantum_processor_2")
@RegisterItem(name = "quantum_processor_3")
@RegisterItem(name = "quantum_processor_4")
@RegisterItem(name = "quantum_processor_5")
@RegisterItem(name = "quantum_processor_6")
@RegisterItem(name = "quantum_scrap")
public class BaseResources extends Item {

    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_1;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_2;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_3;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_4;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_5;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_6;
    public static DeferredHolder<Item, BaseResources> QUANTUM_SCRAP;

    public BaseResources(Properties properties) {
        super(properties);
    }
}