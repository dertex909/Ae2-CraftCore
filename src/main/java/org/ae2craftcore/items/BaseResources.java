package org.ae2craftcore.items;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.annotations.RegisterItem;

@RegisterItem(name = "obsidian_plate")
@RegisterItem(name = "quartz_plate")
@RegisterItem(name = "end_stone_plate")

@RegisterItem(name = "quantum_processor_1")
@RegisterItem(name = "quantum_processor_2")
@RegisterItem(name = "quantum_processor_3")
@RegisterItem(name = "quantum_processor_4")
@RegisterItem(name = "quantum_processor_5")
@RegisterItem(name = "quantum_processor_6")
@RegisterItem(name = "quantum_scrap")

@RegisterItem(name = "quantum_processor_press", stacksTo = 1)

@RegisterItem(name = "luck_card_1", stacksTo = 16)
@RegisterItem(name = "luck_card_2", stacksTo = 16)
@RegisterItem(name = "luck_card_3", stacksTo = 16)
@RegisterItem(name = "luck_card_4", stacksTo = 16)
public class BaseResources extends Item {

    public static DeferredHolder<Item, BaseResources> OBSIDIAN_PLATE;
    public static DeferredHolder<Item, BaseResources> QUARTZ_PLATE;
    public static DeferredHolder<Item, BaseResources> END_STONE_PLATE;

    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_1;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_2;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_3;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_4;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_5;
    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_6;
    public static DeferredHolder<Item, BaseResources> QUANTUM_SCRAP;

    public static DeferredHolder<Item, BaseResources> QUANTUM_PROCESSOR_PRESS;

    public static DeferredHolder<Item, BaseResources> LUCK_CARD_1;
    public static DeferredHolder<Item, BaseResources> LUCK_CARD_2;
    public static DeferredHolder<Item, BaseResources> LUCK_CARD_3;
    public static DeferredHolder<Item, BaseResources> LUCK_CARD_4;

    public BaseResources(Properties properties) {
        super(properties);
    }
}