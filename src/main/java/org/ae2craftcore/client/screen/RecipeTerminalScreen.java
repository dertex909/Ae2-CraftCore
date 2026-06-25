package org.ae2craftcore.client.screen;

import appeng.api.stacks.AEItemKey;
import appeng.crafting.pattern.AEPatternDecoder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.network.packet.RecipeTerminalDeleteRecipePacket;
import org.ae2craftcore.network.packet.RecipeTerminalSavePacket;
import org.ae2craftcore.network.packet.RecipeTerminalSelectGroupPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class RecipeTerminalScreen extends AbstractContainerScreen<RecipeTerminalMenu> {
    private int groupScrollOffset = 0;
    private int recipeScrollOffset = 0;

    public RecipeTerminalScreen(RecipeTerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 84;
        this.titleLabelY = 6;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xDDDDDD, false);
        guiGraphics.drawString(this.font, Component.literal("Groups"), 6, 10, 0x999999, false);
        guiGraphics.drawString(this.font, Component.literal("Recipes"), 180, 10, 0x999999, false);

        var groups = getGroups();
        for (int i = 0; i < 5; i++) {
            int actualIndex = i + groupScrollOffset;
            if (actualIndex >= 0 && actualIndex < groups.size()) {
                var group = groups.get(actualIndex);
                boolean isSelected = group.name().equalsIgnoreCase(this.menu.getSelectedGroup());
                int textY = 22 + i * 16;

                int bgCol = isSelected ? 0xFF3D4F61 : 0xFF1B1F23;
                guiGraphics.fill(6, textY - 2, 76, textY + 12, bgCol);

                String displayName = group.name();
                if (this.font.width(displayName) > 55) {
                    displayName = this.font.plainSubstrByWidth(displayName, 50) + "..";
                }
                guiGraphics.drawString(this.font, displayName, 8, textY, isSelected ? 0xFFFFFF : 0xCCCCCC, false);
                guiGraphics.drawString(this.font, String.valueOf(group.count()), 64, textY, 0x888888, false);
            }
        }

        guiGraphics.fill(6, 106, 40, 120, 0xFF2A2E32);
        guiGraphics.drawString(this.font, "▲", 20, 108, 0xCCCCCC, false);

        guiGraphics.fill(42, 106, 76, 120, 0xFF2A2E32);
        guiGraphics.drawString(this.font, "▼", 56, 108, 0xCCCCCC, false);

        boolean hasSelectedGroup = !this.menu.getSelectedGroup().isEmpty();
        int saveBtnCol = hasSelectedGroup ? 0xFF1D6F42 : 0xFF2D3236;
        guiGraphics.fill(144, 84, 172, 102, saveBtnCol);
        guiGraphics.drawString(this.font, "Save", 147, 89, hasSelectedGroup ? 0xFFFFFF : 0x777777, false);

        var filtered = getFilteredRecipes();
        for (int i = 0; i < 5; i++) {
            int actualIndex = i + recipeScrollOffset;
            if (actualIndex >= 0 && actualIndex < filtered.size()) {
                int startY = 22 + i * 18;

                guiGraphics.fill(180, startY - 1, 250, startY + 16, 0xFF1C2023);
                guiGraphics.fill(236, startY + 2, 246, startY + 12, 0xFF7A1C1C);
                guiGraphics.drawString(this.font, "X", 239, startY + 3, 0xFFFFFF, false);
            }
        }

        guiGraphics.fill(180, 106, 212, 120, 0xFF2A2E32);
        guiGraphics.drawString(this.font, "▲", 193, 108, 0xCCCCCC, false);

        guiGraphics.fill(216, 106, 248, 120, 0xFF2A2E32);
        guiGraphics.drawString(this.font, "▼", 229, 108, 0xCCCCCC, false);
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

        guiGraphics.fill(x + 5, y + 18, x + 77, y + 103, 0xFF0D0F11);
        guiGraphics.fill(x + 5, y + 18, x + 77, y + 19, 0xFF2F363D);
        guiGraphics.fill(x + 5, y + 18, x + 6, y + 103, 0xFF2F363D);
        guiGraphics.fill(x + 76, y + 18, x + 77, y + 103, 0xFF2F363D);
        guiGraphics.fill(x + 5, y + 102, x + 77, y + 103, 0xFF2F363D);

        guiGraphics.fill(x + 80, y + 18, x + 177, y + 103, 0xFF0D0F11);
        guiGraphics.fill(x + 80, y + 18, x + 177, y + 19, 0xFF2F363D);
        guiGraphics.fill(x + 80, y + 18, x + 81, y + 103, 0xFF2F363D);
        guiGraphics.fill(x + 176, y + 18, x + 177, y + 103, 0xFF2F363D);
        guiGraphics.fill(x + 80, y + 102, x + 177, y + 103, 0xFF2F363D);

        guiGraphics.fill(x + 179, y + 18, x + 251, y + 103, 0xFF0D0F11);
        guiGraphics.fill(x + 179, y + 18, x + 251, y + 19, 0xFF2F363D);
        guiGraphics.fill(x + 179, y + 18, x + 180, y + 103, 0xFF2F363D);
        guiGraphics.fill(x + 250, y + 18, x + 251, y + 103, 0xFF2F363D);
        guiGraphics.fill(x + 179, y + 102, x + 251, y + 103, 0xFF2F363D);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotX = x + 84 + col * 18;
                int slotY = y + 22 + row * 18;
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF353B42);
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF14171A);
            }
        }

        for (int i = 0; i < 3; i++) {
            int slotX = x + 84 + i * 18;
            int slotY = y + 84;
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF353B42);
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF14171A);
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = x + 48 + col * 18;
                int slotY = y + 138 + row * 18;
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF353B42);
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF1F2428);
            }
        }

        for (int col = 0; col < 9; col++) {
            int slotX = x + 48 + col * 18;
            int slotY = y + 196;
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF353B42);
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF1F2428);
        }

        var filtered = getFilteredRecipes();
        for (int i = 0; i < 5; i++) {
            int actualIndex = i + recipeScrollOffset;
            if (actualIndex >= 0 && actualIndex < filtered.size()) {
                var recipe = filtered.get(actualIndex);
                if (!recipe.outputStack().isEmpty()) {
                    int renderX = x + 184;
                    int renderY = y + 22 + i * 18;
                    guiGraphics.renderFakeItem(recipe.outputStack(), renderX, renderY);
                }
            }
        }
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (mouseX - this.leftPos);
        int y = (mouseY - this.topPos);

        var filtered = getFilteredRecipes();
        for (int i = 0; i < 5; i++) {
            int actualIndex = i + recipeScrollOffset;
            if (actualIndex >= 0 && actualIndex < filtered.size()) {
                int startY = 22 + i * 18;
                if (x >= 184 && x < 200 && y >= startY && y < startY + 16) {
                    var recipe = filtered.get(actualIndex);
                    if (!recipe.outputStack().isEmpty()) {
                        guiGraphics.renderTooltip(this.font, recipe.outputStack(), mouseX, mouseY);
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (int) mouseX - this.leftPos;
        int y = (int) mouseY - this.topPos;

        if (x >= 144 && x < 172 && y >= 84 && y < 102) {
            String selected = this.menu.getSelectedGroup();
            if (!selected.isEmpty()) {
                PacketDistributor.sendToServer(new RecipeTerminalSavePacket(selected));
                if (this.minecraft != null) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
            }
            return true;
        }

        if (x >= 6 && x < 76 && y >= 22 && y < 102) {
            int index = (y - 22) / 16;
            var groups = getGroups();
            int actualIndex = index + groupScrollOffset;
            if (actualIndex >= 0 && actualIndex < groups.size()) {
                String clickedGroup = groups.get(actualIndex).name();
                this.menu.setSelectedGroup(clickedGroup);
                PacketDistributor.sendToServer(new RecipeTerminalSelectGroupPacket(clickedGroup));
                if (this.minecraft != null) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
            }
            return true;
        }

        if (x >= 6 && x < 40 && y >= 106 && y < 120) {
            if (groupScrollOffset > 0) {
                groupScrollOffset--;
                if (this.minecraft != null) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
            }
            return true;
        }
        if (x >= 42 && x < 76 && y >= 106 && y < 120) {
            var groups = getGroups();
            if (groupScrollOffset < groups.size() - 5) {
                groupScrollOffset++;
                if (this.minecraft != null) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
            }
            return true;
        }

        if (x >= 180 && x < 250 && y >= 22 && y < 112) {
            int itemIndex = (y - 22) / 18;
            var filtered = getFilteredRecipes();
            int actualIndex = itemIndex + recipeScrollOffset;
            if (actualIndex >= 0 && actualIndex < filtered.size()) {
                int startY = 22 + itemIndex * 18;
                if (x >= 236 && x < 246 && y >= startY + 2 && y < startY + 12) {
                    var patternToDelete = filtered.get(actualIndex).patternStack();
                    PacketDistributor.sendToServer(new RecipeTerminalDeleteRecipePacket(patternToDelete));
                    if (this.minecraft != null) {
                        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    }
                }
            }
            return true;
        }

        if (x >= 180 && x < 212 && y >= 106 && y < 120) {
            if (recipeScrollOffset > 0) {
                recipeScrollOffset--;
                if (this.minecraft != null) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
            }
            return true;
        }
        if (x >= 216 && x < 248 && y >= 106 && y < 120) {
            var filtered = getFilteredRecipes();
            if (recipeScrollOffset < filtered.size() - 5) {
                recipeScrollOffset++;
                if (this.minecraft != null) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private List<RecipeInfo> getFilteredRecipes() {
        var filtered = new ArrayList<RecipeInfo>();
        var currentList = this.menu.getClientRecipes();

        String selected = this.menu.getSelectedGroup();
        for (var p : currentList) {
            var customData = p.get(DataComponents.CUSTOM_DATA);
            String group = "";
            if (customData != null) {
                var tag = customData.copyTag();
                if (tag.contains("RecipeMachineGroup")) group = tag.getString("RecipeMachineGroup");
            }
            if (group.equalsIgnoreCase(selected)) {
                var outputStack = ItemStack.EMPTY;
                try {
                    var level = this.minecraft != null ? this.minecraft.level : null;
                    if (level != null) {
                        var details = AEPatternDecoder.INSTANCE.decodePattern(AEItemKey.of(p), level);
                        if (details != null && !details.getOutputs().isEmpty()) {
                            var firstOutput = details.getOutputs().getFirst();
                            if (firstOutput.what() instanceof AEItemKey itemKey) {
                                outputStack = itemKey.toStack((int) firstOutput.amount());
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                filtered.add(new RecipeInfo(p, outputStack));
            }
        }
        return filtered;
    }

    private List<GroupInfo> getGroups() {
        var map = new LinkedHashMap<String, Integer>();
        for (var group : this.menu.getClientGroups()) if (!group.isEmpty()) map.put(group, 0);
        for (var p : this.menu.getClientRecipes()) {
            var customData = p.get(DataComponents.CUSTOM_DATA);
            String group = "";
            if (customData != null) {
                var tag = customData.copyTag();
                if (tag.contains("RecipeMachineGroup")) group = tag.getString("RecipeMachineGroup");
            }
            if (!group.isEmpty()) map.put(group, map.getOrDefault(group, 0) + 1);
        }
        var list = new ArrayList<GroupInfo>();
        map.forEach((k, v) -> list.add(new GroupInfo(k, v)));
        return list;
    }

    public record GroupInfo(String name, int count) {
    }

    public record RecipeInfo(ItemStack patternStack, ItemStack outputStack) {
    }
}