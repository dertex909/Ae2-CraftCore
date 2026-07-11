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

package org.ae2craftcore.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.LogicAssemblerMenu;
import org.jetbrains.annotations.NotNull;

public class LogicAssemblerScreen extends AbstractContainerScreen<LogicAssemblerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "textures/gui/container/logic_assembler.png");

    public LogicAssemblerScreen(LogicAssemblerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 197;
        this.imageHeight = 178;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) >> 1;
        this.inventoryLabelY = 83;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        int chance = this.menu.getCraftingChance();
        if (chance >= 0) {
            var text = Component.literal(chance + "%");
            int textWidth = this.font.width(text);
            guiGraphics.drawString(this.font, text, 122 - (textWidth >> 1), 26, 0x000000, false);
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();
        if (progress > 0 && maxProgress > 0) {
            int h = (progress * 18) / maxProgress;
            int offset = 18 - h;
            guiGraphics.blit(TEXTURE, x + 135, y + 39 + offset, 197, offset, 6, h);
        }
    }
}