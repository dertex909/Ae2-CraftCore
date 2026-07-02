package org.ae2craftcore.client.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.api.stacks.GenericStack;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.crafting.pattern.AEPatternDecoder;
import appeng.parts.encoding.EncodingMode;
import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.neoforged.neoforge.network.PacketDistributor;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.mixin.AbstractContainerScreenAccessor;
import org.ae2craftcore.network.packet.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class RecipeTerminalScreen extends AEBaseScreen<RecipeTerminalMenu> {

    private static final int TABS_X = RecipeTerminalMenu.MODE_TABS_X;
    private static final int TABS_Y = RecipeTerminalMenu.MODE_TABS_Y;
    private static final int TAB_W = 22;
    private static final int TAB_H = 22;
    private static final int TAB_STEP_Y = 21;

    private static final int SAVE_X = RecipeTerminalMenu.ENCODING_X + 127;
    private static final int SAVE_Y = 180;
    private static final int SAVE_W = 16;
    private static final int SAVE_H = 16;

    private static final int PROC_SCROLL_X = RecipeTerminalMenu.ENCODING_X + 7;
    private static final int PROC_SCROLL_Y = RecipeTerminalMenu.ENCODING_Y + 7;
    private static final int PROC_SCROLL_W = 11;
    private static final int PROC_SCROLL_H = 52;
    private static final int PROC_SCROLL_MAX = 24;

    private static final int STONE_SCROLL_X = RecipeTerminalMenu.ENCODING_X + 109;
    private static final int STONE_SCROLL_Y = RecipeTerminalMenu.ENCODING_Y + 7;
    private static final int STONE_SCROLL_W = 11;
    private static final int STONE_SCROLL_H = 52;

    private static final int MACHINE_SCROLL_X = 138;
    private static final int MACHINE_SCROLL_Y = 19;
    private static final int RECIPE_SCROLL_X = 303;
    private static final int RECIPE_SCROLL_Y = 19;

    private static final int SCROLL_W = 11;
    private static final int SCROLL_H = 114;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int LIST_ROWS = 5;
    private static final int ROW_HEIGHT = 23;
    private static final int ROW_BG_HEIGHT = 22;
    private static final int LIST_PADDING_TOP = 3;

    private static final int MACHINE_ROW_TEXT_OFFSET_Y = (ROW_BG_HEIGHT - 8) / 2;
    private static final int RECIPE_ROW_TEXT_OFFSET_Y = (ROW_BG_HEIGHT - 8) / 2;
    private static final int RECIPE_ROW_ITEM_OFFSET_Y = (ROW_BG_HEIGHT - 16) / 2;
    private static final int RECIPE_ROW_ICON_OFFSET_Y = (ROW_BG_HEIGHT - 12) / 2;
    private static final int RECIPE_ROW_CLEAR_OFFSET_Y = (ROW_BG_HEIGHT - 8) / 2;

    private static final int CRAFT_CLEAR_X = RecipeTerminalMenu.ENCODING_X + 62;
    private static final int CRAFT_CLEAR_Y = RecipeTerminalMenu.ENCODING_Y + 6;
    private static final int CRAFT_SUB_X = RecipeTerminalMenu.ENCODING_X + 72;
    private static final int CRAFT_SUB_Y = RecipeTerminalMenu.ENCODING_Y + 6;
    private static final int CRAFT_FLUID_X = RecipeTerminalMenu.ENCODING_X + 82;
    private static final int CRAFT_FLUID_Y = RecipeTerminalMenu.ENCODING_Y + 6;

    private static final int SMITH_CLEAR_X = RecipeTerminalMenu.ENCODING_X + 6;
    private static final int SMITH_CLEAR_Y = RecipeTerminalMenu.ENCODING_Y + 14;
    private static final int SMITH_SUB_X = RecipeTerminalMenu.ENCODING_X + 16;
    private static final int SMITH_SUB_Y = RecipeTerminalMenu.ENCODING_Y + 14;

    private static final int PROC_CLEAR_X = RecipeTerminalMenu.ENCODING_X + 72;
    private static final int PROC_CLEAR_Y = RecipeTerminalMenu.ENCODING_Y + 6;
    private static final int PROC_CYCLE_X = RecipeTerminalMenu.ENCODING_X + 89;
    private static final int PROC_CYCLE_Y = RecipeTerminalMenu.ENCODING_Y + 6;
    private static final int BUTTON_MINI = 8;

    private static final EncodingMode[] MODE_ORDER = {
            EncodingMode.CRAFTING,
            EncodingMode.PROCESSING,
            EncodingMode.SMITHING_TABLE,
            EncodingMode.STONECUTTING
    };

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "textures/gui/container/recipe_terminal.png");
    private static final ResourceLocation MACHINE_ROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "textures/gui/container/machine_row.png");

    private static final Blitter CRAFTING_BG = Blitter.texture("guis/pattern_modes.png").src(0, 0, 124, 66);
    private static final Blitter PROCESSING_BG = Blitter.texture("guis/pattern_modes.png").src(0, 70, 124, 66);
    private static final Blitter SMITHING_BG = Blitter.texture("guis/pattern_modes.png").src(128, 70, 124, 66);
    private static final Blitter STONECUTTING_BG = Blitter.texture("guis/pattern_modes.png").src(0, 140, 124, 66);
    private static final Blitter STONE_SLOT = STONECUTTING_BG.copy().src(124, 140, 20, 22);
    private static final Blitter STONE_SLOT_SELECTED = STONECUTTING_BG.copy().src(124, 162, 20, 22);
    private static final Blitter STONE_SLOT_HOVER = STONECUTTING_BG.copy().src(124, 184, 20, 22);

    private int groupScrollOffset = 0;
    private int recipeScrollOffset = 0;
    private int stonecutterScrollOffset = 0;

    private boolean draggingScrollbar = false;
    private int activeScrollbarType = 0;
    private double dragYOffset = 0;

    public RecipeTerminalScreen(RecipeTerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/recipe_terminal.json"));
        this.imageWidth = RecipeTerminalMenu.IMAGE_WIDTH;
        this.imageHeight = RecipeTerminalMenu.IMAGE_HEIGHT;
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = RecipeTerminalMenu.PLAYER_INV_X;
        this.inventoryLabelY = RecipeTerminalMenu.PLAYER_INV_Y - 12;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.clampScrollOffsets();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void renderSlot(@NotNull GuiGraphics guiGraphics, @NotNull Slot slot) {
        if (slot instanceof RecipeTerminalMenu.RecipeTerminalPhantomSlot || slot instanceof RecipeTerminalMenu.RecipeTerminalProcessingInputSlot || slot instanceof RecipeTerminalMenu.RecipeTerminalProcessingOutputSlot) {
            var itemstack = slot.getItem();
            if (!itemstack.isEmpty()) {
                guiGraphics.renderFakeItem(itemstack, slot.x, slot.y);
                int count = itemstack.getCount();
                if (count > 1) {
                    StackSizeRenderer.renderSizeLabel(guiGraphics, this.font, slot.x, slot.y, this.formatStackSize(count));
                }
                return;
            }
        }
        super.renderSlot(guiGraphics, slot);
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x333342, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.ae2craftcore.recipe_terminal.machines"), RecipeTerminalMenu.MACHINE_LIST_X + 1, 6, 0xFF403E53, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.ae2craftcore.recipe_terminal.recipes"), RecipeTerminalMenu.RECIPE_LIST_X + 1, 6, 0xFF403E53, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.ae2.PatternEncoding"), RecipeTerminalMenu.ENCODING_X, this.inventoryLabelY, 0xFF403E53, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF403E53, false);

        this.renderMachineRows(guiGraphics);
        this.renderRecipeRows(guiGraphics);
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        int x = this.leftPos;
        int y = this.topPos;
        int relMouseX = mouseX - x;
        int relMouseY = mouseY - y;

        guiGraphics.blit(BACKGROUND_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        this.drawEncodingPanel(guiGraphics, x, y, relMouseX, relMouseY);
        this.drawScrollbar(guiGraphics, x + MACHINE_SCROLL_X, y + MACHINE_SCROLL_Y, this.groupScrollOffset, Math.max(0, this.getGroups().size() - LIST_ROWS));
        this.drawScrollbar(guiGraphics, x + RECIPE_SCROLL_X, y + RECIPE_SCROLL_Y, this.recipeScrollOffset, Math.max(0, this.getFilteredRecipes().size() - LIST_ROWS));
        this.drawRecipeItems(guiGraphics, x, y, relMouseX, relMouseY);
    }

    private void drawEncodingPanel(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        this.getModeBackground().dest(x + RecipeTerminalMenu.ENCODING_X, y + RecipeTerminalMenu.ENCODING_Y).blit(guiGraphics);

        var currentMode = this.menu.getEncodingMode();
        for (int i = 0; i < MODE_ORDER.length; i++) {
            var mode = MODE_ORDER[i];
            int tabX = x + TABS_X;
            int tabY = y + TABS_Y + i * TAB_STEP_Y;
            var backdrop = mode == currentMode ? Icon.HORIZONTAL_TAB_SELECTED : Icon.HORIZONTAL_TAB;
            backdrop.getBlitter().dest(tabX, tabY).blit(guiGraphics);
            this.getModeIcon(mode).getBlitter().dest(tabX + 3, tabY + 2).blit(guiGraphics);
        }

        if (currentMode == EncodingMode.CRAFTING) {
            var subIcon = this.menu.substitute ? Icon.S_SUBSTITUTION_ENABLED : Icon.S_SUBSTITUTION_DISABLED;
            subIcon.getBlitter().dest(x + CRAFT_SUB_X, y + CRAFT_SUB_Y).blit(guiGraphics);
            Icon.S_CLEAR.getBlitter().dest(x + CRAFT_CLEAR_X, y + CRAFT_CLEAR_Y).blit(guiGraphics);
            var fluidIcon = this.menu.substituteFluids ? Icon.S_FLUID_SUBSTITUTION_ENABLED : Icon.S_FLUID_SUBSTITUTION_DISABLED;
            fluidIcon.getBlitter().dest(x + CRAFT_FLUID_X, y + CRAFT_FLUID_Y).blit(guiGraphics);
        }

        if (currentMode == EncodingMode.SMITHING_TABLE) {
            var subIcon = this.menu.substitute ? Icon.S_SUBSTITUTION_ENABLED : Icon.S_SUBSTITUTION_DISABLED;
            subIcon.getBlitter().dest(x + SMITH_SUB_X, y + SMITH_SUB_Y).blit(guiGraphics);
            Icon.S_CLEAR.getBlitter().dest(x + SMITH_CLEAR_X, y + SMITH_CLEAR_Y).blit(guiGraphics);
        }

        if (currentMode == EncodingMode.PROCESSING) {
            Icon.S_CLEAR.getBlitter().dest(x + PROC_CLEAR_X, y + PROC_CLEAR_Y).blit(guiGraphics);
            if (this.menu.canCycleProcessingOutputs()) {
                Icon.S_CYCLE.getBlitter().dest(x + PROC_CYCLE_X, y + PROC_CYCLE_Y).blit(guiGraphics);
            }
            this.drawAe2Scrollbar(guiGraphics, x + PROC_SCROLL_X, y + PROC_SCROLL_Y, this.menu.getProcessingScrollOffset());
        }

        if (currentMode == EncodingMode.STONECUTTING) {
            if (this.minecraft == null || this.minecraft.level == null) return;

            var matched = this.getMatchedStonecutterRecipes();
            int startIndex = this.stonecutterScrollOffset * 4;
            int endIndex = startIndex + 8;

            var selectedRecipe = this.menu.stonecuttingRecipeId;

            int absMouseX = x + mouseX;
            int absMouseY = y + mouseY;

            for (int i = startIndex; i < endIndex && i < matched.size(); ++i) {
                var slotBounds = getRecipeBounds(i - startIndex, x, y);
                var recipe = matched.get(i);
                boolean selected = selectedIdEquals(selectedRecipe, recipe.id());
                boolean hovered = isMouseInBounds(absMouseX, absMouseY, slotBounds);

                var blitter = STONE_SLOT;
                if (selected) {
                    blitter = STONE_SLOT_SELECTED;
                } else if (hovered) {
                    blitter = STONE_SLOT_HOVER;
                }

                blitter.dest(slotBounds.getX(), slotBounds.getY()).blit(guiGraphics);
                var resultItem = recipe.value().getResultItem(this.minecraft.level.registryAccess());
                if (selected || hovered) {
                    guiGraphics.renderFakeItem(resultItem, slotBounds.getX() + 2, slotBounds.getY() + 3);
                    guiGraphics.renderItemDecorations(this.font, resultItem, slotBounds.getX() + 2, slotBounds.getY() + 3);
                } else {
                    guiGraphics.renderFakeItem(resultItem, slotBounds.getX() + 2, slotBounds.getY() + 2);
                    guiGraphics.renderItemDecorations(this.font, resultItem, slotBounds.getX() + 2, slotBounds.getY() + 2);
                }
            }

            int totalRows = (matched.size() + 3) / 4;
            int maxScroll = Math.max(0, totalRows - 2);
            this.drawStonecutterScrollbar(guiGraphics, x + STONE_SCROLL_X, y + STONE_SCROLL_Y, this.stonecutterScrollOffset, maxScroll);
        }
        this.drawToolbarButton(guiGraphics, x + SAVE_X, y + SAVE_Y, this.isInside(mouseX, mouseY, SAVE_X, SAVE_Y, SAVE_W, SAVE_H));
    }

    private void drawAe2Scrollbar(GuiGraphics guiGraphics, int x, int y, int value) {
        var enabledSprite = ResourceLocation.fromNamespaceAndPath("ae2", "small_scroller");
        var disabledSprite = ResourceLocation.fromNamespaceAndPath("ae2", "small_scroller_disabled");

        int handleHeight = 15;
        int yOffset;
        ResourceLocation sprite;
        if (RecipeTerminalScreen.PROC_SCROLL_MAX == 0) {
            yOffset = 0;
            sprite = disabledSprite;
        } else {
            int availableHeight = RecipeTerminalScreen.PROC_SCROLL_H - handleHeight;
            yOffset = value * availableHeight / RecipeTerminalScreen.PROC_SCROLL_MAX;
            sprite = enabledSprite;
        }

        Blitter.guiSprite(sprite).dest(x, y + yOffset).blit(guiGraphics);
    }

    private void drawStonecutterScrollbar(GuiGraphics guiGraphics, int x, int y, int value, int maxScroll) {
        var enabledSprite = ResourceLocation.fromNamespaceAndPath("ae2", "small_scroller");
        var disabledSprite = ResourceLocation.fromNamespaceAndPath("ae2", "small_scroller_disabled");

        int handleHeight = 15;
        int yOffset;
        ResourceLocation sprite;
        if (maxScroll == 0) {
            yOffset = 0;
            sprite = disabledSprite;
        } else {
            int availableHeight = STONE_SCROLL_H - handleHeight;
            yOffset = value * availableHeight / maxScroll;
            sprite = enabledSprite;
        }

        Blitter.guiSprite(sprite).dest(x, y + yOffset).blit(guiGraphics);
    }

    private void drawToolbarButton(GuiGraphics guiGraphics, int x, int y, boolean hovered) {
        int yOffset = hovered ? 1 : 0;
        var bgIcon = hovered ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER : Icon.TOOLBAR_BUTTON_BACKGROUND;
        bgIcon.getBlitter().dest(x - 1, y + yOffset, 18, 20).zOffset(2).blit(guiGraphics);
        Icon.WHITE_ARROW_DOWN.getBlitter().dest(x, y + 1 + yOffset).zOffset(3).blit(guiGraphics);
    }

    private void drawScrollbar(GuiGraphics guiGraphics, int x, int y, int value, int maxScroll) {
        var enabledSprite = ResourceLocation.fromNamespaceAndPath("ae2", "big_scroller");
        var disabledSprite = ResourceLocation.fromNamespaceAndPath("ae2", "big_scroller_disabled");

        int yOffset;
        ResourceLocation sprite;
        if (maxScroll <= 0) {
            yOffset = 0;
            sprite = disabledSprite;
        } else {
            int availableHeight = SCROLL_H - SCROLLER_HEIGHT;
            yOffset = value * availableHeight / maxScroll;
            sprite = enabledSprite;
        }

        Blitter.guiSprite(sprite).dest(x, y + yOffset, SCROLLER_WIDTH, SCROLLER_HEIGHT).blit(guiGraphics);
    }

    private void drawRecipeItems(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        var filtered = this.getFilteredRecipes();
        for (int i = 0; i < LIST_ROWS; i++) {
            int actualIndex = i + this.recipeScrollOffset;
            if (actualIndex >= filtered.size()) continue;
            int rowTop = RecipeTerminalMenu.RECIPE_LIST_Y + LIST_PADDING_TOP + i * ROW_HEIGHT;
            int bgLeft = RecipeTerminalMenu.RECIPE_LIST_X + 2;
            int bgRight = RecipeTerminalMenu.RECIPE_LIST_X + RecipeTerminalMenu.RECIPE_LIST_WIDTH - 2;
            boolean hovered = this.isInside(mouseX, mouseY, bgLeft, rowTop, bgRight - bgLeft, ROW_BG_HEIGHT);
            guiGraphics.blit(MACHINE_ROW_TEXTURE, x + bgLeft, y + rowTop, 0, hovered ? 22 : 0, 128, 22, 128, 44);

            var recipe = filtered.get(actualIndex);
            var displayStack = recipe.outputStack().isEmpty() ? recipe.patternStack() : recipe.outputStack();
            int itemY = rowTop + RECIPE_ROW_ITEM_OFFSET_Y;
            guiGraphics.renderFakeItem(displayStack, x + RecipeTerminalMenu.RECIPE_LIST_X + 4, y + itemY);
            guiGraphics.renderItemDecorations(this.font, displayStack, x + RecipeTerminalMenu.RECIPE_LIST_X + 4, y + itemY);
            int iconY = rowTop + RECIPE_ROW_ICON_OFFSET_Y;
            this.getModeIcon(recipe.mode()).getBlitter().dest(x + RecipeTerminalMenu.RECIPE_LIST_X + 24, y + iconY).blit(guiGraphics);
            int clearY = rowTop + RECIPE_ROW_CLEAR_OFFSET_Y;
            Icon.S_CLEAR.getBlitter().dest(x + RecipeTerminalMenu.RECIPE_LIST_X + RecipeTerminalMenu.RECIPE_LIST_WIDTH - 13, y + clearY).blit(guiGraphics);
        }
    }

    private void renderMachineRows(GuiGraphics guiGraphics) {
        var groups = this.getGroups();
        for (int i = 0; i < LIST_ROWS; i++) {
            int actualIndex = i + this.groupScrollOffset;
            if (actualIndex >= groups.size()) continue;

            var group = groups.get(actualIndex);
            boolean selected = group.name().equalsIgnoreCase(this.menu.getSelectedGroup());
            int rowTop = RecipeTerminalMenu.MACHINE_LIST_Y + LIST_PADDING_TOP + i * ROW_HEIGHT;
            int bgLeft = RecipeTerminalMenu.MACHINE_LIST_X + 2;
            int textY = rowTop + MACHINE_ROW_TEXT_OFFSET_Y;
            guiGraphics.blit(MACHINE_ROW_TEXTURE, bgLeft, rowTop, 0, selected ? 22 : 0, 128, 22, 128, 44);

            String displayName = group.name();
            if (this.font.width(displayName) > 101) {
                displayName = this.font.plainSubstrByWidth(displayName, 96) + "...";
            }

            guiGraphics.drawString(this.font, displayName, RecipeTerminalMenu.MACHINE_LIST_X + 5, textY, selected ? 0x27304A : 0x42475A, false);
            guiGraphics.drawString(this.font, String.valueOf(group.count()), RecipeTerminalMenu.MACHINE_LIST_X + RecipeTerminalMenu.MACHINE_LIST_WIDTH - 14, textY, 0x656A7C, false);
        }
    }

    private void renderRecipeRows(GuiGraphics guiGraphics) {
        var filtered = this.getFilteredRecipes();
        for (int i = 0; i < LIST_ROWS; i++) {
            int actualIndex = i + this.recipeScrollOffset;
            if (actualIndex >= filtered.size()) continue;
            var recipe = filtered.get(actualIndex);
            int rowTop = RecipeTerminalMenu.RECIPE_LIST_Y + LIST_PADDING_TOP + i * ROW_HEIGHT;
            int textY = rowTop + RECIPE_ROW_TEXT_OFFSET_Y;

            String name = recipe.outputStack().isEmpty() ? recipe.patternStack().getHoverName().getString() : recipe.outputStack().getHoverName().getString();
            if (this.font.width(name) > 85) name = this.font.plainSubstrByWidth(name, 80) + "...";
            guiGraphics.drawString(this.font, name, RecipeTerminalMenu.RECIPE_LIST_X + 42, textY, 0x42475A, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 2 || (this.minecraft != null && this.minecraft.options.keyPickItem.matchesMouse(button))) {
            var slot = ((AbstractContainerScreenAccessor) this).ae2craftcore$findSlot(mouseX, mouseY);
            if ((slot instanceof RecipeTerminalMenu.RecipeTerminalProcessingInputSlot || slot instanceof RecipeTerminalMenu.RecipeTerminalProcessingOutputSlot) && slot.isActive()) {
                var currentStack = GenericStack.fromItemStack(slot.getItem());
                if (currentStack != null) {
                    var screen = new RecipeTerminalSetAmountScreen(this, currentStack, newStack -> {
                        var message = new InventoryActionPacket(InventoryAction.SET_FILTER, slot.index, GenericStack.wrapInItemStack(newStack));
                        PacketDistributor.sendToServer(message);
                        int count = newStack != null ? (int) newStack.amount() : 0;
                        this.menu.setPhantomSlotCount(slot.index, count);
                    });
                    switchToScreen(screen);
                    this.playClick();
                    return true;
                }
            }
        }

        int x = (int) mouseX - this.leftPos;
        int y = (int) mouseY - this.topPos;

        var currentMode = this.menu.getEncodingMode();

        if (currentMode == EncodingMode.CRAFTING && button == 0) {
            if (this.isInside(x, y, CRAFT_SUB_X, CRAFT_SUB_Y, BUTTON_MINI, BUTTON_MINI)) {
                PacketDistributor.sendToServer(new RecipeTerminalUpdateSettingsPacket(!this.menu.substitute, this.menu.substituteFluids));
                this.playClick();
                return true;
            }
            if (this.isInside(x, y, CRAFT_CLEAR_X, CRAFT_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.menu.clearEncodingSlots();
                PacketDistributor.sendToServer(new RecipeTerminalClearPacket());
                this.playClick();
                return true;
            }
            if (this.isInside(x, y, CRAFT_FLUID_X, CRAFT_FLUID_Y, BUTTON_MINI, BUTTON_MINI)) {
                PacketDistributor.sendToServer(new RecipeTerminalUpdateSettingsPacket(this.menu.substitute, !this.menu.substituteFluids));
                this.playClick();
                return true;
            }
        }

        if (currentMode == EncodingMode.SMITHING_TABLE && button == 0) {
            if (this.isInside(x, y, SMITH_SUB_X, SMITH_SUB_Y, BUTTON_MINI, BUTTON_MINI)) {
                PacketDistributor.sendToServer(new RecipeTerminalUpdateSettingsPacket(!this.menu.substitute, this.menu.substituteFluids));
                this.playClick();
                return true;
            }
            if (this.isInside(x, y, SMITH_CLEAR_X, SMITH_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.menu.clearEncodingSlots();
                PacketDistributor.sendToServer(new RecipeTerminalClearPacket());
                this.playClick();
                return true;
            }
        }

        if (currentMode == EncodingMode.PROCESSING && button == 0) {
            if (this.isInside(x, y, PROC_CLEAR_X, PROC_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.menu.clearEncodingSlots();
                PacketDistributor.sendToServer(new RecipeTerminalClearPacket());
                this.playClick();
                return true;
            }

            if (this.menu.canCycleProcessingOutputs() && this.isInside(x, y, PROC_CYCLE_X, PROC_CYCLE_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.menu.cycleProcessingOutputs();
                PacketDistributor.sendToServer(new RecipeTerminalCycleOutputsPacket());
                this.playClick();
                return true;
            }

            if (this.isInside(x, y, PROC_SCROLL_X - 2, PROC_SCROLL_Y, PROC_SCROLL_W, PROC_SCROLL_H)) {
                this.draggingScrollbar = true;
                this.activeScrollbarType = 1;
                this.setDragging(true);

                int handleHeight = 15;
                int currentScroll = this.menu.getProcessingScrollOffset();
                int availableHeight = PROC_SCROLL_H - handleHeight;
                int currentHandleY = currentScroll * availableHeight / PROC_SCROLL_MAX;
                int relY = y - PROC_SCROLL_Y;

                if (relY >= currentHandleY && relY < currentHandleY + handleHeight) {
                    this.dragYOffset = relY - currentHandleY;
                } else {
                    this.dragYOffset = handleHeight / 2.0;
                    double position = Math.clamp((relY - this.dragYOffset) / (double) availableHeight, 0.0, 1.0);
                    int newScroll = (int) Math.round(position * PROC_SCROLL_MAX);
                    this.menu.setProcessingScrollOffset(newScroll);
                }
                return true;
            }
        }

        if (currentMode == EncodingMode.STONECUTTING && button == 0) {
            if (this.minecraft == null || this.minecraft.level == null) return true;

            var matched = this.getMatchedStonecutterRecipes();
            int startIndex = this.stonecutterScrollOffset * 4;
            int endIndex = startIndex + 8;

            for (int i = startIndex; i < endIndex && i < matched.size(); ++i) {
                var slotBounds = getRecipeBounds(i - startIndex, this.leftPos, this.topPos);
                if (isMouseInBounds((int) mouseX, (int) mouseY, slotBounds)) {
                    var clickedRecipe = matched.get(i);
                    ResourceLocation recipeLoc;
                    var idObj = clickedRecipe.id();
                    if (idObj instanceof ResourceLocation rl) {
                        recipeLoc = rl;
                    } else {
                        recipeLoc = ResourceLocation.parse(idObj.toString());
                    }
                    this.menu.selectStonecutterRecipeOnServer(recipeLoc);
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    return true;
                }
            }

            if (this.isInside(x, y, STONE_SCROLL_X - 2, STONE_SCROLL_Y, STONE_SCROLL_W, STONE_SCROLL_H)) {
                this.draggingScrollbar = true;
                this.activeScrollbarType = 2;
                this.setDragging(true);

                int handleHeight = 15;
                int currentScroll = this.stonecutterScrollOffset;
                int totalRows = (this.getMatchedStonecutterRecipes().size() + 3) / 4;
                int maxScroll = Math.max(0, totalRows - 2);
                int availableHeight = STONE_SCROLL_H - handleHeight;
                int currentHandleY = maxScroll == 0 ? 0 : (currentScroll * availableHeight / maxScroll);
                int relY = y - STONE_SCROLL_Y;

                if (relY >= currentHandleY && relY < currentHandleY + handleHeight) {
                    this.dragYOffset = relY - currentHandleY;
                } else {
                    this.dragYOffset = handleHeight / 2.0;
                    double position = Math.clamp((relY - this.dragYOffset) / (double) availableHeight, 0.0, 1.0);
                    this.stonecutterScrollOffset = maxScroll == 0 ? 0 : (int) Math.round(position * maxScroll);
                }
                return true;
            }
        }
        for (int i = 0; i < MODE_ORDER.length; i++) {
            if (this.isInside(x, y, TABS_X, TABS_Y + i * TAB_STEP_Y, TAB_W, TAB_H)) {
                this.menu.setEncodingMode(MODE_ORDER[i]);
                PacketDistributor.sendToServer(new RecipeTerminalChangeModePacket(MODE_ORDER[i]));
                this.playClick();
                return true;
            }
        }

        if (this.isInside(x, y, SAVE_X, SAVE_Y, SAVE_W, SAVE_H)) {
            String selected = this.menu.getSelectedGroup();
            if (selected.isEmpty()) selected = "Default";
            PacketDistributor.sendToServer(new RecipeTerminalSavePacket(selected, this.menu.getEncodingMode(), this.menu.stonecuttingRecipeId, this.menu.substitute, this.menu.substituteFluids));
            this.playClick();
            return true;
        }

        if (this.isInside(x, y, RecipeTerminalMenu.MACHINE_LIST_X, RecipeTerminalMenu.MACHINE_LIST_Y,
                RecipeTerminalMenu.MACHINE_LIST_WIDTH, RecipeTerminalMenu.MACHINE_LIST_HEIGHT)) {
            int relativeY = y - RecipeTerminalMenu.MACHINE_LIST_Y - LIST_PADDING_TOP;

            if (relativeY >= 0) {
                int index = relativeY / ROW_HEIGHT;
                int rowOffset = relativeY % ROW_HEIGHT;

                if (rowOffset < ROW_BG_HEIGHT) {
                    var groups = this.getGroups();
                    int actualIndex = index + this.groupScrollOffset;
                    if (actualIndex >= 0 && actualIndex < groups.size()) {
                        String clickedGroup = groups.get(actualIndex).name();
                        this.menu.setSelectedGroup(clickedGroup);
                        this.recipeScrollOffset = 0;
                        PacketDistributor.sendToServer(new RecipeTerminalSelectGroupPacket(clickedGroup));
                        this.playClick();
                    }
                }
            }
            return true;
        }

        if (button == 0) {
            if (this.isInside(x, y, MACHINE_SCROLL_X - 2, MACHINE_SCROLL_Y, SCROLL_W + 4, SCROLL_H)) {
                int maxScroll = Math.max(0, this.getGroups().size() - LIST_ROWS);
                if (maxScroll > 0) {
                    this.draggingScrollbar = true;
                    this.activeScrollbarType = 3;
                    this.setDragging(true);

                    int handleHeight = SCROLLER_HEIGHT;
                    int availableHeight = SCROLL_H - handleHeight;
                    int currentHandleY = this.groupScrollOffset * availableHeight / maxScroll;
                    int relY = y - MACHINE_SCROLL_Y;

                    if (relY >= currentHandleY && relY < currentHandleY + handleHeight) {
                        this.dragYOffset = relY - currentHandleY;
                    } else {
                        this.dragYOffset = handleHeight / 2.0;
                        double position = Math.clamp((relY - this.dragYOffset) / (double) availableHeight, 0.0, 1.0);
                        this.groupScrollOffset = (int) Math.round(position * maxScroll);
                    }
                    this.playClick();
                }
                return true;
            }

            if (this.isInside(x, y, RECIPE_SCROLL_X - 2, RECIPE_SCROLL_Y, SCROLL_W + 4, SCROLL_H)) {
                int maxScroll = Math.max(0, this.getFilteredRecipes().size() - LIST_ROWS);
                if (maxScroll > 0) {
                    this.draggingScrollbar = true;
                    this.activeScrollbarType = 4;
                    this.setDragging(true);

                    int handleHeight = SCROLLER_HEIGHT;
                    int availableHeight = SCROLL_H - handleHeight;
                    int currentHandleY = this.recipeScrollOffset * availableHeight / maxScroll;
                    int relY = y - RECIPE_SCROLL_Y;

                    if (relY >= currentHandleY && relY < currentHandleY + handleHeight) {
                        this.dragYOffset = relY - currentHandleY;
                    } else {
                        this.dragYOffset = handleHeight / 2.0;
                        double position = Math.clamp((relY - this.dragYOffset) / (double) availableHeight, 0.0, 1.0);
                        this.recipeScrollOffset = (int) Math.round(position * maxScroll);
                    }
                    this.playClick();
                }
                return true;
            }
        }

        if (this.isInside(x, y, RecipeTerminalMenu.RECIPE_LIST_X, RecipeTerminalMenu.RECIPE_LIST_Y,
                RecipeTerminalMenu.RECIPE_LIST_WIDTH, RecipeTerminalMenu.RECIPE_LIST_HEIGHT)) {
            int relativeY = y - RecipeTerminalMenu.RECIPE_LIST_Y - LIST_PADDING_TOP;

            if (relativeY >= 0) {
                int index = relativeY / ROW_HEIGHT;
                int rowOffset = relativeY % ROW_HEIGHT;

                if (rowOffset < ROW_BG_HEIGHT) {
                    var filtered = this.getFilteredRecipes();
                    int actualIndex = index + this.recipeScrollOffset;
                    if (actualIndex >= 0 && actualIndex < filtered.size()) {
                        int rowTop = RecipeTerminalMenu.RECIPE_LIST_Y + LIST_PADDING_TOP + index * ROW_HEIGHT;

                        int clearX = RecipeTerminalMenu.RECIPE_LIST_X + RecipeTerminalMenu.RECIPE_LIST_WIDTH - 13;
                        int clearY = rowTop + RECIPE_ROW_CLEAR_OFFSET_Y;
                        if (this.isInside(x, y, clearX, clearY, 12, 12)) {
                            PacketDistributor.sendToServer(new RecipeTerminalDeleteRecipePacket(filtered.get(actualIndex).patternStack()));
                            this.playClick();
                        }
                    }
                }
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int x = (int) mouseX - this.leftPos;
        int y = (int) mouseY - this.topPos;
        int direction = scrollY > 0 ? -1 : 1;

        if (this.isInside(x, y, RecipeTerminalMenu.MACHINE_LIST_X, RecipeTerminalMenu.MACHINE_LIST_Y,
                RecipeTerminalMenu.MACHINE_LIST_WIDTH, RecipeTerminalMenu.MACHINE_LIST_HEIGHT)) {
            int maxScroll = Math.max(0, this.getGroups().size() - LIST_ROWS);
            int oldScroll = this.groupScrollOffset;
            this.groupScrollOffset = Math.clamp(this.groupScrollOffset + direction, 0, maxScroll);
            return oldScroll != this.groupScrollOffset;
        }

        if (this.isInside(x, y, RecipeTerminalMenu.RECIPE_LIST_X, RecipeTerminalMenu.RECIPE_LIST_Y,
                RecipeTerminalMenu.RECIPE_LIST_WIDTH, RecipeTerminalMenu.RECIPE_LIST_HEIGHT)) {
            int maxScroll = Math.max(0, this.getFilteredRecipes().size() - LIST_ROWS);
            int oldScroll = this.recipeScrollOffset;
            this.recipeScrollOffset = Math.clamp(this.recipeScrollOffset + direction, 0, maxScroll);
            return oldScroll != this.recipeScrollOffset;
        }

        var currentMode = this.menu.getEncodingMode();
        if (currentMode == EncodingMode.PROCESSING && this.isInside(x, y, RecipeTerminalMenu.ENCODING_X,
                RecipeTerminalMenu.ENCODING_Y, 124, 66)) {
            int oldScroll = this.menu.getProcessingScrollOffset();
            this.menu.setProcessingScrollOffset(Math.clamp(this.menu.getProcessingScrollOffset() + direction, 0, PROC_SCROLL_MAX));
            return oldScroll != this.menu.getProcessingScrollOffset();
        }

        if (currentMode == EncodingMode.STONECUTTING && this.isInside(x, y, RecipeTerminalMenu.ENCODING_X,
                RecipeTerminalMenu.ENCODING_Y, 124, 66)) {
            int totalRows = (this.getMatchedStonecutterRecipes().size() + 3) / 4;
            int maxScroll = Math.max(0, totalRows - 2);
            int oldScroll = this.stonecutterScrollOffset;
            this.stonecutterScrollOffset = Math.clamp(this.stonecutterScrollOffset + direction, 0, maxScroll);
            return oldScroll != this.stonecutterScrollOffset;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.draggingScrollbar = false;
            this.activeScrollbarType = 0;
            this.setDragging(false);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingScrollbar && button == 0) {
            int y = (int) mouseY - this.topPos;

            if (this.activeScrollbarType == 1) {
                int handleHeight = 15;
                double handleUpperEdgeY = y - PROC_SCROLL_Y - this.dragYOffset;
                double availableHeight = PROC_SCROLL_H - handleHeight;
                double position = Math.clamp(handleUpperEdgeY / availableHeight, 0.0, 1.0);
                int newScroll = (int) Math.round(position * PROC_SCROLL_MAX);
                this.menu.setProcessingScrollOffset(newScroll);
                return true;
            }

            if (this.activeScrollbarType == 2) {
                int handleHeight = 15;
                double handleUpperEdgeY = y - STONE_SCROLL_Y - this.dragYOffset;
                double availableHeight = STONE_SCROLL_H - handleHeight;
                double position = Math.clamp(handleUpperEdgeY / availableHeight, 0.0, 1.0);
                int totalRows = (this.getMatchedStonecutterRecipes().size() + 3) / 4;
                int maxScroll = Math.max(0, totalRows - 2);
                this.stonecutterScrollOffset = maxScroll == 0 ? 0 : (int) Math.round(position * maxScroll);
                return true;
            }

            if (this.activeScrollbarType == 3) {
                double handleUpperEdgeY = y - MACHINE_SCROLL_Y - this.dragYOffset;
                double availableHeight = SCROLL_H - SCROLLER_HEIGHT;
                double position = Math.clamp(handleUpperEdgeY / availableHeight, 0.0, 1.0);
                int maxScroll = Math.max(0, this.getGroups().size() - LIST_ROWS);
                this.groupScrollOffset = maxScroll == 0 ? 0 : (int) Math.round(position * maxScroll);
                return true;
            }

            if (this.activeScrollbarType == 4) {
                double handleUpperEdgeY = y - RECIPE_SCROLL_Y - this.dragYOffset;
                double availableHeight = SCROLL_H - SCROLLER_HEIGHT;
                double position = Math.clamp(handleUpperEdgeY / availableHeight, 0.0, 1.0);
                int maxScroll = Math.max(0, this.getFilteredRecipes().size() - LIST_ROWS);
                this.recipeScrollOffset = maxScroll == 0 ? 0 : (int) Math.round(position * maxScroll);
                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            if (this.hoveredSlot instanceof RecipeTerminalMenu.RecipeTerminalPhantomSlot
                    || this.hoveredSlot instanceof RecipeTerminalMenu.RecipeTerminalProcessingInputSlot
                    || this.hoveredSlot instanceof RecipeTerminalMenu.RecipeTerminalProcessingOutputSlot) {

                var itemStack = this.hoveredSlot.getItem();
                var tooltip = new ArrayList<>(this.getTooltipFromContainerItem(itemStack));

                if (this.menu.getEncodingMode() == EncodingMode.PROCESSING && this.menu.isProcessingOutputSlot(this.hoveredSlot)) {
                    boolean isPrimary = this.hoveredSlot.getContainerSlot() == 0;
                    if (isPrimary) {
                        tooltip.add(Component.translatable("gui.ae2.PatternEncoding.primary_processing_result_tooltip").withStyle(ChatFormatting.GOLD));
                        tooltip.add(Component.translatable("gui.ae2.PatternEncoding.primary_processing_result_hint").withStyle(ChatFormatting.GRAY));
                    } else {
                        tooltip.add(Component.translatable("gui.ae2.PatternEncoding.secondary_processing_result_tooltip").withStyle(ChatFormatting.GOLD));
                        tooltip.add(Component.translatable("gui.ae2.PatternEncoding.secondary_processing_result_hint").withStyle(ChatFormatting.GRAY));
                    }
                }

                if (this.menu.getEncodingMode() == EncodingMode.PROCESSING) {
                    tooltip.add(Component.translatable("gui.tooltips.ae2.ModifyAmountAction", Component.translatable("gui.tooltips.ae2.MiddleClick").withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY));
                }

                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                return;
            }
        }

        int x = mouseX - this.leftPos;
        int y = mouseY - this.topPos;

        var currentMode = this.menu.getEncodingMode();

        if (currentMode == EncodingMode.STONECUTTING) {
            if (this.minecraft == null || this.minecraft.level == null) return;
            var matched = this.getMatchedStonecutterRecipes();
            int startIndex = this.stonecutterScrollOffset * 4;
            int endIndex = startIndex + 8;

            for (int i = startIndex; i < endIndex && i < matched.size(); ++i) {
                var slotBounds = getRecipeBounds(i - startIndex, this.leftPos, this.topPos);
                if (isMouseInBounds(mouseX, mouseY, slotBounds)) {
                    var outputStack = matched.get(i).value().getResultItem(this.minecraft.level.registryAccess());
                    guiGraphics.renderTooltip(this.font, outputStack, mouseX, mouseY);
                    return;
                }
            }
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        if (currentMode == EncodingMode.CRAFTING) {
            if (this.isInside(x, y, CRAFT_SUB_X, CRAFT_SUB_Y, BUTTON_MINI, BUTTON_MINI)) {
                var title = Component.translatable(this.menu.substitute ? "gui.tooltips.ae2.SubstitutionsOn" : "gui.tooltips.ae2.SubstitutionsOff");
                var desc = Component.translatable(this.menu.substitute ? "gui.tooltips.ae2.SubstitutionsDescEnabled" : "gui.tooltips.ae2.SubstitutionsDescDisabled");
                this.renderCustomTooltip(guiGraphics, title, desc, mouseX, mouseY);
                return;
            }
            if (this.isInside(x, y, CRAFT_CLEAR_X, CRAFT_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.renderCustomTooltip(guiGraphics, Component.translatable("gui.tooltips.ae2.Clear"), Component.translatable("gui.tooltips.ae2.ClearSettings"), mouseX, mouseY);
                return;
            }
            if (this.isInside(x, y, CRAFT_FLUID_X, CRAFT_FLUID_Y, BUTTON_MINI, BUTTON_MINI)) {
                var title = Component.translatable("gui.tooltips.ae2.FluidSubstitutions");
                var desc = Component.translatable(this.menu.substituteFluids ? "gui.tooltips.ae2.FluidSubstitutionsDescEnabled" : "gui.tooltips.ae2.FluidSubstitutionsDescDisabled");
                this.renderCustomTooltip(guiGraphics, title, desc, mouseX, mouseY);
                return;
            }
        } else if (currentMode == EncodingMode.SMITHING_TABLE) {
            if (this.isInside(x, y, SMITH_SUB_X, SMITH_SUB_Y, BUTTON_MINI, BUTTON_MINI)) {
                var title = Component.translatable(this.menu.substitute ? "gui.tooltips.ae2.SubstitutionsOn" : "gui.tooltips.ae2.SubstitutionsOff");
                var desc = Component.translatable(this.menu.substitute ? "gui.tooltips.ae2.SubstitutionsDescEnabled" : "gui.tooltips.ae2.SubstitutionsDescDisabled");
                this.renderCustomTooltip(guiGraphics, title, desc, mouseX, mouseY);
                return;
            }
            if (this.isInside(x, y, SMITH_CLEAR_X, SMITH_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.renderCustomTooltip(guiGraphics, Component.translatable("gui.tooltips.ae2.Clear"), Component.translatable("gui.tooltips.ae2.ClearSettings"), mouseX, mouseY);
                return;
            }
        } else if (currentMode == EncodingMode.PROCESSING) {
            if (this.isInside(x, y, PROC_CLEAR_X, PROC_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.renderCustomTooltip(guiGraphics, Component.translatable("gui.tooltips.ae2.Clear"), Component.translatable("gui.tooltips.ae2.ClearSettings"), mouseX, mouseY);
                return;
            }
            if (this.menu.canCycleProcessingOutputs() && this.isInside(x, y, PROC_CYCLE_X, PROC_CYCLE_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.renderCustomTooltip(guiGraphics, Component.translatable("gui.tooltips.ae2.CycleProcessingOutput"), Component.translatable("gui.tooltips.ae2.CycleProcessingOutputTooltip"), mouseX, mouseY);
                return;
            }
        }

        for (int i = 0; i < MODE_ORDER.length; i++) {
            if (this.isInside(x, y, TABS_X, TABS_Y + i * TAB_STEP_Y, TAB_W, TAB_H)) {
                guiGraphics.renderTooltip(this.font, this.getModeTooltip(MODE_ORDER[i]), mouseX, mouseY);
                return;
            }
        }

        if (this.isInside(x, y, SAVE_X, SAVE_Y, SAVE_W, SAVE_H)) {
            var title = Component.translatable("gui.tooltips.ae2.Encode");
            this.renderCustomTooltip(guiGraphics, title, Component.nullToEmpty(null), mouseX, mouseY);
        }
    }

    private void renderCustomTooltip(GuiGraphics guiGraphics, Component title, Component desc, int mouseX, int mouseY) {
        var tooltip = new ArrayList<Component>();
        tooltip.add(title.copy().withStyle(ChatFormatting.WHITE));

        String descText = desc.getString();
        int limit = 35;
        var paragraphs = descText.split("\n");
        for (var paragraph : paragraphs) {
            var words = paragraph.split(" ");
            var currentLine = new StringBuilder();

            for (var word : words) {
                if (currentLine.length() + word.length() + (!currentLine.isEmpty() ? 1 : 0) <= limit) {
                    if (!currentLine.isEmpty()) currentLine.append(" ");
                    currentLine.append(word);
                } else {
                    if (!currentLine.isEmpty()) {
                        tooltip.add(Component.literal(currentLine.toString()).withStyle(ChatFormatting.GRAY));
                        currentLine = new StringBuilder();
                    }
                    while (word.length() > limit) {
                        tooltip.add(Component.literal(word.substring(0, limit)).withStyle(ChatFormatting.GRAY));
                        word = word.substring(limit);
                    }
                    currentLine.append(word);
                }
            }
            if (!currentLine.isEmpty()) {
                tooltip.add(Component.literal(currentLine.toString()).withStyle(ChatFormatting.GRAY));
            }
        }

        guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
    }

    private Component getModeTooltip(EncodingMode mode) {
        return switch (mode) {
            case CRAFTING -> Component.translatable("item.ae2.crafting_pattern");
            case PROCESSING -> Component.translatable("item.ae2.processing_pattern");
            case SMITHING_TABLE -> Component.translatable("item.ae2.smithing_table_pattern");
            case STONECUTTING -> Component.translatable("item.ae2.stonecutting_pattern");
        };
    }

    private String formatStackSize(int count) {
        if (count >= 1_000_000) return String.format("%.1fM", count / 1_000_000.0).replace(".0", "");
        if (count >= 10_000) return String.format("%.1fK", count / 1000.0).replace(".0", "");
        return String.valueOf(count);
    }

    private void playClick() {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    private Blitter getModeBackground() {
        return switch (this.menu.getEncodingMode()) {
            case CRAFTING -> CRAFTING_BG;
            case PROCESSING -> PROCESSING_BG;
            case SMITHING_TABLE -> SMITHING_BG;
            case STONECUTTING -> STONECUTTING_BG;
        };
    }

    private Icon getModeIcon(EncodingMode mode) {
        return switch (mode) {
            case CRAFTING -> Icon.TAB_CRAFTING;
            case PROCESSING -> Icon.TAB_PROCESSING;
            case SMITHING_TABLE -> Icon.TAB_SMITHING;
            case STONECUTTING -> Icon.TAB_STONECUTTING;
        };
    }

    private boolean isInside(int x, int y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    private void clampScrollOffsets() {
        this.groupScrollOffset = Math.clamp(this.groupScrollOffset, 0, Math.max(0, this.getGroups().size() - LIST_ROWS));
        this.recipeScrollOffset = Math.clamp(this.recipeScrollOffset, 0, Math.max(0, this.getFilteredRecipes().size() - LIST_ROWS));
    }

    private List<RecipeInfo> getFilteredRecipes() {
        var filtered = new ArrayList<RecipeInfo>();
        var currentList = this.menu.getClientRecipes();
        String selected = this.menu.getSelectedGroup();

        for (var pattern : currentList) {
            if (!this.getPatternGroup(pattern).equalsIgnoreCase(selected)) continue;

            var outputStack = ItemStack.EMPTY;
            try {
                var level = this.minecraft != null ? this.minecraft.level : null;
                if (level != null) {
                    var details = AEPatternDecoder.INSTANCE.decodePattern(AEItemKey.of(pattern), level);
                    if (details != null && !details.getOutputs().isEmpty()) {
                        var firstOutput = details.getOutputs().getFirst();
                        if (firstOutput.what() instanceof AEItemKey itemKey) {
                            outputStack = itemKey.toStack((int) firstOutput.amount());
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            filtered.add(new RecipeInfo(pattern, outputStack, this.getPatternMode(pattern)));
        }
        return filtered;
    }

    private List<GroupInfo> getGroups() {
        var map = new LinkedHashMap<String, Integer>();
        for (var group : this.menu.getClientGroups()) if (!group.isEmpty()) map.put(group, 0);
        for (var pattern : this.menu.getClientRecipes()) {
            var group = this.getPatternGroup(pattern);
            if (!group.isEmpty()) map.put(group, map.getOrDefault(group, 0) + 1);
        }
        var list = new ArrayList<GroupInfo>();
        map.forEach((k, v) -> list.add(new GroupInfo(k, v)));
        return list;
    }

    private String getPatternGroup(ItemStack pattern) {
        var customData = pattern.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) return "";
        var tag = customData.copyTag();
        return tag.contains("RecipeMachineGroup") ? tag.getString("RecipeMachineGroup") : "";
    }

    private EncodingMode getPatternMode(ItemStack pattern) {
        if (AEItems.CRAFTING_PATTERN.is(pattern)) return EncodingMode.CRAFTING;
        if (AEItems.SMITHING_TABLE_PATTERN.is(pattern)) return EncodingMode.SMITHING_TABLE;
        if (AEItems.STONECUTTING_PATTERN.is(pattern)) return EncodingMode.STONECUTTING;
        return EncodingMode.PROCESSING;
    }

    @Override
    protected boolean shouldAddToolbar() {
        return false;
    }

    private List<RecipeHolder<StonecutterRecipe>> getMatchedStonecutterRecipes() {
        var list = new ArrayList<RecipeHolder<StonecutterRecipe>>();
        if (this.minecraft == null || this.minecraft.level == null) return list;

        var inputStack = ItemStack.EMPTY;
        for (var slot : this.menu.slots) {
            if (slot instanceof RecipeTerminalMenu.RecipeTerminalPhantomSlot phantomSlot && phantomSlot.getMode() == EncodingMode.STONECUTTING) {
                inputStack = phantomSlot.getItem();
                break;
            }
        }

        if (inputStack.isEmpty()) return list;

        var level = this.minecraft.level;
        var recipeManager = level.getRecipeManager();
        var recipeInput = new SingleRecipeInput(inputStack);
        list.addAll(recipeManager.getRecipesFor(RecipeType.STONECUTTING, recipeInput, level));
        return list;
    }

    public record GroupInfo(String name, int count) {
    }

    public record RecipeInfo(ItemStack patternStack, ItemStack outputStack, EncodingMode mode) {
    }

    private Rect2i getRecipeBounds(int index, int screenLeft, int screenTop) {
        int col = index % 4;
        int row = index / 4;
        int slotX = screenLeft + RecipeTerminalMenu.ENCODING_X + 27 + col * 20;
        int slotY = screenTop + RecipeTerminalMenu.ENCODING_Y + 11 + row * 22;
        return new Rect2i(slotX, slotY, 20, 22);
    }

    private boolean isMouseInBounds(int mouseX, int mouseY, Rect2i rect) {
        return mouseX >= rect.getX() && mouseX < rect.getX() + rect.getWidth() && mouseY >= rect.getY() && mouseY < rect.getY() + rect.getHeight();
    }

    private boolean selectedIdEquals(ResourceLocation selected, Object recipeId) {
        if (selected == null || recipeId == null) return false;
        return selected.equals(recipeId) || recipeId.toString().contains(selected.toString());
    }
}