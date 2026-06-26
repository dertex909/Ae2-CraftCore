package org.ae2craftcore.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor {
    @Accessor("x")
    void ae2craftcore$setX(int x);

    @Accessor("y")
    void ae2craftcore$setY(int y);
}