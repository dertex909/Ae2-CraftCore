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

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.LogicAssemblerMenu;
import org.jetbrains.annotations.NotNull;

public class LogicAssemblerScreen extends AbstractContainerScreen<LogicAssemblerMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Ae2craftcore.MODID, "textures/gui/container/logic_assembler.png");

    public LogicAssemblerScreen(LogicAssemblerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 197, 178);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) >> 1;
        this.inventoryLabelY = 83;
    }

    @Override
    protected void extractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);

        int chance = this.menu.getCraftingChance();
        if (chance >= 0) {
            var text = Component.literal(chance + "%");
            int textWidth = this.font.width(text);
            graphics.text(this.font, text, 122 - (textWidth >> 1), 26, 0x000000, false);
        }
    }

    @Override
    public void extractContents(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);

        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();
        if (progress > 0 && maxProgress > 0) {
            int h = (progress * 18) / maxProgress;
            int offset = 18 - h;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 135, y + 39 + offset, 197.0f, (float) offset, 6, h, 256, 256);
        }

        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }
}