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
