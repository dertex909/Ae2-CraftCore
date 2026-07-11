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

package org.ae2craftcore.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import org.ae2craftcore.Ae2craftcore;

public final class CompatUtil {
    private CompatUtil() {
    }

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Ae2craftcore.MODID, "textures/gui/container/logic_assembler.png");

    public static final int BG_U = 30;
    public static final int BG_V = 15;
    public static final int BG_WIDTH = 120;
    public static final int BG_HEIGHT = 62;

    public static final int PROGRESS_X = 105;
    public static final int PROGRESS_Y = 24;
    public static final int PROGRESS_WIDTH = 6;
    public static final int PROGRESS_HEIGHT = 18;
    public static final int PROGRESS_U = 197;
    public static final int PROGRESS_V = 0;
    public static final int PROGRESS_DURATION_MS = 2000;

    public static final int SLOT_IN_X = 9;
    public static final int SLOT_TOP_Y = 8;
    public static final int SLOT_BOTTOM_Y = 40;

    public static final int SLOT_OUT_X = 83;
    public static final int SLOT_OUT_Y = 25;

    public static final int CHANCE_X = 92;
    public static final int CHANCE_Y = 11;

    public static String formatChance(double chance) {
        return (int) (chance * 100) + "%";
    }
}
