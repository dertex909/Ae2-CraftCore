package org.ae2craftcore.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.ae2craftcore.blocks.menu.MeMachineInterfaceMenu;
import org.ae2craftcore.network.packet.MeMachineInterfaceSyncPacket;
import org.jetbrains.annotations.NotNull;

import static org.ae2craftcore.Ae2craftcore.MODID;

public class MeMachineInterfaceScreen extends AbstractContainerScreen<MeMachineInterfaceMenu> {

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/me_machine_interface.png");

    private EditBox nameInput;

    public MeMachineInterfaceScreen(MeMachineInterfaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 60;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) >> 1;

        int x = this.leftPos;
        int y = this.topPos;

        String currentName = this.menu.getBlockEntity() != null ? this.menu.getBlockEntity().getInterfaceName() : "Recipe";
        this.nameInput = new EditBox(this.font, x + 12, y + 38, 153, 12, Component.literal("Name"));
        this.nameInput.setBordered(false);
        this.nameInput.setTextColor(0xE0E0E0);
        this.nameInput.setValue(currentName);
        this.nameInput.setMaxLength(32);
        this.nameInput.setResponder(this::sendSyncPacket);
        this.addRenderableWidget(this.nameInput);
    }

    private void sendSyncPacket(String name) {
        PacketDistributor.sendToServer(new MeMachineInterfaceSyncPacket(this.menu.getBlockPos(), name));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.nameInput.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (this.nameInput.isFocused() && keyCode != 256) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, 10, 0xFF3F3D52, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }
}