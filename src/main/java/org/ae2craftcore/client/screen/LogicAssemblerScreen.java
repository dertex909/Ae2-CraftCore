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

package org.ae2craftcore.client.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.StyleManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.LogicAssemblerMenu;

public class LogicAssemblerScreen extends AEBaseScreen<LogicAssemblerMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Ae2craftcore.MODID, "textures/gui/container/logic_assembler.png");

    public LogicAssemblerScreen(LogicAssemblerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/logic_assembler.json"));
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) >> 1;
        this.inventoryLabelY = 83;
    }

    @Override
    public void drawFG(GuiGraphicsExtractor graphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(graphics, offsetX, offsetY, mouseX, mouseY);
        int chance = this.menu.getCraftingChance();
        if (chance >= 0) {
            var text = Component.literal(chance + "%");
            int textWidth = this.font.width(text);
            graphics.text(this.font, text, 122 - (textWidth >> 1), 26, 0xFF000000, false);
        }
    }

    @Override
    public void drawBG(GuiGraphicsExtractor graphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(graphics, offsetX, offsetY, mouseX, mouseY, partialTicks);
        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();
        if (progress > 0 && maxProgress > 0) {
            int h = (progress * 18) / maxProgress;
            int offset = 18 - h;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 135, this.topPos + 39 + offset, 197.0f, (float) offset, 6, h, 256, 256);
        }
    }
}