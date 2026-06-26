package org.ae2craftcore.client.screen;

import appeng.api.stacks.AEItemKey;
import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AEPatternDecoder;
import appeng.parts.encoding.EncodingMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.neoforged.neoforge.network.PacketDistributor;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.network.packet.RecipeTerminalClearPacket;
import org.ae2craftcore.network.packet.RecipeTerminalDeleteRecipePacket;
import org.ae2craftcore.network.packet.RecipeTerminalSavePacket;
import org.ae2craftcore.network.packet.RecipeTerminalSelectGroupPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class RecipeTerminalScreen extends AbstractContainerScreen<RecipeTerminalMenu> {
    private static final EncodingMode[] MODE_ORDER = {
            EncodingMode.CRAFTING,
            EncodingMode.PROCESSING,
            EncodingMode.SMITHING_TABLE,
            EncodingMode.STONECUTTING
    };

    private static final Blitter CRAFTING_BG = Blitter.texture("guis/pattern_modes.png").src(0, 0, 124, 66);
    private static final Blitter PROCESSING_BG = Blitter.texture("guis/pattern_modes.png").src(0, 70, 124, 66);
    private static final Blitter SMITHING_BG = Blitter.texture("guis/pattern_modes.png").src(128, 70, 124, 66);
    private static final Blitter STONECUTTING_BG = Blitter.texture("guis/pattern_modes.png").src(0, 140, 124, 66);
    private static final Blitter STONE_RECIPE_SLOT = Blitter.texture("guis/pattern_modes.png").src(124, 140, 20, 22);
    private static final Blitter STONE_RECIPE_SLOT_SELECTED = Blitter.texture("guis/pattern_modes.png").src(124, 162, 20, 22);
    private static final Blitter STONE_RECIPE_SLOT_HOVER = Blitter.texture("guis/pattern_modes.png").src(124, 184, 20, 22);

    private static final int LIST_ROWS = 6;
    private static final int MACHINE_ROW_HEIGHT = 17;
    private static final int RECIPE_ROW_HEIGHT = 18;
    private static final int STONE_COLS = 4;
    private static final int STONE_ROWS = 2;
    private static final int CRAFTING_RESULT_X = RecipeTerminalMenu.ENCODING_X + 106;
    private static final int CRAFTING_RESULT_Y = RecipeTerminalMenu.ENCODING_Y + 23;
    private static final int SMITHING_RESULT_X = RecipeTerminalMenu.ENCODING_X + 109;
    private static final int SMITHING_RESULT_Y = RecipeTerminalMenu.ENCODING_Y + 23;

    private int groupScrollOffset = 0;
    private int recipeScrollOffset = 0;
    private int stoneScrollOffset = 0;
    private EncodingMode encodingMode = EncodingMode.PROCESSING;
    @Nullable
    private ResourceLocation selectedStonecuttingRecipeId;

    private boolean draggingScrollbar = false;
    private int activeScrollbarType = 0;
    private double dragYOffset = 0;

    public RecipeTerminalScreen(RecipeTerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = RecipeTerminalMenu.IMAGE_WIDTH;
        this.imageHeight = RecipeTerminalMenu.IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = RecipeTerminalMenu.PLAYER_INV_X;
        this.inventoryLabelY = RecipeTerminalMenu.PLAYER_INV_Y - 12;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.menu.setEncodingMode(this.encodingMode);
        this.clampScrollOffsets();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x333342, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.ae2craftcore.recipe_terminal.machines"),
                RecipeTerminalMenu.MACHINE_LIST_X, 13, 0x55596B, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.ae2craftcore.recipe_terminal.recipes"),
                RecipeTerminalMenu.RECIPE_LIST_X, 13, 0x55596B, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.ae2.PatternEncoding"),
                RecipeTerminalMenu.ENCODING_X, 24, 0x55596B, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x55596B, false);

        this.renderMachineRows(guiGraphics);
        this.renderRecipeRows(guiGraphics);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int relMouseX = mouseX - x;
        int relMouseY = mouseY - y;

        this.drawMainPanel(guiGraphics, x, y);
        this.drawListPanel(guiGraphics, x + RecipeTerminalMenu.MACHINE_LIST_X, y + RecipeTerminalMenu.MACHINE_LIST_Y,
                RecipeTerminalMenu.MACHINE_LIST_WIDTH, RecipeTerminalMenu.MACHINE_LIST_HEIGHT);
        this.drawListPanel(guiGraphics, x + RecipeTerminalMenu.RECIPE_LIST_X, y + RecipeTerminalMenu.RECIPE_LIST_Y,
                RecipeTerminalMenu.RECIPE_LIST_WIDTH, RecipeTerminalMenu.RECIPE_LIST_HEIGHT);

        this.drawEncodingPanel(guiGraphics, x, y, relMouseX, relMouseY);
        this.drawControlButtons(guiGraphics, x, y, relMouseX, relMouseY);
        this.drawScrollButtons(guiGraphics, x, y);
        this.drawInventorySlots(guiGraphics, x, y);
        this.drawRecipeItems(guiGraphics, x, y, relMouseX, relMouseY);
    }

    private void drawMainPanel(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFD6D8E1);
        guiGraphics.fill(x, y, x + this.imageWidth, y + 2, 0xFFF8F8FF);
        guiGraphics.fill(x, y, x + 2, y + this.imageHeight, 0xFFF8F8FF);
        guiGraphics.fill(x + this.imageWidth - 2, y, x + this.imageWidth, y + this.imageHeight, 0xFF7F8496);
        guiGraphics.fill(x, y + this.imageHeight - 2, x + this.imageWidth, y + this.imageHeight, 0xFF7F8496);
        guiGraphics.fill(x + 3, y + 3, x + this.imageWidth - 3, y + this.imageHeight - 3, 0xFFC9CBD6);
    }

    private void drawListPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFFF4F5FA);
        guiGraphics.fill(x, y, x + width, y + height, 0xFFAEB2C2);
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF777D94);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFF777D94);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFFE9EAF2);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFFE9EAF2);
    }

    private void drawEncodingPanel(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        this.getModeBackground().dest(x + RecipeTerminalMenu.ENCODING_X, y + RecipeTerminalMenu.ENCODING_Y).blit(guiGraphics);

        for (int i = 0; i < MODE_ORDER.length; i++) {
            var mode = MODE_ORDER[i];
            int tabX = x + RecipeTerminalMenu.MODE_TABS_X;
            int tabY = y + RecipeTerminalMenu.MODE_TABS_Y + i * 21;
            var backdrop = mode == this.encodingMode ? Icon.HORIZONTAL_TAB_SELECTED : Icon.HORIZONTAL_TAB;
            if (this.isInside(mouseX, mouseY, RecipeTerminalMenu.MODE_TABS_X, RecipeTerminalMenu.MODE_TABS_Y + i * 21, 22, 22)) {
                backdrop = Icon.HORIZONTAL_TAB_FOCUS;
            }
            backdrop.getBlitter().dest(tabX, tabY).blit(guiGraphics);
            this.getModeIcon(mode).getBlitter().dest(tabX + 3, tabY + 2).blit(guiGraphics);
        }

        if (this.encodingMode == EncodingMode.CRAFTING) {
            this.renderEncodingResult(guiGraphics, this.getCraftingResult(), x, y, CRAFTING_RESULT_X, CRAFTING_RESULT_Y, mouseX, mouseY);
        } else if (this.encodingMode == EncodingMode.SMITHING_TABLE) {
            this.renderEncodingResult(guiGraphics, this.getSmithingResult(), x, y, SMITHING_RESULT_X, SMITHING_RESULT_Y, mouseX, mouseY);
        } else if (this.encodingMode == EncodingMode.STONECUTTING) {
            this.renderStonecuttingRecipes(guiGraphics, x, y, mouseX, mouseY);
        }

        this.drawModeScrollbar(guiGraphics, x, y);
    }

    private void renderEncodingResult(GuiGraphics guiGraphics, ItemStack result, int screenX, int screenY, int slotX, int slotY, int mouseX, int mouseY) {
        if (this.isInside(mouseX, mouseY, slotX, slotY, 18, 18)) {
            guiGraphics.fill(screenX + slotX, screenY + slotY, screenX + slotX + 16, screenY + slotY + 16, 0x66FFFFFF);
        }

        if (!result.isEmpty()) {
            guiGraphics.renderFakeItem(result, screenX + slotX, screenY + slotY);
            guiGraphics.renderItemDecorations(this.font, result, screenX + slotX, screenY + slotY);
        }
    }

    private void renderStonecuttingRecipes(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.level == null) return;

        var level = this.minecraft.level;
        var recipes = this.getStonecuttingRecipes();
        this.validateStonecuttingSelection(recipes);
        int startIndex = this.stoneScrollOffset * STONE_COLS;
        int endIndex = Math.min(recipes.size(), startIndex + STONE_COLS * STONE_ROWS);

        for (int i = startIndex; i < endIndex; i++) {
            int localIndex = i - startIndex;
            int col = localIndex % STONE_COLS;
            int row = localIndex / STONE_COLS;
            int slotX = RecipeTerminalMenu.ENCODING_X + 26 + col * 20;
            int slotY = RecipeTerminalMenu.ENCODING_Y + 12 + row * 22;
            var recipe = recipes.get(i);
            boolean selected = recipe.id().equals(this.selectedStonecuttingRecipeId);
            boolean hovered = this.isInside(mouseX, mouseY, slotX, slotY, 20, 22);

            var blitter = selected ? STONE_RECIPE_SLOT_SELECTED : hovered ? STONE_RECIPE_SLOT_HOVER : STONE_RECIPE_SLOT;
            blitter.dest(x + slotX, y + slotY).blit(guiGraphics);

            int itemY = y + slotY + (selected || hovered ? 3 : 2);
            var result = recipe.value().getResultItem(level.registryAccess());
            guiGraphics.renderFakeItem(result, x + slotX + 2, itemY);
            guiGraphics.renderItemDecorations(this.font, result, x + slotX + 2, itemY);
        }

        int maxScroll = this.getMaxStoneScroll(recipes);
        if (maxScroll > 0) {
            Icon.S_ARROW_UP.getBlitter().dest(x + RecipeTerminalMenu.ENCODING_X + 115, y + RecipeTerminalMenu.ENCODING_Y + 15).blit(guiGraphics);
            Icon.S_ARROW_DOWN.getBlitter().dest(x + RecipeTerminalMenu.ENCODING_X + 115, y + RecipeTerminalMenu.ENCODING_Y + 48).blit(guiGraphics);
        }
    }

    private void drawModeScrollbar(GuiGraphics guiGraphics, int x, int y) {
        if (this.encodingMode == EncodingMode.PROCESSING) {
            int maxScroll = 6;
            this.drawAe2Scrollbar(guiGraphics, x + RecipeTerminalMenu.ENCODING_X + 6, y + RecipeTerminalMenu.ENCODING_Y + 7, 52, this.menu.getProcessingScrollOffset(), maxScroll);
        } else if (this.encodingMode == EncodingMode.STONECUTTING) {
            int maxScroll = this.getMaxStoneScroll(this.getStonecuttingRecipes());
            if (maxScroll > 0) {
                this.drawAe2Scrollbar(guiGraphics, x + RecipeTerminalMenu.ENCODING_X + 117, y + RecipeTerminalMenu.ENCODING_Y + 12, 44, this.stoneScrollOffset, maxScroll);
            }
        }
    }

    private void drawAe2Scrollbar(GuiGraphics guiGraphics, int x, int y, int height, int value, int maxValue) {
        var enabledSprite = ResourceLocation.fromNamespaceAndPath("ae2", "small_scroller");
        var disabledSprite = ResourceLocation.fromNamespaceAndPath("ae2", "small_scroller_disabled");

        int handleHeight = 15;
        int yOffset;
        ResourceLocation sprite;
        if (maxValue == 0) {
            yOffset = 0;
            sprite = disabledSprite;
        } else {
            int availableHeight = height - handleHeight;
            yOffset = value * availableHeight / maxValue;
            sprite = enabledSprite;
        }

        Blitter.guiSprite(sprite).dest(x, y + yOffset).blit(guiGraphics);
    }

    private void drawControlButtons(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        this.drawToolbarButton(guiGraphics, x + RecipeTerminalMenu.ENCODING_X + 82, y + RecipeTerminalMenu.ENCODING_Y + 76, this.isInside(mouseX, mouseY, RecipeTerminalMenu.ENCODING_X + 82, RecipeTerminalMenu.ENCODING_Y + 76, 18, 20), Icon.CLEAR);
        this.drawToolbarButton(guiGraphics, x + RecipeTerminalMenu.ENCODING_X + 104, y + RecipeTerminalMenu.ENCODING_Y + 76, this.isInside(mouseX, mouseY, RecipeTerminalMenu.ENCODING_X + 104, RecipeTerminalMenu.ENCODING_Y + 76, 18, 20), Icon.WHITE_ARROW_DOWN);
    }

    private void drawToolbarButton(GuiGraphics guiGraphics, int x, int y, boolean hovered, Icon icon) {
        (hovered ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER : Icon.TOOLBAR_BUTTON_BACKGROUND).getBlitter().dest(x, y).blit(guiGraphics);
        icon.getBlitter().dest(x + 1, y + 2).blit(guiGraphics);
    }

    private void drawScrollButtons(GuiGraphics guiGraphics, int x, int y) {
        this.drawSmallButton(guiGraphics, x + RecipeTerminalMenu.MACHINE_LIST_X, y + 137, Icon.S_ARROW_UP);
        this.drawSmallButton(guiGraphics, x + RecipeTerminalMenu.MACHINE_LIST_X + 39, y + 137, Icon.S_ARROW_DOWN);
        this.drawSmallButton(guiGraphics, x + RecipeTerminalMenu.RECIPE_LIST_X, y + 137, Icon.S_ARROW_UP);
        this.drawSmallButton(guiGraphics, x + RecipeTerminalMenu.RECIPE_LIST_X + 43, y + 137, Icon.S_ARROW_DOWN);
    }

    private void drawSmallButton(GuiGraphics guiGraphics, int x, int y, Icon icon) {
        guiGraphics.fill(x, y, x + 35, y + 13, 0xFF9EA3B5);
        guiGraphics.fill(x, y, x + 35, y + 1, 0xFFE7E9F0);
        guiGraphics.fill(x, y, x + 1, y + 13, 0xFFE7E9F0);
        guiGraphics.fill(x + 34, y, x + 35, y + 13, 0xFF6E748A);
        guiGraphics.fill(x, y + 12, x + 35, y + 13, 0xFF6E748A);
        icon.getBlitter().dest(x + 13, y + 3).blit(guiGraphics);
    }

    private void drawInventorySlots(GuiGraphics guiGraphics, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                Icon.SLOT_BACKGROUND.getBlitter().dest(x + RecipeTerminalMenu.PLAYER_INV_X + col * 18 - 1, y + RecipeTerminalMenu.PLAYER_INV_Y + row * 18 - 1).blit(guiGraphics);
            }
        }
        for (int col = 0; col < 9; col++) {
            Icon.SLOT_BACKGROUND.getBlitter().dest(x + RecipeTerminalMenu.PLAYER_INV_X + col * 18 - 1, y + RecipeTerminalMenu.HOTBAR_Y - 1).blit(guiGraphics);
        }
    }

    private void drawRecipeItems(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        var filtered = this.getFilteredRecipes();
        for (int i = 0; i < LIST_ROWS; i++) {
            int actualIndex = i + this.recipeScrollOffset;
            if (actualIndex >= filtered.size()) continue;

            int rowY = RecipeTerminalMenu.RECIPE_LIST_Y + 4 + i * RECIPE_ROW_HEIGHT;
            boolean hovered = this.isInside(mouseX, mouseY, RecipeTerminalMenu.RECIPE_LIST_X + 2, rowY - 2, RecipeTerminalMenu.RECIPE_LIST_WIDTH - 4, 17);
            guiGraphics.fill(x + RecipeTerminalMenu.RECIPE_LIST_X + 2, y + rowY - 2, x + RecipeTerminalMenu.RECIPE_LIST_X + RecipeTerminalMenu.RECIPE_LIST_WIDTH - 2, y + rowY + 16, hovered ? 0xFFC5CAD8 : 0xFFB7BBCB);

            var recipe = filtered.get(actualIndex);
            var displayStack = recipe.outputStack().isEmpty() ? recipe.patternStack() : recipe.outputStack();
            guiGraphics.renderFakeItem(displayStack, x + RecipeTerminalMenu.RECIPE_LIST_X + 4, y + rowY - 1);
            guiGraphics.renderItemDecorations(this.font, displayStack, x + RecipeTerminalMenu.RECIPE_LIST_X + 4, y + rowY - 1);

            this.getModeIcon(recipe.mode()).getBlitter().dest(x + RecipeTerminalMenu.RECIPE_LIST_X + 24, y + rowY + 3).blit(guiGraphics);
            Icon.S_CLEAR.getBlitter().dest(x + RecipeTerminalMenu.RECIPE_LIST_X + RecipeTerminalMenu.RECIPE_LIST_WIDTH - 13, y + rowY + 4).blit(guiGraphics);
        }
    }

    private void renderMachineRows(GuiGraphics guiGraphics) {
        var groups = this.getGroups();
        for (int i = 0; i < LIST_ROWS; i++) {
            int actualIndex = i + this.groupScrollOffset;
            if (actualIndex >= groups.size()) continue;

            var group = groups.get(actualIndex);
            boolean selected = group.name().equalsIgnoreCase(this.menu.getSelectedGroup());
            int rowY = RecipeTerminalMenu.MACHINE_LIST_Y + 5 + i * MACHINE_ROW_HEIGHT;
            guiGraphics.fill(RecipeTerminalMenu.MACHINE_LIST_X + 2, rowY - 2, RecipeTerminalMenu.MACHINE_LIST_X + RecipeTerminalMenu.MACHINE_LIST_WIDTH - 2, rowY + 13, selected ? 0xFFD9DDEB : 0x00FFFFFF);

            String displayName = group.name();
            if (this.font.width(displayName) > 54) {
                displayName = this.font.plainSubstrByWidth(displayName, 50) + "..";
            }
            guiGraphics.drawString(this.font, displayName, RecipeTerminalMenu.MACHINE_LIST_X + 5, rowY, selected ? 0x27304A : 0x42475A, false);
            guiGraphics.drawString(this.font, String.valueOf(group.count()), RecipeTerminalMenu.MACHINE_LIST_X + RecipeTerminalMenu.MACHINE_LIST_WIDTH - 14, rowY, 0x656A7C, false);
        }
    }

    private void renderRecipeRows(GuiGraphics guiGraphics) {
        var filtered = this.getFilteredRecipes();
        for (int i = 0; i < LIST_ROWS; i++) {
            int actualIndex = i + this.recipeScrollOffset;
            if (actualIndex >= filtered.size()) continue;
            var recipe = filtered.get(actualIndex);
            int rowY = RecipeTerminalMenu.RECIPE_LIST_Y + 7 + i * RECIPE_ROW_HEIGHT;
            String name = recipe.outputStack().isEmpty() ? recipe.patternStack().getHoverName().getString() : recipe.outputStack().getHoverName().getString();
            if (this.font.width(name) > 30) name = this.font.plainSubstrByWidth(name, 28) + "..";
            guiGraphics.drawString(this.font, name, RecipeTerminalMenu.RECIPE_LIST_X + 42, rowY, 0x42475A, false);
        }
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = mouseX - this.leftPos;
        int y = mouseY - this.topPos;

        for (int i = 0; i < MODE_ORDER.length; i++) {
            if (this.isInside(x, y, RecipeTerminalMenu.MODE_TABS_X, RecipeTerminalMenu.MODE_TABS_Y + i * 21, 22, 22)) {
                guiGraphics.renderTooltip(this.font, this.getModeTooltip(MODE_ORDER[i]), mouseX, mouseY);
                return;
            }
        }

        if (this.isInside(x, y, RecipeTerminalMenu.ENCODING_X + 82, RecipeTerminalMenu.ENCODING_Y + 76, 18, 20)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("gui.ae2craftcore.recipe_terminal.clear"), mouseX, mouseY);
            return;
        }
        if (this.isInside(x, y, RecipeTerminalMenu.ENCODING_X + 104, RecipeTerminalMenu.ENCODING_Y + 76, 18, 20)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("gui.ae2craftcore.recipe_terminal.encode"), mouseX, mouseY);
            return;
        }

        var hoveredRecipe = this.getHoveredRecipe(x, y);
        if (hoveredRecipe != null) {
            var displayStack = hoveredRecipe.outputStack().isEmpty() ? hoveredRecipe.patternStack() : hoveredRecipe.outputStack();
            guiGraphics.renderTooltip(this.font, displayStack, mouseX, mouseY);
            return;
        }

        var encodingResult = this.getHoveredEncodingResult(x, y);
        if (!encodingResult.isEmpty()) {
            guiGraphics.renderTooltip(this.font, encodingResult, mouseX, mouseY);
            return;
        }

        var stoneRecipe = this.getStonecuttingRecipeAt(x, y);
        if (stoneRecipe != null && this.minecraft != null && this.minecraft.level != null) {
            var level = this.minecraft.level;
            guiGraphics.renderTooltip(this.font, stoneRecipe.value().getResultItem(level.registryAccess()), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (int) mouseX - this.leftPos;
        int y = (int) mouseY - this.topPos;

        if (this.encodingMode == EncodingMode.PROCESSING && button == 0) {
            int scrollbarX = RecipeTerminalMenu.ENCODING_X + 6;
            int scrollbarY = RecipeTerminalMenu.ENCODING_Y + 7;
            int scrollbarHeight = 52;
            int maxScroll = 6;

            if (this.isInside(x, y, scrollbarX - 2, scrollbarY, 11, scrollbarHeight)) {
                this.draggingScrollbar = true;
                this.activeScrollbarType = 1;

                int handleHeight = 15;
                int currentScroll = this.menu.getProcessingScrollOffset();
                int availableHeight = scrollbarHeight - handleHeight;
                int currentHandleY = currentScroll * availableHeight / maxScroll;
                int relY = y - scrollbarY;

                if (relY >= currentHandleY && relY < currentHandleY + handleHeight) {
                    this.dragYOffset = relY - currentHandleY;
                } else {
                    this.dragYOffset = handleHeight / 2.0;
                    double position = net.minecraft.util.Mth.clamp((relY - this.dragYOffset) / (double) availableHeight, 0.0, 1.0);
                    int newScroll = (int) Math.round(position * maxScroll);
                    this.menu.setProcessingScrollOffset(newScroll);
                }
                this.playClick();
                return true;
            }
        }

        if (this.encodingMode == EncodingMode.STONECUTTING && button == 0) {
            int maxScroll = this.getMaxStoneScroll(this.getStonecuttingRecipes());
            if (maxScroll > 0) {
                int scrollbarX = RecipeTerminalMenu.ENCODING_X + 117;
                int scrollbarY = RecipeTerminalMenu.ENCODING_Y + 12;
                int scrollbarHeight = 44;

                if (this.isInside(x, y, scrollbarX - 2, scrollbarY, 11, scrollbarHeight)) {
                    this.draggingScrollbar = true;
                    this.activeScrollbarType = 2;

                    int handleHeight = 15;
                    int currentScroll = this.stoneScrollOffset;
                    int availableHeight = scrollbarHeight - handleHeight;
                    int currentHandleY = currentScroll * availableHeight / maxScroll;
                    int relY = y - scrollbarY;

                    if (relY >= currentHandleY && relY < currentHandleY + handleHeight) {
                        this.dragYOffset = relY - currentHandleY;
                    } else {
                        this.dragYOffset = handleHeight / 2.0;
                        double position = Mth.clamp((relY - this.dragYOffset) / (double) availableHeight, 0.0, 1.0);
                        this.stoneScrollOffset = (int) Math.round(position * maxScroll);
                    }
                    this.playClick();
                    return true;
                }
            }
        }

        for (int i = 0; i < MODE_ORDER.length; i++) {
            if (this.isInside(x, y, RecipeTerminalMenu.MODE_TABS_X, RecipeTerminalMenu.MODE_TABS_Y + i * 21, 22, 22)) {
                this.encodingMode = MODE_ORDER[i];
                this.menu.setEncodingMode(this.encodingMode);
                this.playClick();
                return true;
            }
        }

        if (this.isInside(x, y, RecipeTerminalMenu.ENCODING_X + 82, RecipeTerminalMenu.ENCODING_Y + 76, 18, 20)) {
            this.menu.clearEncodingSlots();
            PacketDistributor.sendToServer(new RecipeTerminalClearPacket());
            this.playClick();
            return true;
        }

        if (this.isInside(x, y, RecipeTerminalMenu.ENCODING_X + 104, RecipeTerminalMenu.ENCODING_Y + 76, 18, 20)) {
            String selected = this.menu.getSelectedGroup();
            if (!selected.isEmpty()) {
                PacketDistributor.sendToServer(new RecipeTerminalSavePacket(selected, this.encodingMode, this.selectedStonecuttingRecipeId));
                this.playClick();
            }
            return true;
        }

        var stoneRecipe = this.getStonecuttingRecipeAt(x, y);
        if (this.encodingMode == EncodingMode.STONECUTTING && stoneRecipe != null) {
            this.selectedStonecuttingRecipeId = stoneRecipe.id();
            this.playClick();
            return true;
        }

        if (this.encodingMode == EncodingMode.STONECUTTING) {
            var stoneRecipes = this.getStonecuttingRecipes();
            int maxScroll = this.getMaxStoneScroll(stoneRecipes);
            if (maxScroll > 0 && this.isInside(x, y, RecipeTerminalMenu.ENCODING_X + 114, RecipeTerminalMenu.ENCODING_Y + 13, 10, 12)) {
                this.stoneScrollOffset = Math.max(0, this.stoneScrollOffset - 1);
                this.playClick();
                return true;
            }
            if (maxScroll > 0 && this.isInside(x, y, RecipeTerminalMenu.ENCODING_X + 114, RecipeTerminalMenu.ENCODING_Y + 46, 10, 12)) {
                this.stoneScrollOffset = Math.min(maxScroll, this.stoneScrollOffset + 1);
                this.playClick();
                return true;
            }
        }

        if (this.isInside(x, y, RecipeTerminalMenu.MACHINE_LIST_X, RecipeTerminalMenu.MACHINE_LIST_Y,
                RecipeTerminalMenu.MACHINE_LIST_WIDTH, RecipeTerminalMenu.MACHINE_LIST_HEIGHT)) {
            int index = (y - RecipeTerminalMenu.MACHINE_LIST_Y) / MACHINE_ROW_HEIGHT;
            var groups = this.getGroups();
            int actualIndex = index + this.groupScrollOffset;
            if (actualIndex >= 0 && actualIndex < groups.size()) {
                String clickedGroup = groups.get(actualIndex).name();
                this.menu.setSelectedGroup(clickedGroup);
                this.recipeScrollOffset = 0;
                PacketDistributor.sendToServer(new RecipeTerminalSelectGroupPacket(clickedGroup));
                this.playClick();
            }
            return true;
        }

        if (this.isInside(x, y, RecipeTerminalMenu.MACHINE_LIST_X, 137, 35, 13)) {
            if (this.groupScrollOffset > 0) {
                this.groupScrollOffset--;
                this.playClick();
            }
            return true;
        }
        if (this.isInside(x, y, RecipeTerminalMenu.MACHINE_LIST_X + 39, 137, 35, 13)) {
            var groups = this.getGroups();
            if (this.groupScrollOffset < Math.max(0, groups.size() - LIST_ROWS)) {
                this.groupScrollOffset++;
                this.playClick();
            }
            return true;
        }

        if (this.isInside(x, y, RecipeTerminalMenu.RECIPE_LIST_X, RecipeTerminalMenu.RECIPE_LIST_Y,
                RecipeTerminalMenu.RECIPE_LIST_WIDTH, RecipeTerminalMenu.RECIPE_LIST_HEIGHT)) {
            int itemIndex = (y - RecipeTerminalMenu.RECIPE_LIST_Y) / RECIPE_ROW_HEIGHT;
            var filtered = this.getFilteredRecipes();
            int actualIndex = itemIndex + this.recipeScrollOffset;
            if (actualIndex >= 0 && actualIndex < filtered.size()) {
                int rowY = RecipeTerminalMenu.RECIPE_LIST_Y + 4 + itemIndex * RECIPE_ROW_HEIGHT;
                if (this.isInside(x, y, RecipeTerminalMenu.RECIPE_LIST_X + RecipeTerminalMenu.RECIPE_LIST_WIDTH - 14, rowY + 2, 12, 12)) {
                    PacketDistributor.sendToServer(new RecipeTerminalDeleteRecipePacket(filtered.get(actualIndex).patternStack()));
                    this.playClick();
                }
            }
            return true;
        }

        if (this.isInside(x, y, RecipeTerminalMenu.RECIPE_LIST_X, 137, 35, 13)) {
            if (this.recipeScrollOffset > 0) {
                this.recipeScrollOffset--;
                this.playClick();
            }
            return true;
        }
        if (this.isInside(x, y, RecipeTerminalMenu.RECIPE_LIST_X + 43, 137, 35, 13)) {
            var filtered = this.getFilteredRecipes();
            if (this.recipeScrollOffset < Math.max(0, filtered.size() - LIST_ROWS)) {
                this.recipeScrollOffset++;
                this.playClick();
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

        if (this.encodingMode == EncodingMode.STONECUTTING && this.isInside(x, y, RecipeTerminalMenu.ENCODING_X,
                RecipeTerminalMenu.ENCODING_Y, 124, 66)) {
            int maxScroll = this.getMaxStoneScroll(this.getStonecuttingRecipes());
            int oldScroll = this.stoneScrollOffset;
            this.stoneScrollOffset = Math.clamp(this.stoneScrollOffset + direction, 0, maxScroll);
            return oldScroll != this.stoneScrollOffset;
        }

        if (this.encodingMode == EncodingMode.PROCESSING && this.isInside(x, y, RecipeTerminalMenu.ENCODING_X,
                RecipeTerminalMenu.ENCODING_Y, 124, 66)) {
            int maxScroll = 6;
            int oldScroll = this.menu.getProcessingScrollOffset();
            this.menu.setProcessingScrollOffset(Math.clamp(this.menu.getProcessingScrollOffset() + direction, 0, maxScroll));
            return oldScroll != this.menu.getProcessingScrollOffset();
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.draggingScrollbar = false;
            this.activeScrollbarType = 0;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingScrollbar && button == 0) {
            int y = (int) mouseY - this.topPos;

            if (this.activeScrollbarType == 1) {
                final int newScroll = getScroll(y);
                this.menu.setProcessingScrollOffset(newScroll);
                return true;
            } else if (this.activeScrollbarType == 2) {
                int scrollbarY = RecipeTerminalMenu.ENCODING_Y + 12;
                int scrollbarHeight = 44;
                int maxScroll = this.getMaxStoneScroll(this.getStonecuttingRecipes());
                int handleHeight = 15;

                if (maxScroll > 0) {
                    double handleUpperEdgeY = y - scrollbarY - this.dragYOffset;
                    double availableHeight = scrollbarHeight - handleHeight;
                    double position = Mth.clamp(handleUpperEdgeY / availableHeight, 0.0, 1.0);
                    this.stoneScrollOffset = (int) Math.round(position * maxScroll);
                    return true;
                }
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private int getScroll(int y) {
        int scrollbarY = RecipeTerminalMenu.ENCODING_Y + 7;
        int scrollbarHeight = 52;
        int maxScroll = 6;
        int handleHeight = 15;

        double handleUpperEdgeY = y - scrollbarY - this.dragYOffset;
        double availableHeight = scrollbarHeight - handleHeight;
        double position = Mth.clamp(handleUpperEdgeY / availableHeight, 0.0, 1.0);
        return (int) Math.round(position * maxScroll);
    }

    private void playClick() {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    private Blitter getModeBackground() {
        return switch (this.encodingMode) {
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

    private Component getModeTooltip(EncodingMode mode) {
        return switch (mode) {
            case CRAFTING -> Component.translatable("item.ae2.crafting_pattern");
            case PROCESSING -> Component.translatable("item.ae2.processing_pattern");
            case SMITHING_TABLE -> Component.translatable("item.ae2.smithing_table_pattern");
            case STONECUTTING -> Component.translatable("item.ae2.stonecutting_pattern");
        };
    }

    private boolean isInside(int x, int y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    private void clampScrollOffsets() {
        this.groupScrollOffset = Math.clamp(this.groupScrollOffset, 0, Math.max(0, this.getGroups().size() - LIST_ROWS));
        this.recipeScrollOffset = Math.clamp(this.recipeScrollOffset, 0, Math.max(0, this.getFilteredRecipes().size() - LIST_ROWS));
        this.stoneScrollOffset = Math.clamp(this.stoneScrollOffset, 0, this.getMaxStoneScroll(this.getStonecuttingRecipes()));
        this.menu.setProcessingScrollOffset(Math.clamp(this.menu.getProcessingScrollOffset(), 0, 6));
    }

    @Nullable
    private RecipeInfo getHoveredRecipe(int x, int y) {
        if (!this.isInside(x, y, RecipeTerminalMenu.RECIPE_LIST_X, RecipeTerminalMenu.RECIPE_LIST_Y,
                RecipeTerminalMenu.RECIPE_LIST_WIDTH, RecipeTerminalMenu.RECIPE_LIST_HEIGHT)) return null;
        int index = (y - RecipeTerminalMenu.RECIPE_LIST_Y) / RECIPE_ROW_HEIGHT + this.recipeScrollOffset;
        var recipes = this.getFilteredRecipes();
        return index >= 0 && index < recipes.size() ? recipes.get(index) : null;
    }

    private ItemStack getHoveredEncodingResult(int x, int y) {
        if (this.encodingMode == EncodingMode.CRAFTING && this.isInside(x, y, CRAFTING_RESULT_X,
                CRAFTING_RESULT_Y, 18, 18)) return this.getCraftingResult();
        if (this.encodingMode == EncodingMode.SMITHING_TABLE && this.isInside(x, y, SMITHING_RESULT_X,
                SMITHING_RESULT_Y, 18, 18)) return this.getSmithingResult();
        return ItemStack.EMPTY;
    }

    private ItemStack getCraftingResult() {
        if (this.minecraft == null || this.minecraft.level == null) return ItemStack.EMPTY;
        var level = this.minecraft.level;

        var grid = NonNullList.withSize(9, ItemStack.EMPTY);
        var hasInput = false;
        for (int i = 0; i < 9; i++) {
            var stack = this.menu.getPhantomContainer().getItem(i);
            if (!stack.isEmpty()) {
                var copy = stack.copy();
                copy.setCount(1);
                grid.set(i, copy);
                hasInput = true;
            }
        }
        if (!hasInput) return ItemStack.EMPTY;

        var input = CraftingInput.of(3, 3, grid);
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElse(null);
        if (recipe == null) return ItemStack.EMPTY;
        return recipe.value().assemble(input, level.registryAccess());
    }

    private ItemStack getSmithingResult() {
        if (this.minecraft == null || this.minecraft.level == null) return ItemStack.EMPTY;
        var level = this.minecraft.level;
        var template = this.menu.getPhantomContainer().getItem(0);
        var base = this.menu.getPhantomContainer().getItem(1);
        var addition = this.menu.getPhantomContainer().getItem(2);
        if (template.isEmpty() || base.isEmpty() || addition.isEmpty()) return ItemStack.EMPTY;

        var input = new SmithingRecipeInput(template, base, addition);
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, level).orElse(null);
        if (recipe == null) return ItemStack.EMPTY;
        return recipe.value().assemble(input, level.registryAccess());
    }

    private List<RecipeHolder<StonecutterRecipe>> getStonecuttingRecipes() {
        if (this.minecraft == null || this.minecraft.level == null) return List.of();
        var level = this.minecraft.level;
        var inputStack = this.menu.getPhantomContainer().getItem(0);
        if (inputStack.isEmpty()) return List.of();
        return level.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, new SingleRecipeInput(inputStack), level);
    }

    private void validateStonecuttingSelection(List<RecipeHolder<StonecutterRecipe>> recipes) {
        if (recipes.isEmpty()) {
            this.selectedStonecuttingRecipeId = null;
            this.stoneScrollOffset = 0;
            return;
        }
        boolean selectedAvailable = this.selectedStonecuttingRecipeId != null
                && recipes.stream().anyMatch(recipe -> recipe.id().equals(this.selectedStonecuttingRecipeId));
        if (!selectedAvailable) this.selectedStonecuttingRecipeId = recipes.getFirst().id();
        this.stoneScrollOffset = Math.min(this.stoneScrollOffset, this.getMaxStoneScroll(recipes));
    }

    private int getMaxStoneScroll(List<RecipeHolder<StonecutterRecipe>> recipes) {
        int rows = (recipes.size() + STONE_COLS - 1) / STONE_COLS;
        return Math.max(0, rows - STONE_ROWS);
    }

    @Nullable
    private RecipeHolder<StonecutterRecipe> getStonecuttingRecipeAt(int x, int y) {
        if (this.encodingMode != EncodingMode.STONECUTTING) return null;
        var recipes = this.getStonecuttingRecipes();
        this.validateStonecuttingSelection(recipes);
        int localX = x - (RecipeTerminalMenu.ENCODING_X + 26);
        int localY = y - (RecipeTerminalMenu.ENCODING_Y + 12);
        if (localX < 0 || localY < 0) return null;

        int col = localX / 20;
        int row = localY / 22;
        if (col >= STONE_COLS || row >= STONE_ROWS) return null;

        int index = (this.stoneScrollOffset + row) * STONE_COLS + col;
        return index >= 0 && index < recipes.size() ? recipes.get(index) : null;
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
        var customData = pattern.get(DataComponents.CUSTOM_DATA);
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

    public record GroupInfo(String name, int count) {
    }

    public record RecipeInfo(ItemStack patternStack, ItemStack outputStack, EncodingMode mode) {
    }
}