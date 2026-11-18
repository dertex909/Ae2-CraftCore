/*
 * Ae2 CraftCore
 * Copyright (C) 2026 dertex909
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

package org.ae2craftcore.mixin;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

public class Ae2CraftCoreMixinPlugin implements IMixinConfigPlugin {
    private boolean isExtendedAeLoaded = false;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            isExtendedAeLoaded = this.getClass().getClassLoader().getResource("com/glodblock/github/extendedae/common/tileentities/matrix/TileAssemblerMatrixPattern.class") != null;
        } catch (Exception e) {
            isExtendedAeLoaded = false;
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("extendedae")) return isExtendedAeLoaded;
        return true;
    }
}