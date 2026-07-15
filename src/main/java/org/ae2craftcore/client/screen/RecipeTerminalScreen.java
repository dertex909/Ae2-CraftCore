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

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.StyleManager;
import appeng.core.definitions.AEItems;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.crafting.RecipeAccess;
import appeng.crafting.pattern.AEPatternDecoder;
import appeng.helpers.InventoryAction;
import appeng.parts.encoding.EncodingMode;
import appeng.util.Icon;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.mixin.AbstractContainerScreenAccessor;
import org.ae2craftcore.network.packet.*;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.*;

import static appeng.client.gui.me.common.StackSizeRenderer.renderSizeLabel;
import static java.util.Locale.ROOT;
import static org.ae2craftcore.Ae2craftcore.MODID;
import static org.ae2craftcore.network.packet.RecipeTerminalSavePacket.RECIPEMACHINEGROUP;

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

    private static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(MODID, "textures/gui/container/recipe_terminal.png");
    private static final Identifier MACHINE_ROW_TEXTURE = Identifier.fromNamespaceAndPath(MODID, "textures/gui/container/machine_row.png");

    private static final Blitter CRAFTING_BG = Blitter.texture("guis/pattern_modes.png").src(0, 0, 124, 66);
    private static final Blitter PROCESSING_BG = Blitter.texture("guis/pattern_modes.png").src(0, 70, 124, 66);
    private static final Blitter SMITHING_BG = Blitter.texture("guis/pattern_modes.png").src(128, 70, 124, 66);
    private static final Blitter STONECUTTING_BG = Blitter.texture("guis/pattern_modes.png").src(0, 140, 124, 66);
    private static final Blitter STONE_SLOT = STONECUTTING_BG.copy().src(124, 140, 20, 22);
    private static final Blitter STONE_SLOT_SELECTED = STONECUTTING_BG.copy().src(124, 162, 20, 22);
    private static final Blitter STONE_SLOT_HOVER = STONECUTTING_BG.copy().src(124, 184, 20, 22);
    private final List<GroupInfo> cachedGroups = new ArrayList<>();
    private final List<RecipeInfo> cachedFilteredRecipes = new ArrayList<>();
    private final List<RecipeHolder<StonecutterRecipe>> cachedStonecutterRecipes = new ArrayList<>();
    private int groupScrollOffset = 0;
    private int recipeScrollOffset = 0;
    private int stonecutterScrollOffset = 0;
    private boolean draggingScrollbar = false;
    private int activeScrollbarType = 0;
    private double dragYOffset = 0;
    private EditBox machineSearchBox;
    private EditBox recipeSearchBox;
    private String machineSearchQuery = "";
    private String recipeSearchQuery = "";
    private boolean groupsDirty = true;
    private boolean recipesDirty = true;
    private boolean stonecutterDirty = true;

    private int lastClientRecipesSize = -1;
    private int lastClientGroupsSize = -1;
    private ItemStack lastFirstRecipe = ItemStack.EMPTY;
    private ItemStack lastLastRecipe = ItemStack.EMPTY;
    private ItemStack lastStonecutterInput = ItemStack.EMPTY;
    private String lastSelectedGroup = "";

    public RecipeTerminalScreen(RecipeTerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StyleManager.loadStyleDoc("/screens/recipe_terminal.json"));
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = RecipeTerminalMenu.PLAYER_INV_X;
        this.inventoryLabelY = RecipeTerminalMenu.PLAYER_INV_Y - 12;

        this.machineSearchBox = new EditBox(this.font, this.leftPos + 69, this.topPos + 6, 77, 12,
                Component.translatable("gui.ae2.SearchPlaceholder"));
        this.machineSearchBox.setBordered(false);
        this.machineSearchBox.setTextColor(0xFFDDDDDD);
        this.machineSearchBox.setHint(Component.translatable("gui.ae2.SearchPlaceholder").withColor(0xFFDDDDDD));
        this.machineSearchBox.setValue(this.machineSearchQuery);
        this.machineSearchBox.setResponder(val -> {
            this.machineSearchQuery = val;
            this.groupsDirty = true;
            this.clampScrollOffsets();
        });
        this.addRenderableWidget(this.machineSearchBox);

        this.recipeSearchBox = new EditBox(this.font, this.leftPos + 234, this.topPos + 6, 77, 12,
                Component.translatable("gui.ae2.SearchPlaceholder"));
        this.recipeSearchBox.setBordered(false);
        this.recipeSearchBox.setTextColor(0xFFDDDDDD);
        this.recipeSearchBox.setHint(Component.translatable("gui.ae2.SearchPlaceholder").withColor(0xFFDDDDDD));
        this.recipeSearchBox.setValue(this.recipeSearchQuery);
        this.recipeSearchBox.setResponder(val -> {
            this.recipeSearchQuery = val;
            this.recipesDirty = true;
            this.clampScrollOffsets();
        });
        this.addRenderableWidget(this.recipeSearchBox);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.updateCachedData();
        this.clampScrollOffsets();
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractSlot(@NotNull GuiGraphicsExtractor graphics, @NotNull Slot slot, int mouseX, int mouseY) {
        if (slot instanceof RecipeTerminalMenu.RecipeTerminalPhantomSlot || slot instanceof RecipeTerminalMenu.RecipeTerminalProcessingInputSlot || slot instanceof RecipeTerminalMenu.RecipeTerminalProcessingOutputSlot) {
            var itemstack = slot.getItem();
            if (!itemstack.isEmpty()) {
                graphics.fakeItem(itemstack, slot.x, slot.y);
                int count = itemstack.getCount();
                graphics.itemDecorations(this.font, itemstack, slot.x, slot.y, count > 64 ? "" : null);
                if (count > 64) renderSizeLabel(graphics, this.font, slot.x, slot.y, this.formatStackSize(count));
                return;
            }
        }
        super.extractSlot(graphics, slot, mouseX, mouseY);
    }

    @Override
    public void drawFG(GuiGraphicsExtractor graphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x333342, false);
        graphics.text(this.font, Component.translatable("gui.ae2craftcore.recipe_terminal.machines"), RecipeTerminalMenu.MACHINE_LIST_X + 1, 6, 0xFF403E53, false);
        graphics.text(this.font, Component.translatable("gui.ae2craftcore.recipe_terminal.recipes"), RecipeTerminalMenu.RECIPE_LIST_X + 1, 6, 0xFF403E53, false);
        graphics.text(this.font, Component.translatable("gui.ae2.PatternEncoding"), RecipeTerminalMenu.ENCODING_X, this.inventoryLabelY, 0xFF403E53, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF403E53, false);

        this.renderMachineRows(graphics);
        this.renderRecipeRows(graphics);
    }

    @Override
    public void drawBG(GuiGraphicsExtractor graphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        int x = this.leftPos;
        int y = this.topPos;
        int relMouseX = mouseX - x;
        int relMouseY = mouseY - y;

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x, y, 0.0f, 0.0f, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        this.drawEncodingPanel(graphics, x, y, relMouseX, relMouseY);
        this.drawScrollbar(graphics, x + MACHINE_SCROLL_X, y + MACHINE_SCROLL_Y, this.groupScrollOffset, Math.max(0, this.cachedGroups.size() - LIST_ROWS));
        this.drawScrollbar(graphics, x + RECIPE_SCROLL_X, y + RECIPE_SCROLL_Y, this.recipeScrollOffset, Math.max(0, this.cachedFilteredRecipes.size() - LIST_ROWS));
        this.drawRecipeItems(graphics, x, y, relMouseX, relMouseY);
    }

    private void drawEncodingPanel(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
        this.getModeBackground().dest(x + RecipeTerminalMenu.ENCODING_X, y + RecipeTerminalMenu.ENCODING_Y).blit(graphics);

        var currentMode = this.menu.getEncodingMode();
        for (int i = 0; i < MODE_ORDER.length; i++) {
            var mode = MODE_ORDER[i];
            int tabX = x + TABS_X;
            int tabY = y + TABS_Y + i * TAB_STEP_Y;
            var backdrop = mode == currentMode ? Icon.HORIZONTAL_TAB_SELECTED : Icon.HORIZONTAL_TAB;
            Blitter.icon(backdrop).dest(tabX, tabY).blit(graphics);
            Blitter.icon(this.getModeIcon(mode)).dest(tabX + 3, tabY + 2).blit(graphics);
        }

        if (currentMode == EncodingMode.CRAFTING) {
            var subIcon = this.menu.substitute ? Icon.S_SUBSTITUTION_ENABLED : Icon.S_SUBSTITUTION_DISABLED;
            Blitter.icon(subIcon).dest(x + CRAFT_SUB_X, y + CRAFT_SUB_Y).blit(graphics);
            Blitter.icon(Icon.S_CLEAR).dest(x + CRAFT_CLEAR_X, y + CRAFT_CLEAR_Y).blit(graphics);
            var fluidIcon = this.menu.substituteFluids ? Icon.S_FLUID_SUBSTITUTION_ENABLED : Icon.S_FLUID_SUBSTITUTION_DISABLED;
            Blitter.icon(fluidIcon).dest(x + CRAFT_FLUID_X, y + CRAFT_FLUID_Y).blit(graphics);
        }

        if (currentMode == EncodingMode.SMITHING_TABLE) {
            var subIcon = this.menu.substitute ? Icon.S_SUBSTITUTION_ENABLED : Icon.S_SUBSTITUTION_DISABLED;
            Blitter.icon(subIcon).dest(x + SMITH_SUB_X, y + SMITH_SUB_Y).blit(graphics);
            Blitter.icon(Icon.S_CLEAR).dest(x + SMITH_CLEAR_X, y + SMITH_CLEAR_Y).blit(graphics);
        }

        if (currentMode == EncodingMode.PROCESSING) {
            Blitter.icon(Icon.S_CLEAR).dest(x + PROC_CLEAR_X, y + PROC_CLEAR_Y).blit(graphics);
            if (this.menu.canCycleProcessingOutputs()) {
                Blitter.icon(Icon.S_CYCLE).dest(x + PROC_CYCLE_X, y + PROC_CYCLE_Y).blit(graphics);
            }
            this.drawAe2Scrollbar(graphics, x + PROC_SCROLL_X, y + PROC_SCROLL_Y, this.menu.getProcessingScrollOffset());
        }

        if (currentMode == EncodingMode.STONECUTTING) {
            if (this.minecraft.level == null) return;

            var matched = this.cachedStonecutterRecipes;
            int startIndex = this.stonecutterScrollOffset << 2;
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

                blitter.dest(slotBounds.getX(), slotBounds.getY()).blit(graphics);
                var resultItem = recipe.value().assemble(new SingleRecipeInput(ItemStack.EMPTY));
                if (selected || hovered) {
                    graphics.fakeItem(resultItem, slotBounds.getX() + 2, slotBounds.getY() + 3);
                    graphics.itemDecorations(this.font, resultItem, slotBounds.getX() + 2, slotBounds.getY() + 3);
                } else {
                    graphics.fakeItem(resultItem, slotBounds.getX() + 2, slotBounds.getY() + 2);
                    graphics.itemDecorations(this.font, resultItem, slotBounds.getX() + 2, slotBounds.getY() + 2);
                }
            }

            int totalRows = (matched.size() + 3) >> 2;
            int maxScroll = Math.max(0, totalRows - 2);
            this.drawStonecutterScrollbar(graphics, x + STONE_SCROLL_X, y + STONE_SCROLL_Y, this.stonecutterScrollOffset, maxScroll);
        }
        this.drawToolbarButton(graphics, x + SAVE_X, y + SAVE_Y, this.isInside(mouseX, mouseY, SAVE_X, SAVE_Y, SAVE_W, SAVE_H));
    }

    private void drawAe2Scrollbar(GuiGraphicsExtractor graphics, int x, int y, int value) {
        var enabledSprite = Identifier.fromNamespaceAndPath("ae2", "small_scroller");
        var disabledSprite = Identifier.fromNamespaceAndPath("ae2", "small_scroller_disabled");

        int handleHeight = 15;
        int yOffset;
        Identifier sprite;
        if (PROC_SCROLL_MAX == 0) {
            yOffset = 0;
            sprite = disabledSprite;
        } else {
            int availableHeight = PROC_SCROLL_H - handleHeight;
            yOffset = value * availableHeight / PROC_SCROLL_MAX;
            sprite = enabledSprite;
        }

        Blitter.guiSprite(sprite).dest(x, y + yOffset).blit(graphics);
    }

    private void drawStonecutterScrollbar(GuiGraphicsExtractor graphics, int x, int y, int value, int maxScroll) {
        var enabledSprite = Identifier.fromNamespaceAndPath("ae2", "small_scroller");
        var disabledSprite = Identifier.fromNamespaceAndPath("ae2", "small_scroller_disabled");

        int handleHeight = 15;
        int yOffset;
        Identifier sprite;
        if (maxScroll == 0) {
            yOffset = 0;
            sprite = disabledSprite;
        } else {
            int availableHeight = STONE_SCROLL_H - handleHeight;
            yOffset = value * availableHeight / maxScroll;
            sprite = enabledSprite;
        }

        Blitter.guiSprite(sprite).dest(x, y + yOffset).blit(graphics);
    }

    private void drawToolbarButton(GuiGraphicsExtractor graphics, int x, int y, boolean hovered) {
        int yOffset = hovered ? 1 : 0;
        var bgIcon = hovered ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER : Icon.TOOLBAR_BUTTON_BACKGROUND;
        Blitter.icon(bgIcon).dest(x - 1, y + yOffset, 18, 20).blit(graphics);
        Blitter.icon(Icon.WHITE_ARROW_DOWN).dest(x, y + 1 + yOffset).blit(graphics);
    }

    private void drawScrollbar(GuiGraphicsExtractor graphics, int x, int y, int value, int maxScroll) {
        var enabledSprite = Identifier.fromNamespaceAndPath("ae2", "big_scroller");
        var disabledSprite = Identifier.fromNamespaceAndPath("ae2", "big_scroller_disabled");

        int yOffset;
        Identifier sprite;
        if (maxScroll <= 0) {
            yOffset = 0;
            sprite = disabledSprite;
        } else {
            int availableHeight = SCROLL_H - SCROLLER_HEIGHT;
            yOffset = value * availableHeight / maxScroll;
            sprite = enabledSprite;
        }

        Blitter.guiSprite(sprite).dest(x, y + yOffset, SCROLLER_WIDTH, SCROLLER_HEIGHT).blit(graphics);
    }

    private void drawRecipeItems(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
        var filtered = this.cachedFilteredRecipes;
        for (int i = 0; i < LIST_ROWS; i++) {
            int actualIndex = i + this.recipeScrollOffset;
            if (actualIndex >= filtered.size()) continue;
            int rowTop = RecipeTerminalMenu.RECIPE_LIST_Y + LIST_PADDING_TOP + i * ROW_HEIGHT;
            int bgLeft = RecipeTerminalMenu.RECIPE_LIST_X + 2;
            int bgRight = RecipeTerminalMenu.RECIPE_LIST_X + RecipeTerminalMenu.RECIPE_LIST_WIDTH - 2;
            boolean hovered = this.isInside(mouseX, mouseY, bgLeft, rowTop, bgRight - bgLeft, ROW_BG_HEIGHT);
            graphics.blit(RenderPipelines.GUI_TEXTURED, MACHINE_ROW_TEXTURE, x + bgLeft, y + rowTop, 0.0f, hovered ? 22.0f : 0.0f, 128, 22, 128, 44);

            var recipe = filtered.get(actualIndex);
            var displayStack = recipe.outputStack().isEmpty() ? recipe.patternStack() : recipe.outputStack();
            int itemY = rowTop + RECIPE_ROW_ITEM_OFFSET_Y;
            int itemX = x + RecipeTerminalMenu.RECIPE_LIST_X + 4;

            graphics.fakeItem(displayStack, itemX, y + itemY);
            int count = displayStack.getCount();
            graphics.itemDecorations(this.font, displayStack, itemX, y + itemY, count > 64 ? "" : null);
            if (count > 64) renderSizeLabel(graphics, this.font, itemX, y + itemY, this.formatStackSize(count));

            int iconY = rowTop + RECIPE_ROW_ICON_OFFSET_Y;
            Blitter.icon(this.getModeIcon(recipe.mode())).dest(x + RecipeTerminalMenu.RECIPE_LIST_X + 24, y + iconY).blit(graphics);
            int clearY = rowTop + RECIPE_ROW_CLEAR_OFFSET_Y;
            Blitter.icon(Icon.S_CLEAR).dest(x + RecipeTerminalMenu.RECIPE_LIST_X + RecipeTerminalMenu.RECIPE_LIST_WIDTH - 13, y + clearY).blit(graphics);
        }
    }

    private void renderMachineRows(GuiGraphicsExtractor graphics) {
        var groups = this.cachedGroups;
        for (int i = 0; i < LIST_ROWS; i++) {
            int actualIndex = i + this.groupScrollOffset;
            if (actualIndex >= groups.size()) continue;

            var group = groups.get(actualIndex);
            boolean selected = group.name().equalsIgnoreCase(this.menu.getSelectedGroup());
            int rowTop = RecipeTerminalMenu.MACHINE_LIST_Y + LIST_PADDING_TOP + i * ROW_HEIGHT;
            int bgLeft = RecipeTerminalMenu.MACHINE_LIST_X + 2;
            int textY = rowTop + MACHINE_ROW_TEXT_OFFSET_Y;
            graphics.blit(RenderPipelines.GUI_TEXTURED, MACHINE_ROW_TEXTURE, bgLeft, rowTop, 0.0f, selected ? 22.0f : 0.0f, 128, 22, 128, 44);

            String displayName = group.name();
            if (this.font.width(displayName) > 85) {
                displayName = this.font.plainSubstrByWidth(displayName, 80) + "...";
            }

            graphics.text(this.font, displayName, RecipeTerminalMenu.MACHINE_LIST_X + 5, textY, selected ? 0xFF272F49 : 0xFF43485B, false);

            if (!group.icon().isEmpty()) {
                graphics.fakeItem(group.icon(), bgLeft + 88, rowTop + 3);
            }

            graphics.text(this.font, String.valueOf(group.count()), bgLeft + 114, textY, selected ? 0xFF272F49 : 0xFF43485B, false);
        }
    }

    private void renderRecipeRows(GuiGraphicsExtractor graphics) {
        var filtered = this.cachedFilteredRecipes;
        for (int i = 0; i < LIST_ROWS; i++) {
            int actualIndex = i + this.recipeScrollOffset;
            if (actualIndex >= filtered.size()) continue;
            var recipe = filtered.get(actualIndex);
            int rowTop = RecipeTerminalMenu.RECIPE_LIST_Y + LIST_PADDING_TOP + i * ROW_HEIGHT;
            int textY = rowTop + RECIPE_ROW_TEXT_OFFSET_Y;

            String name = recipe.outputStack().isEmpty() ? recipe.patternStack().getHoverName().getString() : recipe.outputStack().getHoverName().getString();
            if (this.font.width(name) > 70) name = this.font.plainSubstrByWidth(name, 70) + "...";
            graphics.text(this.font, name, RecipeTerminalMenu.RECIPE_LIST_X + 42, textY, 0xFF414659, false);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop) {
        int x = (int) Math.round(mouseX) - guiLeft;
        int y = (int) Math.round(mouseY) - guiTop;
        for (int i = 0; i < MODE_ORDER.length; i++) {
            if (this.isInside(x, y, TABS_X, TABS_Y + i * TAB_STEP_Y, TAB_W, TAB_H)) return false;
        }

        if (this.isInside(x, y, SAVE_X, SAVE_Y, SAVE_W, SAVE_H)) return false;
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        if (this.machineSearchBox != null && this.recipeSearchBox != null) {
            boolean overMachine = this.machineSearchBox.isMouseOver(mouseX, mouseY);
            boolean overRecipe = this.recipeSearchBox.isMouseOver(mouseX, mouseY);
            this.machineSearchBox.setFocused(overMachine);
            this.recipeSearchBox.setFocused(overRecipe);
            if (overMachine || overRecipe) {
                this.setFocused(overMachine ? this.machineSearchBox : this.recipeSearchBox);
                if (this.machineSearchBox.mouseClicked(event, doubleClick)) return true;
                if (this.recipeSearchBox.mouseClicked(event, doubleClick)) return true;
            } else {
                this.setFocused(null);
            }
        }

        if (button == 2 || this.minecraft.options.keyPickItem.matchesMouse(event)) {
            var slot = ((AbstractContainerScreenAccessor) this).ae2craftcore$findSlot(mouseX, mouseY);
            if ((slot instanceof RecipeTerminalMenu.RecipeTerminalPhantomSlot || slot instanceof RecipeTerminalMenu.RecipeTerminalProcessingInputSlot || slot instanceof RecipeTerminalMenu.RecipeTerminalProcessingOutputSlot) && slot.isActive()) {
                var currentStack = GenericStack.fromItemStack(slot.getItem());
                if (currentStack != null) {
                    var screen = new RecipeTerminalSetAmountScreen(this, currentStack, newStack -> {
                        var message = new InventoryActionPacket(InventoryAction.SET_FILTER, slot.index, GenericStack.wrapInItemStack(newStack));
                        ClientPacketDistributor.sendToServer(message);
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
                ClientPacketDistributor.sendToServer(new RecipeTerminalUpdateSettingsPacket(!this.menu.substitute, this.menu.substituteFluids));
                this.playClick();
                return true;
            }
            if (this.isInside(x, y, CRAFT_CLEAR_X, CRAFT_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.menu.clearEncodingSlots();
                ClientPacketDistributor.sendToServer(new RecipeTerminalClearPacket());
                this.playClick();
                return true;
            }
            if (this.isInside(x, y, CRAFT_FLUID_X, CRAFT_FLUID_Y, BUTTON_MINI, BUTTON_MINI)) {
                ClientPacketDistributor.sendToServer(new RecipeTerminalUpdateSettingsPacket(this.menu.substitute, !this.menu.substituteFluids));
                this.playClick();
                return true;
            }
        }

        if (currentMode == EncodingMode.SMITHING_TABLE && button == 0) {
            if (this.isInside(x, y, SMITH_SUB_X, SMITH_SUB_Y, BUTTON_MINI, BUTTON_MINI)) {
                ClientPacketDistributor.sendToServer(new RecipeTerminalUpdateSettingsPacket(!this.menu.substitute, this.menu.substituteFluids));
                this.playClick();
                return true;
            }
            if (this.isInside(x, y, SMITH_CLEAR_X, SMITH_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.menu.clearEncodingSlots();
                ClientPacketDistributor.sendToServer(new RecipeTerminalClearPacket());
                this.playClick();
                return true;
            }
        }

        if (currentMode == EncodingMode.PROCESSING && button == 0) {
            if (this.isInside(x, y, PROC_CLEAR_X, PROC_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.menu.clearEncodingSlots();
                ClientPacketDistributor.sendToServer(new RecipeTerminalClearPacket());
                this.playClick();
                return true;
            }

            if (this.menu.canCycleProcessingOutputs() && this.isInside(x, y, PROC_CYCLE_X, PROC_CYCLE_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.menu.cycleProcessingOutputs();
                ClientPacketDistributor.sendToServer(new RecipeTerminalCycleOutputsPacket());
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
            if (this.minecraft.level == null) return true;

            var matched = this.cachedStonecutterRecipes;
            int startIndex = this.stonecutterScrollOffset << 2;
            int endIndex = startIndex + 8;

            for (int i = startIndex; i < endIndex && i < matched.size(); ++i) {
                var slotBounds = getRecipeBounds(i - startIndex, this.leftPos, this.topPos);
                if (isMouseInBounds((int) mouseX, (int) mouseY, slotBounds)) {
                    var clickedRecipe = matched.get(i);
                    var recipeKey = clickedRecipe.id();
                    this.menu.selectStonecutterRecipeOnServer(recipeKey);
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
                int totalRows = (matched.size() + 3) >> 2;
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
                ClientPacketDistributor.sendToServer(new RecipeTerminalChangeModePacket(MODE_ORDER[i]));
                this.playClick();
                return true;
            }
        }

        if (this.isInside(x, y, SAVE_X, SAVE_Y, SAVE_W, SAVE_H)) {
            String selected = this.menu.getSelectedGroup();
            if (selected.isEmpty()) selected = "Default";
            ClientPacketDistributor.sendToServer(new RecipeTerminalSavePacket(selected, this.menu.getEncodingMode(), this.menu.stonecuttingRecipeId, this.menu.substitute, this.menu.substituteFluids));
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
                    var groups = this.cachedGroups;
                    int actualIndex = index + this.groupScrollOffset;
                    if (actualIndex >= 0 && actualIndex < groups.size()) {
                        String clickedGroup = groups.get(actualIndex).name();
                        this.menu.setSelectedGroup(clickedGroup);
                        this.recipeScrollOffset = 0;
                        ClientPacketDistributor.sendToServer(new RecipeTerminalSelectGroupPacket(clickedGroup));
                        this.playClick();
                    }
                }
            }
            return true;
        }

        if (button == 0) {
            if (this.isInside(x, y, MACHINE_SCROLL_X - 2, MACHINE_SCROLL_Y, SCROLL_W + 4, SCROLL_H)) {
                int maxScroll = Math.max(0, this.cachedGroups.size() - LIST_ROWS);
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
                int maxScroll = Math.max(0, this.cachedFilteredRecipes.size() - LIST_ROWS);
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
                    var filtered = this.cachedFilteredRecipes;
                    int actualIndex = index + this.recipeScrollOffset;
                    if (actualIndex >= 0 && actualIndex < filtered.size()) {
                        int rowTop = RecipeTerminalMenu.RECIPE_LIST_Y + LIST_PADDING_TOP + index * ROW_HEIGHT;

                        int clearX = RecipeTerminalMenu.RECIPE_LIST_X + RecipeTerminalMenu.RECIPE_LIST_WIDTH - 13;
                        int clearY = rowTop + RECIPE_ROW_CLEAR_OFFSET_Y;
                        if (this.isInside(x, y, clearX, clearY, 12, 12)) {
                            ClientPacketDistributor.sendToServer(new RecipeTerminalDeleteRecipePacket(filtered.get(actualIndex).patternStack()));
                            this.playClick();
                        } else {
                            ClientPacketDistributor.sendToServer(new RecipeTerminalLoadRecipePacket(filtered.get(actualIndex).patternStack()));
                            this.playClick();
                        }
                    }
                }
            }
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int x = (int) mouseX - this.leftPos;
        int y = (int) mouseY - this.topPos;
        int direction = scrollY > 0 ? -1 : 1;

        if (this.isInside(x, y, RecipeTerminalMenu.MACHINE_LIST_X, RecipeTerminalMenu.MACHINE_LIST_Y,
                RecipeTerminalMenu.MACHINE_LIST_WIDTH, RecipeTerminalMenu.MACHINE_LIST_HEIGHT)) {
            int maxScroll = Math.max(0, this.cachedGroups.size() - LIST_ROWS);
            int oldScroll = this.groupScrollOffset;
            this.groupScrollOffset = Math.clamp(this.groupScrollOffset + direction, 0, maxScroll);
            return oldScroll != this.groupScrollOffset;
        }

        if (this.isInside(x, y, RecipeTerminalMenu.RECIPE_LIST_X, RecipeTerminalMenu.RECIPE_LIST_Y,
                RecipeTerminalMenu.RECIPE_LIST_WIDTH, RecipeTerminalMenu.RECIPE_LIST_HEIGHT)) {
            int maxScroll = Math.max(0, this.cachedFilteredRecipes.size() - LIST_ROWS);
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
            int totalRows = (this.cachedStonecutterRecipes.size() + 3) >> 2;
            int maxScroll = Math.max(0, totalRows - 2);
            int oldScroll = this.stonecutterScrollOffset;
            this.stonecutterScrollOffset = Math.clamp(this.stonecutterScrollOffset + direction, 0, maxScroll);
            return oldScroll != this.stonecutterScrollOffset;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            this.draggingScrollbar = false;
            this.activeScrollbarType = 0;
            this.setDragging(false);
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.draggingScrollbar && event.button() == 0) {
            int y = (int) event.y() - this.topPos;

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
                int totalRows = (this.cachedStonecutterRecipes.size() + 3) >> 2;
                int maxScroll = Math.max(0, totalRows - 2);
                this.stonecutterScrollOffset = maxScroll == 0 ? 0 : (int) Math.round(position * maxScroll);
                return true;
            }

            if (this.activeScrollbarType == 3) {
                double handleUpperEdgeY = y - MACHINE_SCROLL_Y - this.dragYOffset;
                double availableHeight = SCROLL_H - SCROLLER_HEIGHT;
                double position = Math.clamp(handleUpperEdgeY / availableHeight, 0.0, 1.0);
                int maxScroll = Math.max(0, this.cachedGroups.size() - LIST_ROWS);
                this.groupScrollOffset = maxScroll == 0 ? 0 : (int) Math.round(position * maxScroll);
                return true;
            }

            if (this.activeScrollbarType == 4) {
                double handleUpperEdgeY = y - RECIPE_SCROLL_Y - this.dragYOffset;
                double availableHeight = SCROLL_H - SCROLLER_HEIGHT;
                double position = Math.clamp(handleUpperEdgeY / availableHeight, 0.0, 1.0);
                int maxScroll = Math.max(0, this.cachedFilteredRecipes.size() - LIST_ROWS);
                this.recipeScrollOffset = maxScroll == 0 ? 0 : (int) Math.round(position * maxScroll);
                return true;
            }
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if ((this.machineSearchBox != null && this.machineSearchBox.isFocused()) || (this.recipeSearchBox != null && this.recipeSearchBox.isFocused())) {
            if (event.key() == 256) {
                this.setFocused(null);
                if (this.machineSearchBox.isFocused()) this.machineSearchBox.setFocused(false);
                if (this.recipeSearchBox.isFocused()) this.recipeSearchBox.setFocused(false);
                return true;
            }
            if (this.machineSearchBox.keyPressed(event)) return true;
            if (this.recipeSearchBox.keyPressed(event)) return true;
            if (this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(event))) return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent event) {
        if ((this.machineSearchBox != null && this.machineSearchBox.isFocused()) || (this.recipeSearchBox != null && this.recipeSearchBox.isFocused())) {
            return this.machineSearchBox.charTyped(event) || this.recipeSearchBox.charTyped(event) || super.charTyped(event);
        }
        return super.charTyped(event);
    }

    @Override
    protected void extractTooltip(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.hoveredSlot != null) {
            if (this.hoveredSlot instanceof RecipeTerminalMenu.RecipeTerminalPhantomSlot
                    || this.hoveredSlot instanceof RecipeTerminalMenu.RecipeTerminalProcessingInputSlot
                    || this.hoveredSlot instanceof RecipeTerminalMenu.RecipeTerminalProcessingOutputSlot) {

                if (this.hoveredSlot.hasItem()) {
                    var itemStack = this.hoveredSlot.getItem();
                    var tooltip = new ArrayList<>(this.getTooltipFromContainerItem(itemStack));

                    if (this.menu.getEncodingMode() == EncodingMode.PROCESSING) {
                        tooltip.add(Component.translatable("gui.tooltips.ae2.ModifyAmountAction", Component.translatable("gui.tooltips.ae2.MiddleClick")).withStyle(ChatFormatting.DARK_GRAY));
                    }

                    graphics.setTooltipForNextFrame(this.font, tooltip, Optional.empty(), mouseX, mouseY);
                    return;
                } else if (this.menu.getEncodingMode() == EncodingMode.PROCESSING && this.menu.isProcessingOutputSlot(this.hoveredSlot)) {
                    var tooltip = new ArrayList<Component>();
                    boolean isPrimary = this.hoveredSlot.getContainerSlot() == 0;
                    if (isPrimary) {
                        tooltip.add(Component.translatable("gui.ae2.PatternEncoding.primary_processing_result_tooltip"));
                        tooltip.add(Component.translatable("gui.ae2.PatternEncoding.primary_processing_result_hint").withStyle(ChatFormatting.DARK_GRAY));
                    } else {
                        tooltip.add(Component.translatable("gui.ae2.PatternEncoding.secondary_processing_result_tooltip"));
                        tooltip.add(Component.translatable("gui.ae2.PatternEncoding.secondary_processing_result_hint").withStyle(ChatFormatting.DARK_GRAY));
                    }
                    graphics.setTooltipForNextFrame(this.font, tooltip, Optional.empty(), mouseX, mouseY);
                    return;
                }
            }
        }

        int x = mouseX - this.leftPos;
        int y = mouseY - this.topPos;

        if (this.isInside(x, y, RecipeTerminalMenu.MACHINE_LIST_X, RecipeTerminalMenu.MACHINE_LIST_Y,
                RecipeTerminalMenu.MACHINE_LIST_WIDTH, RecipeTerminalMenu.MACHINE_LIST_HEIGHT)) {
            int relativeY = y - RecipeTerminalMenu.MACHINE_LIST_Y - LIST_PADDING_TOP;
            if (relativeY >= 0) {
                int index = relativeY / ROW_HEIGHT;
                int rowOffset = relativeY % ROW_HEIGHT;
                if (rowOffset < ROW_BG_HEIGHT) {
                    var groups = this.cachedGroups;
                    int actualIndex = index + this.groupScrollOffset;
                    if (actualIndex >= 0 && actualIndex < groups.size()) {
                        String fullName = groups.get(actualIndex).name();
                        graphics.setTooltipForNextFrame(this.font, Component.literal(fullName), mouseX, mouseY);
                        return;
                    }
                }
            }
        }

        if (this.isInside(x, y, RecipeTerminalMenu.RECIPE_LIST_X, RecipeTerminalMenu.RECIPE_LIST_Y,
                RecipeTerminalMenu.RECIPE_LIST_WIDTH, RecipeTerminalMenu.RECIPE_LIST_HEIGHT)) {
            int relativeY = y - RecipeTerminalMenu.RECIPE_LIST_Y - LIST_PADDING_TOP;
            if (relativeY >= 0) {
                int index = relativeY / ROW_HEIGHT;
                int rowOffset = relativeY % ROW_HEIGHT;
                if (rowOffset < ROW_BG_HEIGHT) {
                    var filtered = this.cachedFilteredRecipes;
                    int actualIndex = index + this.recipeScrollOffset;
                    if (actualIndex >= 0 && actualIndex < filtered.size()) {
                        var recipe = filtered.get(actualIndex);
                        var stack = recipe.outputStack().isEmpty() ? recipe.patternStack() : recipe.outputStack();
                        String fullName = stack.getHoverName().getString();
                        graphics.setTooltipForNextFrame(this.font, Component.literal(fullName), mouseX, mouseY);
                        return;
                    }
                }
            }
        }

        var currentMode = this.menu.getEncodingMode();

        if (currentMode == EncodingMode.STONECUTTING) {
            if (this.minecraft.level == null) return;
            var matched = this.cachedStonecutterRecipes;
            int startIndex = this.stonecutterScrollOffset << 2;
            int endIndex = startIndex + 8;

            for (int i = startIndex; i < endIndex && i < matched.size(); ++i) {
                var slotBounds = getRecipeBounds(i - startIndex, this.leftPos, this.topPos);
                if (isMouseInBounds(mouseX, mouseY, slotBounds)) {
                    var outputStack = matched.get(i).value().assemble(new SingleRecipeInput(ItemStack.EMPTY));
                    graphics.setTooltipForNextFrame(this.font, outputStack, mouseX, mouseY);
                    return;
                }
            }
        }
        super.extractTooltip(graphics, mouseX, mouseY);

        if (currentMode == EncodingMode.CRAFTING) {
            if (this.isInside(x, y, CRAFT_SUB_X, CRAFT_SUB_Y, BUTTON_MINI, BUTTON_MINI)) {
                var title = Component.translatable(this.menu.substitute ? "gui.tooltips.ae2.SubstitutionsOn" : "gui.tooltips.ae2.SubstitutionsOff");
                var desc = Component.translatable(this.menu.substitute ? "gui.tooltips.ae2.SubstitutionsDescEnabled" : "gui.tooltips.ae2.SubstitutionsDescDisabled");
                this.renderCustomTooltip(graphics, title, desc, mouseX, mouseY);
                return;
            }
            if (this.isInside(x, y, CRAFT_CLEAR_X, CRAFT_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.renderCustomTooltip(graphics, Component.translatable("gui.tooltips.ae2.Clear"), Component.translatable("gui.tooltips.ae2.ClearSettings"), mouseX, mouseY);
                return;
            }
            if (this.isInside(x, y, CRAFT_FLUID_X, CRAFT_FLUID_Y, BUTTON_MINI, BUTTON_MINI)) {
                var title = Component.translatable("gui.tooltips.ae2.FluidSubstitutions");
                var desc = Component.translatable(this.menu.substituteFluids ? "gui.tooltips.ae2.FluidSubstitutionsDescEnabled" : "gui.tooltips.ae2.FluidSubstitutionsDescDisabled");
                this.renderCustomTooltip(graphics, title, desc, mouseX, mouseY);
                return;
            }
        } else if (currentMode == EncodingMode.SMITHING_TABLE) {
            if (this.isInside(x, y, SMITH_SUB_X, SMITH_SUB_Y, BUTTON_MINI, BUTTON_MINI)) {
                var title = Component.translatable(this.menu.substitute ? "gui.tooltips.ae2.SubstitutionsOn" : "gui.tooltips.ae2.SubstitutionsOff");
                var desc = Component.translatable(this.menu.substitute ? "gui.tooltips.ae2.SubstitutionsDescEnabled" : "gui.tooltips.ae2.SubstitutionsDescDisabled");
                this.renderCustomTooltip(graphics, title, desc, mouseX, mouseY);
                return;
            }
            if (this.isInside(x, y, SMITH_CLEAR_X, SMITH_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.renderCustomTooltip(graphics, Component.translatable("gui.tooltips.ae2.Clear"), Component.translatable("gui.tooltips.ae2.ClearSettings"), mouseX, mouseY);
                return;
            }
        } else if (currentMode == EncodingMode.PROCESSING) {
            if (this.isInside(x, y, PROC_CLEAR_X, PROC_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.renderCustomTooltip(graphics, Component.translatable("gui.tooltips.ae2.Clear"), Component.translatable("gui.tooltips.ae2.ClearSettings"), mouseX, mouseY);
                return;
            }
            if (this.menu.canCycleProcessingOutputs() && this.isInside(x, y, PROC_CYCLE_X, PROC_CYCLE_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.renderCustomTooltip(graphics, Component.translatable("gui.tooltips.ae2.CycleProcessingOutput"), Component.translatable("gui.tooltips.ae2.CycleProcessingOutputTooltip"), mouseX, mouseY);
                return;
            }
        }

        for (int i = 0; i < MODE_ORDER.length; i++) {
            if (this.isInside(x, y, TABS_X, TABS_Y + i * TAB_STEP_Y, TAB_W, TAB_H)) {
                graphics.setTooltipForNextFrame(this.font, this.getModeTooltip(MODE_ORDER[i]), mouseX, mouseY);
                return;
            }
        }

        if (this.isInside(x, y, SAVE_X, SAVE_Y, SAVE_W, SAVE_H)) {
            var title = Component.translatable("gui.tooltips.ae2.Encode");
            this.renderCustomTooltip(graphics, title, Component.nullToEmpty(null), mouseX, mouseY);
        }
    }

    private void renderCustomTooltip(GuiGraphicsExtractor graphics, Component title, Component desc, int mouseX, int mouseY) {
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

        graphics.setTooltipForNextFrame(this.font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
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
        if (count >= 1_000_000) {
            int temp = count / 100_000;
            return (temp % 10 == 0) ? (temp / 10) + "M" : (temp / 10.0) + "M";
        }
        if (count >= 10_000) {
            int temp = count / 100;
            return (temp % 10 == 0) ? (temp / 10) + "K" : (temp / 10.0) + "K";
        }
        return String.valueOf(count);
    }

    private void playClick() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
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
        this.groupScrollOffset = Math.clamp(this.groupScrollOffset, 0, Math.max(0, this.cachedGroups.size() - LIST_ROWS));
        this.recipeScrollOffset = Math.clamp(this.recipeScrollOffset, 0, Math.max(0, this.cachedFilteredRecipes.size() - LIST_ROWS));
    }

    private void updateCachedData() {
        var clientRecipes = this.menu.getClientRecipes();
        var clientGroups = this.menu.getClientGroups();

        var firstRecipe = clientRecipes.isEmpty() ? ItemStack.EMPTY : clientRecipes.getFirst();
        var lastRecipe = clientRecipes.isEmpty() ? ItemStack.EMPTY : clientRecipes.getLast();

        boolean clientRecipesChanged = clientRecipes.size() != this.lastClientRecipesSize
                || !ItemStack.matches(firstRecipe, this.lastFirstRecipe) || !ItemStack.matches(lastRecipe, this.lastLastRecipe);

        boolean clientGroupsChanged = clientGroups.size() != this.lastClientGroupsSize;

        if (clientRecipesChanged) {
            this.lastClientRecipesSize = clientRecipes.size();
            this.lastFirstRecipe = firstRecipe.copy();
            this.lastLastRecipe = lastRecipe.copy();
            this.groupsDirty = true;
            this.recipesDirty = true;
        }

        if (clientGroupsChanged) {
            this.lastClientGroupsSize = clientGroups.size();
            this.groupsDirty = true;
        }

        String currentGroup = this.menu.getSelectedGroup();
        if (!currentGroup.equalsIgnoreCase(this.lastSelectedGroup)) {
            this.lastSelectedGroup = currentGroup;
            this.recipesDirty = true;
        }

        ItemStack currentStonecutterInput = this.getStonecutterInputStack();
        if (!ItemStack.matches(currentStonecutterInput, this.lastStonecutterInput)) {
            this.lastStonecutterInput = currentStonecutterInput.copy();
            this.stonecutterDirty = true;
        }

        if (this.groupsDirty) this.rebuildGroups();
        if (this.recipesDirty) this.rebuildFilteredRecipes();
        if (this.stonecutterDirty) this.rebuildStonecutterRecipes();
    }

    private void rebuildGroups() {
        this.cachedGroups.clear();
        var map = new LinkedHashMap<String, Integer>();
        var iconMap = new HashMap<String, ItemStack>();

        for (var group : this.menu.getClientGroups()) {
            if (!group.name().isEmpty()) {
                map.put(group.name(), group.count());
                iconMap.put(group.name(), group.icon());
            }
        }

        boolean hasSearch = this.machineSearchQuery != null && !this.machineSearchQuery.isEmpty();
        String query = hasSearch ? this.machineSearchQuery.toLowerCase(ROOT) : "";

        map.forEach((k, v) -> {
            if (!hasSearch || k.toLowerCase(ROOT).contains(query)) {
                this.cachedGroups.add(new GroupInfo(k, v, iconMap.getOrDefault(k, ItemStack.EMPTY)));
            }
        });
        this.groupsDirty = false;
    }

    private void rebuildFilteredRecipes() {
        this.cachedFilteredRecipes.clear();
        var currentList = this.menu.getClientRecipes();
        String selected = this.menu.getSelectedGroup();

        boolean hasSearch = this.recipeSearchQuery != null && !this.recipeSearchQuery.isEmpty();
        String query = hasSearch ? this.recipeSearchQuery.toLowerCase(ROOT) : "";
        var level = this.minecraft.level;

        for (var pattern : currentList) {
            if (!this.getPatternGroup(pattern).equalsIgnoreCase(selected)) continue;

            var outputStack = ItemStack.EMPTY;
            if (level != null) {
                try {
                    var details = AEPatternDecoder.INSTANCE.decodePattern(AEItemKey.of(pattern), level);
                    if (details != null && !details.getOutputs().isEmpty()) {
                        var firstOutput = details.getOutputs().getFirst();
                        if (firstOutput.what() instanceof AEItemKey itemKey) {
                            outputStack = itemKey.toStack((int) firstOutput.amount());
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            String name = outputStack.isEmpty() ? pattern.getHoverName().getString() : outputStack.getHoverName().getString();
            if (!hasSearch || name.toLowerCase(ROOT).contains(query)) {
                this.cachedFilteredRecipes.add(new RecipeInfo(pattern, outputStack, this.getPatternMode(pattern)));
            }
        }
        this.recipesDirty = false;
    }

    private void rebuildStonecutterRecipes() {
        this.cachedStonecutterRecipes.clear();
        if (this.minecraft.level != null) {
            var inputStack = this.getStonecutterInputStack();
            if (!inputStack.isEmpty()) {
                var level = this.minecraft.level;
                var recipeInput = new SingleRecipeInput(inputStack);
                this.cachedStonecutterRecipes.addAll(RecipeAccess.getRecipesFor(level, RecipeType.STONECUTTING, recipeInput).toList());
            }
        }
        this.stonecutterDirty = false;
    }

    private ItemStack getStonecutterInputStack() {
        for (var slot : this.menu.slots) {
            if (slot instanceof RecipeTerminalMenu.RecipeTerminalPhantomSlot phantomSlot && phantomSlot.getMode() == EncodingMode.STONECUTTING) {
                return phantomSlot.getItem();
            }
        }
        return ItemStack.EMPTY;
    }

    private String getPatternGroup(ItemStack pattern) {
        var customData = pattern.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) return "";
        if (customData.contains(RECIPEMACHINEGROUP)) return customData.copyTag().getStringOr(RECIPEMACHINEGROUP, "");
        return "";
    }

    private EncodingMode getPatternMode(ItemStack pattern) {
        if (AEItems.CRAFTING_PATTERN.is(pattern)) return EncodingMode.CRAFTING;
        if (AEItems.SMITHING_TABLE_PATTERN.is(pattern)) return EncodingMode.SMITHING_TABLE;
        if (AEItems.STONECUTTING_PATTERN.is(pattern)) return EncodingMode.STONECUTTING;
        return EncodingMode.PROCESSING;
    }

    private Rect2i getRecipeBounds(int index, int screenLeft, int screenTop) {
        int col = index & 3;
        int row = index >> 2;
        int slotX = screenLeft + RecipeTerminalMenu.ENCODING_X + 27 + col * 20;
        int slotY = screenTop + RecipeTerminalMenu.ENCODING_Y + 11 + row * 22;
        return new Rect2i(slotX, slotY, 20, 22);
    }

    private boolean isMouseInBounds(int mouseX, int mouseY, Rect2i rect) {
        return mouseX >= rect.getX() && mouseX < rect.getX() + rect.getWidth() && mouseY >= rect.getY() && mouseY < rect.getY() + rect.getHeight();
    }

    private boolean selectedIdEquals(ResourceKey<Recipe<?>> selected, Object recipeId) {
        if (selected == null || recipeId == null) return false;
        return selected.equals(recipeId);
    }

    public record GroupInfo(String name, int count, ItemStack icon) {
    }

    public record RecipeInfo(ItemStack patternStack, ItemStack outputStack, EncodingMode mode) {
    }
}