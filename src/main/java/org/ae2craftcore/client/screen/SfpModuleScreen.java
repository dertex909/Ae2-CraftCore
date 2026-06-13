package org.ae2craftcore.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.ae2craftcore.blocks.menu.SfpModuleMenu;
import org.ae2craftcore.network.SfpChannelPacket;
import org.jetbrains.annotations.NotNull;

public class SfpModuleScreen extends AbstractContainerScreen<SfpModuleMenu> {

    public SfpModuleScreen(SfpModuleMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 110;
    }

    @Override
    protected void init() {
        super.init();
        
        int x = this.leftPos;
        int y = this.topPos;

        this.addRenderableWidget(Button.builder(Component.literal("-512"), b -> adjustChannels(-512))
                .bounds(x + 10, y + 55, 36, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("-64"), b -> adjustChannels(-64))
                .bounds(x + 48, y + 55, 36, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("+64"), b -> adjustChannels(64))
                .bounds(x + 92, y + 55, 36, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("+512"), b -> adjustChannels(512))
                .bounds(x + 130, y + 55, 36, 20).build());
    }

    private void adjustChannels(int delta) {
        int current = this.menu.getChannels();
        int next = Math.clamp(current + delta, 64, 8192);
        if (next != current) PacketDistributor.sendToServer(new SfpChannelPacket(this.menu.getBlockPos(), next));
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, Component.literal("SFP-Module Option Slot"), 10, 8, 0xDDDDDD, false);

        boolean isConnected = this.menu.isPathValid();
        var statusText = isConnected ? Component.literal("Status: CONNECTED") : Component.literal("Status: OFFLINE");
        int statusColor = isConnected ? 0x22FF22 : 0xFF2222;
        guiGraphics.drawString(this.font, statusText, 10, 22, statusColor, false);

        String channelString = String.format("%d Channels", this.menu.getChannels());
        int channelStrWidth = this.font.width(channelString);
        guiGraphics.drawString(this.font, Component.literal(channelString), (this.imageWidth - channelStrWidth) / 2, 40, 0x1AEBFF, false);

        guiGraphics.drawString(this.font, Component.literal("Channels Range: 64 to 8192"), 14, 88, 0x888888, false);
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
    }
}