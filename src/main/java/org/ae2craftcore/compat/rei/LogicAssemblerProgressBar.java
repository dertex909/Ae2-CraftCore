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
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.resources.Identifier;

import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;
import static org.ae2craftcore.compat.CompatUtil.PROGRESS_DURATION_MS;

public record LogicAssemblerProgressBar(Identifier location, int x, int y, int width, int height, int u, int v)
        implements Renderer {

    @Override
    public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        int progress = (int) (System.currentTimeMillis() % PROGRESS_DURATION_MS);
        int h = (progress * height) / PROGRESS_DURATION_MS;
        int offset = height - h;
        graphics.blit(GUI_TEXTURED, location, x, y + offset, (float) u, (float) offset, width, h, 256, 256);
    }
}
