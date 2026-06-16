package org.ae2craftcore.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.ae2craftcore.blocks.menu.CryostatMenu;
import org.jetbrains.annotations.NotNull;

public class CryostatScreen extends AbstractContainerScreen<CryostatMenu> {

    public CryostatScreen(CryostatMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 150;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) >> 1;
        this.inventoryLabelY = 57;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, 5, 0xDDDDDD, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x888888, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF14171A);
        guiGraphics.fill(x, y, x + this.imageWidth, y + 1, 0xFF444D56);
        guiGraphics.fill(x, y, x + 1, y + this.imageHeight, 0xFF444D56);
        guiGraphics.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, 0xFF444D56);
        guiGraphics.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, 0xFF444D56);
        guiGraphics.fill(x + 5, y + 56, x + this.imageWidth - 5, y + 57, 0xFF2A313C);

        drawLargeSlotOutline(guiGraphics, x + 78, y + 17);

        for (int i = 0; i < 3; i++) drawLargeSlotOutline(guiGraphics, x + 56 + i * 22, y + 39);

        int invX = x + 7;
        int invY = y + 67;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int px = invX + col * 18;
                int py = invY + row * 18;
                drawMiniSlotOutline(guiGraphics, px, py);
            }
        }

        int hotbarY = y + 125;
        for (int col = 0; col < 9; col++) {
            int px = invX + col * 18;
            drawMiniSlotOutline(guiGraphics, px, hotbarY);
        }
    }

    private void drawLargeSlotOutline(GuiGraphics guiGraphics, int px, int py) {
        guiGraphics.fill(px, py, px + 18, py + 18, 0xFF0E1012);
        guiGraphics.fill(px, py, px + 18, py + 1, 0xFF444D56);
        guiGraphics.fill(px, py, px + 1, py + 18, 0xFF444D56);
        guiGraphics.fill(px + 17, py, px + 18, py + 18, 0xFF444D56);
        guiGraphics.fill(px, py + 17, px + 18, py + 18, 0xFF444D56);
    }

    private void drawMiniSlotOutline(GuiGraphics guiGraphics, int px, int py) {
        guiGraphics.fill(px, py, px + 18, py + 18, 0xFF0E1012);
        guiGraphics.fill(px, py, px + 18, py + 1, 0xFF23282F);
        guiGraphics.fill(px, py, px + 1, py + 18, 0xFF23282F);
        guiGraphics.fill(px + 17, py, px + 18, py + 18, 0xFF23282F);
        guiGraphics.fill(px, py + 17, px + 18, py + 18, 0xFF23282F);
    }
}