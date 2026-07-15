package org.ae2craftcore.client.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.StyleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.LogicAssemblerMenu;

public class LogicAssemblerScreen extends AEBaseScreen<LogicAssemblerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "textures/gui/container/logic_assembler.png");

    public LogicAssemblerScreen(LogicAssemblerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/logic_assembler.json"));
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
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);
        int chance = this.menu.getCraftingChance();
        if (chance >= 0) {
            var text = Component.literal(chance + "%");
            int textWidth = this.font.width(text);
            guiGraphics.drawString(this.font, text, 122 - (textWidth >> 1), 26, 0x000000, false);
        }
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTicks);
        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();
        if (progress > 0 && maxProgress > 0) {
            int h = (progress * 18) / maxProgress;
            int offset = 18 - h;
            guiGraphics.blit(TEXTURE, this.leftPos + 135, this.topPos + 39 + offset, 197, offset, 6, h);
        }
    }
}