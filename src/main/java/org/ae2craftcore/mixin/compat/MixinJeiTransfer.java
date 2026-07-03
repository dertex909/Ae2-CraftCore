package org.ae2craftcore.mixin.compat;

import appeng.menu.me.items.PatternEncodingTermMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "mezz.jei.library.recipes.RecipeTransferManager", remap = false)
public class MixinJeiTransfer {
    @Redirect(method = "getRecipeTransferHandler", at = @At(value = "INVOKE", target = "Ljava/lang/Object;getClass()Ljava/lang/Class;"))
    private Class<?> redirectGetClass(Object container) {
        if (container instanceof PatternEncodingTermMenu) return PatternEncodingTermMenu.class;
        return container.getClass();
    }
}