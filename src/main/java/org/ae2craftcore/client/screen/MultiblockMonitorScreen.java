package org.ae2craftcore.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.ae2craftcore.blocks.menu.MultiblockMonitorMenu;
import org.jetbrains.annotations.NotNull;

public class MultiblockMonitorScreen extends AbstractContainerScreen<MultiblockMonitorMenu> {

    public MultiblockMonitorScreen(MultiblockMonitorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 220;
        this.imageHeight = 160;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) >> 1;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, 8, 0x1AEBFF, false);

        int startY = 28;
        int spacing = 12;

        guiGraphics.drawString(this.font, Component.literal("Structure Status:"), 12, startY, 0xDDDDDD, false);
        boolean structValid = this.menu.isStructureValid();
        String structText = structValid ? "VALID" : "INVALID";
        int structColor = structValid ? 0x00FF00 : 0xFF0000;
        guiGraphics.drawString(this.font, Component.literal(structText), 130, startY, structColor, false);

        guiGraphics.drawString(this.font, Component.literal("Inventory Status:"), 12, startY + spacing, 0xDDDDDD, false);
        boolean invValid = this.menu.isInventoriesValid();
        String invText = invValid ? "COMPLETE" : "INCOMPLETE";
        int invColor = invValid ? 0x00FF00 : 0xFFAA00;
        guiGraphics.drawString(this.font, Component.literal(invText), 130, startY + spacing, invColor, false);

        guiGraphics.drawString(this.font, Component.literal("ME Optic Connection:"), 12, startY + spacing * 2, 0xDDDDDD, false);
        boolean mePowered = this.menu.isMePowered();
        String powerText = mePowered ? "CONNECTED" : "OFFLINE";
        int powerColor = mePowered ? 0x00FF00 : 0xFF0000;
        guiGraphics.drawString(this.font, Component.literal(powerText), 130, startY + spacing * 2, powerColor, false);

        guiGraphics.fill(10, startY + spacing * 3 + 2, this.imageWidth - 10, startY + spacing * 3 + 3, 0xFF444D56);

        guiGraphics.drawString(this.font, Component.literal("PIC Durability Matrix:"), 12, startY + spacing * 4, 0x00D9FF, false);

        int gridY = startY + spacing * 5 + 2;
        renderPicStatus(guiGraphics, "PIC E (2,0,0):", this.menu.getPic1Durability(), 12, gridY);
        renderPicStatus(guiGraphics, "PIC W (-2,0,0):", this.menu.getPic2Durability(), 12, gridY + spacing);
        renderPicStatus(guiGraphics, "PIC S (0,0,2):", this.menu.getPic3Durability(), 12, gridY + spacing * 2);
        renderPicStatus(guiGraphics, "PIC N (0,0,-2):", this.menu.getPic4Durability(), 12, gridY + spacing * 3);
    }

    private void renderPicStatus(GuiGraphics guiGraphics, String label, int durability, int x, int y) {
        guiGraphics.drawString(this.font, Component.literal(label), x, y, 0xBBBBBB, false);
        String durText = durability == -1 ? "NOT INSTALLED" : durability + "%";
        int color;
        if (durability == -1) {
            color = 0x888888;
        } else if (durability > 50) {
            color = 0x00FF00;
        } else if (durability > 20) {
            color = 0xFFFF00;
        } else {
            color = 0xFF0000;
        }
        guiGraphics.drawString(this.font, Component.literal(durText), 130, y, color, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF0E1114);
        guiGraphics.fill(x, y, x + this.imageWidth, y + 1, 0xFF2F3640);
        guiGraphics.fill(x, y, x + 1, y + this.imageHeight, 0xFF2F3640);
        guiGraphics.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, 0xFF2F3640);
        guiGraphics.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, 0xFF2F3640);

        guiGraphics.fill(x + 5, y + 20, x + this.imageWidth - 5, y + 21, 0xFF144D56);
    }
}