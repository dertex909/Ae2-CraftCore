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

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.GuiText;
import appeng.core.localization.InGameTooltip;
import appeng.util.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.ae2craftcore.blocks.menu.MeMachineInterfaceMenu;
import org.ae2craftcore.network.packet.MeMachineInterfaceSyncPacket;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class MeMachineInterfaceScreen extends AEBaseScreen<MeMachineInterfaceMenu> {

    private final SettingToggleButton<YesNo> blockingModeButton;
    private final SettingToggleButton<LockCraftingMode> lockCraftingModeButton;
    private final MeMachineInterfaceLockReason lockReason;

    private EditBox nameInput;

    public MeMachineInterfaceScreen(MeMachineInterfaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/me_machine_interface.json"));

        this.blockingModeButton = new ServerSettingToggleButton<>(Settings.BLOCKING_MODE, YesNo.NO);
        this.addToLeftToolbar(this.blockingModeButton);

        this.lockCraftingModeButton = new ServerSettingToggleButton<>(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE);
        this.addToLeftToolbar(this.lockCraftingModeButton);

        this.lockReason = new MeMachineInterfaceLockReason(this);
        widgets.add("lockReason", this.lockReason);

        widgets.addOpenPriorityButton();
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) >> 1;

        int x = this.leftPos;
        int y = this.topPos;

        String currentName = this.menu.getBlockEntity() != null ? this.menu.getBlockEntity().getInterfaceName() : "Unknown recipe";
        this.nameInput = new EditBox(this.font, x + 12, y + 38, 153, 12, Component.literal("Name"));
        this.nameInput.setBordered(false);
        this.nameInput.setTextColor(0xFFDDDDDD);
        this.nameInput.setValue(currentName);
        this.nameInput.setMaxLength(32);
        this.nameInput.setResponder(this::sendSyncPacket);
        this.addRenderableWidget(this.nameInput);
    }

    private void sendSyncPacket(String name) {
        ClientPacketDistributor.sendToServer(new MeMachineInterfaceSyncPacket(this.menu.getBlockPos(), name));
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (this.nameInput.keyPressed(event)) return true;
        if (this.nameInput.isFocused() && event.key() != 256) return true;
        return super.keyPressed(event);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.lockReason.setVisible(this.menu.getLockCraftingMode() != LockCraftingMode.NONE);
        this.blockingModeButton.set(this.menu.getBlockingMode());
        this.lockCraftingModeButton.set(this.menu.getLockCraftingMode());
    }

    @Override
    public void drawFG(GuiGraphicsExtractor graphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, 10, 0xFF3F3D52, false);
    }

    private static class MeMachineInterfaceLockReason implements ICompositeWidget {
        protected boolean visible = false;
        protected int x;
        protected int y;
        private final MeMachineInterfaceScreen screen;

        public MeMachineInterfaceLockReason(MeMachineInterfaceScreen screen) {
            this.screen = screen;
        }

        @Override
        public void setPosition(Point position) {
            this.x = position.getX();
            this.y = position.getY();
        }

        @Override
        public void setSize(int width, int height) {
        }

        @Override
        public Rect2i getBounds() {
            return new Rect2i(this.x, this.y, 126, 16);
        }

        @Override
        public final boolean isVisible() {
            return this.visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        @Override
        public void drawForegroundLayer(GuiGraphicsExtractor graphics, Rect2i bounds, Point mouse) {
            var menu = this.screen.getMenu();
            Icon icon;
            Component lockStatusText;
            if (menu.getCraftingLockedReason() == LockCraftingMode.NONE) {
                icon = Icon.UNLOCKED;
                lockStatusText = GuiText.CraftingLockIsUnlocked.text().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7DA9D2)));
            } else {
                icon = Icon.LOCKED;
                lockStatusText = GuiText.CraftingLockIsLocked.text().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xC1424B)));
            }
            Blitter.icon(icon).dest(this.x, this.y).blit(graphics);
            graphics.text(Minecraft.getInstance().font, lockStatusText, this.x + 15, this.y + 5, -1, false);
        }

        @Nullable
        @Override
        public Tooltip getTooltip(int mouseX, int mouseY) {
            var menu = this.screen.getMenu();
            var tooltip = switch (menu.getCraftingLockedReason()) {
                case LOCK_UNTIL_PULSE -> InGameTooltip.CraftingLockedUntilPulse.text();
                case LOCK_WHILE_HIGH -> InGameTooltip.CraftingLockedByRedstoneSignal.text();
                case LOCK_WHILE_LOW -> InGameTooltip.CraftingLockedByLackOfRedstoneSignal.text();
                default -> null;
            };
            return tooltip != null ? new Tooltip(tooltip) : null;
        }
    }
}