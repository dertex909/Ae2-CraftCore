package org.ae2craftcore.client.screen;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.ae2craftcore.blocks.menu.MeMachineInterfaceMenu;
import org.ae2craftcore.network.packet.MeMachineInterfaceSyncPacket;

public class MeMachineInterfaceScreen extends AEBaseScreen<MeMachineInterfaceMenu> {

    private final SettingToggleButton<YesNo> blockingModeButton;
    private final SettingToggleButton<LockCraftingMode> lockCraftingModeButton;

    private EditBox nameInput;

    public MeMachineInterfaceScreen(MeMachineInterfaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/me_machine_interface.json"));
        this.imageWidth = 176;
        this.imageHeight = 60;

        this.blockingModeButton = new ServerSettingToggleButton<>(Settings.BLOCKING_MODE, YesNo.NO);
        this.addToLeftToolbar(this.blockingModeButton);

        this.lockCraftingModeButton = new ServerSettingToggleButton<>(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE);
        this.addToLeftToolbar(this.lockCraftingModeButton);

        widgets.addOpenPriorityButton();
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
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.blockingModeButton.set(this.menu.getBlockingMode());
        this.lockCraftingModeButton.set(this.menu.getLockCraftingMode());
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, 10, 0xFF3F3D52, false);
    }
}