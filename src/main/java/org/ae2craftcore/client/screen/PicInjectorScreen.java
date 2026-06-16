package org.ae2craftcore.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.ae2craftcore.blocks.menu.PicInjectorMenu;
import org.jetbrains.annotations.NotNull;

public class PicInjectorScreen extends AbstractContainerScreen<PicInjectorMenu> {

    public PicInjectorScreen(PicInjectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 144;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) >> 1;
        this.inventoryLabelY = 51;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xDDDDDD, false);
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
        guiGraphics.fill(x + 5, y + 18, x + this.imageWidth - 5, y + 19, 0xFF2A313C);

        int slotX = x + 79;
        int slotY = y + 25;

        guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF0E1012);
        guiGraphics.fill(slotX - 1, slotY - 1, slotX + 19, slotY, 0xFF444D56);
        guiGraphics.fill(slotX - 1, slotY - 1, slotX, slotY + 19, 0xFF444D56);
        guiGraphics.fill(slotX + 18, slotY - 1, slotX + 19, slotY + 19, 0xFF444D56);
        guiGraphics.fill(slotX - 1, slotY + 18, slotX + 19, slotY + 19, 0xFF444D56);

        int invX = x + 7;
        int invY = y + 61;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int px = invX + col * 18;
                int py = invY + row * 18;
                drawMiniSlotOutline(guiGraphics, px, py);
            }
        }

        int hotbarY = y + 119;
        for (int col = 0; col < 9; col++) {
            int px = invX + col * 18;
            drawMiniSlotOutline(guiGraphics, px, hotbarY);
        }
    }

    private void drawMiniSlotOutline(GuiGraphics guiGraphics, int px, int py) {
        guiGraphics.fill(px, py, px + 18, py + 18, 0xFF0E1012);
        guiGraphics.fill(px, py, px + 18, py + 1, 0xFF23282F);
        guiGraphics.fill(px, py, px + 1, py + 18, 0xFF23282F);
        guiGraphics.fill(px + 17, py, px + 18, py + 18, 0xFF23282F);
        guiGraphics.fill(px, py + 17, px + 18, py + 18, 0xFF23282F);
    }
}