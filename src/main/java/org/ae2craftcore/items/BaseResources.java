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

@RegisterItem(name = "recipe_cell_component_1k")
@RegisterItem(name = "recipe_cell_component_4k")
@RegisterItem(name = "recipe_cell_component_16k")
@RegisterItem(name = "recipe_cell_component_64k")
@RegisterItem(name = "recipe_cell_component_256k")
@RegisterItem(name = "recipe_cell_housing")
public class BaseResources extends Item {

    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_1;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_2;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_3;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_4;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_5;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_6;
    public static DeferredHolder<Item, BaseResources> QUANTUM_SCRAP;

    public static DeferredHolder<Item, BaseResources> RECIPE_CELL_COMPONENT_1K;
    public static DeferredHolder<Item, BaseResources> RECIPE_CELL_COMPONENT_4K;
    public static DeferredHolder<Item, BaseResources> RECIPE_CELL_COMPONENT_16K;
    public static DeferredHolder<Item, BaseResources> RECIPE_CELL_COMPONENT_64K;
    public static DeferredHolder<Item, BaseResources> RECIPE_CELL_COMPONENT_256K;
    public static DeferredHolder<Item, BaseResources> RECIPE_CELL_HOUSING;

    public BaseResources(Properties properties) {
        super(properties);
    }
}