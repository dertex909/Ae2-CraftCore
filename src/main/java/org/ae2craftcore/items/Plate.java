package org.ae2craftcore.items;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.annotations.RegisterItem;

@RegisterItem(name = "obsidian_plate")
@RegisterItem(name = "quartz_plate")
@RegisterItem(name = "end_stone_plate")
public class Plate extends Item {

    public static DeferredHolder<Item, Plate> OBSIDIAN_PLATE;
    public static DeferredHolder<Item, Plate> QUARTZ_PLATE;
    public static DeferredHolder<Item, Plate> END_STONE_PLATE;

    public Plate(Properties properties) {
        super(properties);
    }
}