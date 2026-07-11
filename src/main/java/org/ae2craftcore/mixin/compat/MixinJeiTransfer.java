/*
 * Ae2 CraftCore
 * Copyright (C) 2025-2026 dertex909
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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