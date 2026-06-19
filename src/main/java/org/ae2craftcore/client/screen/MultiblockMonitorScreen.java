package org.ae2craftcore.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.ae2craftcore.blocks.menu.MultiblockMonitorMenu;
import org.jetbrains.annotations.NotNull;

public class MultiblockMonitorScreen extends AbstractContainerScreen<MultiblockMonitorMenu> {

    private static final Component LABEL_STRUCTURE_STATUS = Component.literal("Structure Status:");
    private static final Component LABEL_INVENTORY_STATUS = Component.literal("Inventory Status:");
    private static final Component LABEL_ME_CONNECTION = Component.literal("ME Optic Connection:");
    private static final Component LABEL_PIC_MATRIX = Component.literal("PIC Durability Matrix:");

    private static final Component TEXT_VALID = Component.literal("VALID");
    private static final Component TEXT_INVALID = Component.literal("INVALID");
    private static final Component TEXT_COMPLETE = Component.literal("COMPLETE");
    private static final Component TEXT_INCOMPLETE = Component.literal("INCOMPLETE");
    private static final Component TEXT_CONNECTED = Component.literal("CONNECTED");
    private static final Component TEXT_OFFLINE = Component.literal("OFFLINE");
    private static final Component TEXT_NOT_INSTALLED = Component.literal("NOT INSTALLED");

    private static final Component PIC_LABEL_E = Component.literal("PIC E (2,0,0):");
    private static final Component PIC_LABEL_W = Component.literal("PIC W (-2,0,0):");
    private static final Component PIC_LABEL_S = Component.literal("PIC S (0,0,2):");
    private static final Component PIC_LABEL_N = Component.literal("PIC N (0,0,-2):");

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

        guiGraphics.drawString(this.font, LABEL_STRUCTURE_STATUS, 12, startY, 0xDDDDDD, false);
        boolean structValid = this.menu.isStructureValid();
        Component structText = structValid ? TEXT_VALID : TEXT_INVALID;
        int structColor = structValid ? 0x00FF00 : 0xFF0000;
        guiGraphics.drawString(this.font, structText, 130, startY, structColor, false);

        guiGraphics.drawString(this.font, LABEL_INVENTORY_STATUS, 12, startY + spacing, 0xDDDDDD, false);
        boolean invValid = this.menu.isInventoriesValid();
        Component invText = invValid ? TEXT_COMPLETE : TEXT_INCOMPLETE;
        int invColor = invValid ? 0x00FF00 : 0xFFAA00;
        guiGraphics.drawString(this.font, invText, 130, startY + spacing, invColor, false);

        guiGraphics.drawString(this.font, LABEL_ME_CONNECTION, 12, startY + spacing * 2, 0xDDDDDD, false);
        boolean mePowered = this.menu.isMePowered();
        Component powerText = mePowered ? TEXT_CONNECTED : TEXT_OFFLINE;
        int powerColor = mePowered ? 0x00FF00 : 0xFF0000;
        guiGraphics.drawString(this.font, powerText, 130, startY + spacing * 2, powerColor, false);

        guiGraphics.fill(10, startY + spacing * 3 + 2, this.imageWidth - 10, startY + spacing * 3 + 3, 0xFF444D56);

        guiGraphics.drawString(this.font, LABEL_PIC_MATRIX, 12, startY + spacing * 4, 0x00D9FF, false);

        int gridY = startY + spacing * 5 + 2;
        renderPicStatus(guiGraphics, PIC_LABEL_E, this.menu.getPic1Durability(), gridY);
        renderPicStatus(guiGraphics, PIC_LABEL_W, this.menu.getPic2Durability(), gridY + spacing);
        renderPicStatus(guiGraphics, PIC_LABEL_S, this.menu.getPic3Durability(), gridY + spacing * 2);
        renderPicStatus(guiGraphics, PIC_LABEL_N, this.menu.getPic4Durability(), gridY + spacing * 3);
    }

    private void renderPicStatus(GuiGraphics guiGraphics, Component label, int durability, int y) {
        guiGraphics.drawString(this.font, label, 12, y, 0xBBBBBB, false);

        Component durText;
        int color;
        if (durability == -1) {
            durText = TEXT_NOT_INSTALLED;
            color = 0x888888;
        } else {
            durText = Component.literal(durability + "%");
            if (durability > 50) {
                color = 0x00FF00;
            } else if (durability > 20) {
                color = 0xFFFF00;
            } else {
                color = 0xFF0000;
            }
        }
        guiGraphics.drawString(this.font, durText, 130, y, color, false);
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