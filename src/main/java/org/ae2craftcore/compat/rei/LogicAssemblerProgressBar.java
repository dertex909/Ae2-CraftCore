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

package org.ae2craftcore.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.ae2craftcore.compat.CompatUtil;

public record LogicAssemblerProgressBar(ResourceLocation location, int x, int y, int width, int height, int u, int v)
        implements Renderer {

    @Override
    public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        int progress = (int) (System.currentTimeMillis() % CompatUtil.PROGRESS_DURATION_MS);
        int h = (progress * height) / CompatUtil.PROGRESS_DURATION_MS;
        int offset = height - h;
        graphics.blit(location, x, y + offset, u, offset, width, h);
    }
}
