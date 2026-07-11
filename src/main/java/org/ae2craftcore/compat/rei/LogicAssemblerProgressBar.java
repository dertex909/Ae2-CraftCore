package org.ae2craftcore.compat.rei;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;

public record LogicAssemblerProgressBar(ResourceLocation location, int x, int y, int width, int height, int u, int v) implements Renderer {
    private static final int ANIMATION_TIME = 2000;

    @Override
    public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        int progress = (int) (System.currentTimeMillis() % ANIMATION_TIME);
        int h = (progress * height) / ANIMATION_TIME;
        int offset = height - h;
        graphics.blit(location, x, y + offset, u, offset, width, h);
    }
}
