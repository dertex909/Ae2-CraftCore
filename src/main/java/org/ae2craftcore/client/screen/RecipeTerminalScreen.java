package org.ae2craftcore.client.screen;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.stacks.AEItemKey;
import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AEPatternDecoder;
import appeng.parts.encoding.EncodingMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.mixin.AbstractContainerScreenAccessor;
import org.ae2craftcore.network.packet.RecipeTerminalClearPacket;
import org.ae2craftcore.network.packet.RecipeTerminalDeleteRecipePacket;
import org.ae2craftcore.network.packet.RecipeTerminalSavePacket;
import org.ae2craftcore.network.packet.RecipeTerminalSelectGroupPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public class RecipeTerminalScreen extends AbstractContainerScreen<RecipeTerminalMenu> {

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
    private static final int STONE_SCROLL_Y = RecipeTerminalMenu.ENCODING_Y + 11;
    private static final int STONE_SCROLL_W = 11;
    private static final int STONE_SCROLL_H = 44;

    private static final int STONE_UP_ARROW_X = RecipeTerminalMenu.ENCODING_X + 114;
    private static final int STONE_UP_ARROW_Y = RecipeTerminalMenu.ENCODING_Y + 13;
    private static final int STONE_DN_ARROW_X = RecipeTerminalMenu.ENCODING_X + 114;
    private static final int STONE_DN_ARROW_Y = RecipeTerminalMenu.ENCODING_Y + 46;
    private static final int STONE_ARROW_W = 10;
    private static final int STONE_ARROW_H = 12;

    private static final int STONE_GRID_X = RecipeTerminalMenu.ENCODING_X + 26;
    private static final int STONE_GRID_Y = RecipeTerminalMenu.ENCODING_Y + 12;
    private static final int STONE_SLOT_W = 20;
    private static final int STONE_SLOT_H = 22;

    private static final int SCROLL_BTN_Y = 131;
    private static final int SCROLL_BTN_W = 35;
    private static final int SCROLL_BTN_H = 13;
    private static final int MACHINE_UP_X = 41;
    private static final int MACHINE_DN_X = 80;
    private static final int RECIPE_UP_X = 198;
    private static final int RECIPE_DN_X = 237;

    private static final int LIST_ROWS = 6;
    private static final int MACHINE_ROW_HEIGHT = 17;
    private static final int RECIPE_ROW_HEIGHT = 18;
    private static final int STONE_COLS = 4;
    private static final int STONE_ROWS = 2;

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
    private static final int BUTTON_MINI = 8;

    private static final EncodingMode[] MODE_ORDER = {
            EncodingMode.CRAFTING,
            EncodingMode.PROCESSING,
            EncodingMode.SMITHING_TABLE,
            EncodingMode.STONECUTTING
    };

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "textures/gui/container/recipe_terminal.png");
    private static final Blitter CRAFTING_BG = Blitter.texture("guis/pattern_modes.png").src(0, 0, 124, 66);
    private static final Blitter PROCESSING_BG = Blitter.texture("guis/pattern_modes.png").src(0, 70, 124, 66);
    private static final Blitter SMITHING_BG = Blitter.texture("guis/pattern_modes.png").src(128, 70, 124, 66);
    private static final Blitter STONECUTTING_BG = Blitter.texture("guis/pattern_modes.png").src(0, 140, 124, 66);
    private static final Blitter STONE_RECIPE_SLOT = Blitter.texture("guis/pattern_modes.png").src(124, 140, 20, 22);
    private static final Blitter STONE_RECIPE_SLOT_SELECTED = Blitter.texture("guis/pattern_modes.png").src(124, 162, 20, 22);
    private static final Blitter STONE_RECIPE_SLOT_HOVER = Blitter.texture("guis/pattern_modes.png").src(124, 184, 20, 22);

    private int groupScrollOffset = 0;
    private int recipeScrollOffset = 0;
    private int stoneScrollOffset = 0;
    private EncodingMode encodingMode = EncodingMode.PROCESSING;
    @Nullable
    private ResourceLocation selectedStonecuttingRecipeId;
    private boolean substitutionsEnabled = true;
    private boolean fluidSubstitutionsEnabled = true;

    private boolean draggingScrollbar = false;
    private int activeScrollbarType = 0;
    private double dragYOffset = 0;
    private final HashSet<Slot> drag_click = new HashSet<>();

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
        guiGraphics.drawString(this.font, Component.translatable("gui.ae2craftcore.recipe_terminal.machines"), RecipeTerminalMenu.MACHINE_LIST_X, 13, 0xFF403E53, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.ae2craftcore.recipe_terminal.recipes"), RecipeTerminalMenu.RECIPE_LIST_X, 13, 0xFF403E53, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.ae2.PatternEncoding"), RecipeTerminalMenu.ENCODING_X, this.inventoryLabelY, 0xFF403E53, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF403E53, false);

        this.renderMachineRows(guiGraphics);
        this.renderRecipeRows(guiGraphics);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int relMouseX = mouseX - x;
        int relMouseY = mouseY - y;

        guiGraphics.blit(BACKGROUND_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        this.drawEncodingPanel(guiGraphics, x, y, relMouseX, relMouseY);
        this.drawControlButton(guiGraphics, x, y, relMouseX, relMouseY);
        this.drawScrollButtons(guiGraphics, x, y);
        this.drawRecipeItems(guiGraphics, x, y, relMouseX, relMouseY);
    }

    private void drawEncodingPanel(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        this.getModeBackground().dest(x + RecipeTerminalMenu.ENCODING_X, y + RecipeTerminalMenu.ENCODING_Y).blit(guiGraphics);

        for (int i = 0; i < MODE_ORDER.length; i++) {
            var mode = MODE_ORDER[i];
            int tabX = x + TABS_X;
            int tabY = y + TABS_Y + i * TAB_STEP_Y;
            var backdrop = mode == this.encodingMode ? Icon.HORIZONTAL_TAB_SELECTED : Icon.HORIZONTAL_TAB;
            backdrop.getBlitter().dest(tabX, tabY).blit(guiGraphics);
            this.getModeIcon(mode).getBlitter().dest(tabX + 3, tabY + 2).blit(guiGraphics);
        }

        if (this.encodingMode == EncodingMode.CRAFTING) {
            var subIcon = this.substitutionsEnabled ? Icon.S_SUBSTITUTION_ENABLED : Icon.S_SUBSTITUTION_DISABLED;
            subIcon.getBlitter().dest(x + CRAFT_SUB_X, y + CRAFT_SUB_Y).blit(guiGraphics);

            Icon.S_CLEAR.getBlitter().dest(x + CRAFT_CLEAR_X, y + CRAFT_CLEAR_Y).blit(guiGraphics);

            var fluidIcon = this.fluidSubstitutionsEnabled ? Icon.S_FLUID_SUBSTITUTION_ENABLED : Icon.S_FLUID_SUBSTITUTION_DISABLED;
            fluidIcon.getBlitter().dest(x + CRAFT_FLUID_X, y + CRAFT_FLUID_Y).blit(guiGraphics);

            if (this.fluidSubstitutionsEnabled && this.isInside(mouseX, mouseY, CRAFT_FLUID_X, CRAFT_FLUID_Y, BUTTON_MINI, BUTTON_MINI)) {
                for (int i = 0; i < 9; i++) {
                    var slot = this.menu.slots.get(i);
                    if (this.supportsFluidSubstitution(slot.getItem())) {
                        int slotX = x + slot.x;
                        int slotY = y + slot.y;
                        guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xff7ac25f);
                    }
                }
            }
        }

        if (this.encodingMode == EncodingMode.SMITHING_TABLE) {
            var subIcon = this.substitutionsEnabled ? Icon.S_SUBSTITUTION_ENABLED : Icon.S_SUBSTITUTION_DISABLED;
            subIcon.getBlitter().dest(x + SMITH_SUB_X, y + SMITH_SUB_Y).blit(guiGraphics);
            Icon.S_CLEAR.getBlitter().dest(x + SMITH_CLEAR_X, y + SMITH_CLEAR_Y).blit(guiGraphics);
        }

        if (this.encodingMode == EncodingMode.PROCESSING) {
            Icon.S_CLEAR.getBlitter().dest(x + PROC_CLEAR_X, y + PROC_CLEAR_Y).blit(guiGraphics);
        }

        if (this.encodingMode == EncodingMode.STONECUTTING) {
            this.renderStonecuttingRecipes(guiGraphics, x, y, mouseX, mouseY);
        }

        this.drawModeScrollbar(guiGraphics, x, y);
    }

    @SuppressWarnings("UnstableApiUsage")
    private boolean supportsFluidSubstitution(ItemStack stack) {
        if (stack.isEmpty()) return false;
        try {
            return ContainerItemStrategies.getEmptyingAction(stack) != null;
        } catch (Exception ignored) {
            return false;
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
            int slotX = STONE_GRID_X + col * STONE_SLOT_W;
            int slotY = STONE_GRID_Y + row * STONE_SLOT_H;
            var recipe = recipes.get(i);
            boolean selected = recipe.id().equals(this.selectedStonecuttingRecipeId);
            boolean hovered = this.isInside(mouseX, mouseY, slotX, slotY, STONE_SLOT_W, STONE_SLOT_H);

            var blitter = selected ? STONE_RECIPE_SLOT_SELECTED : hovered ? STONE_RECIPE_SLOT_HOVER : STONE_RECIPE_SLOT;
            blitter.dest(x + slotX, y + slotY).blit(guiGraphics);

            int itemY = y + slotY + (selected || hovered ? 3 : 2);
            var result = recipe.value().getResultItem(level.registryAccess());
            guiGraphics.renderFakeItem(result, x + slotX + 2, itemY);
            guiGraphics.renderItemDecorations(this.font, result, x + slotX + 2, itemY);
        }
    }

    private void drawModeScrollbar(GuiGraphics guiGraphics, int x, int y) {
        if (this.encodingMode == EncodingMode.PROCESSING) {
            this.drawAe2Scrollbar(guiGraphics, x + PROC_SCROLL_X, y + PROC_SCROLL_Y, PROC_SCROLL_H, this.menu.getProcessingScrollOffset(), PROC_SCROLL_MAX);
        } else if (this.encodingMode == EncodingMode.STONECUTTING) {
            int maxScroll = this.getMaxStoneScroll(this.getStonecuttingRecipes());
            this.drawAe2Scrollbar(guiGraphics, x + STONE_SCROLL_X, y + STONE_SCROLL_Y, STONE_SCROLL_H, this.stoneScrollOffset, maxScroll);
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

    private void drawControlButton(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        this.drawToolbarButton(guiGraphics, x + SAVE_X, y + SAVE_Y, this.isInside(mouseX, mouseY, SAVE_X, SAVE_Y, SAVE_W, SAVE_H));
    }

    private void drawToolbarButton(GuiGraphics guiGraphics, int x, int y, boolean hovered) {
        int yOffset = hovered ? 1 : 0;
        var bgIcon = hovered ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER : Icon.TOOLBAR_BUTTON_BACKGROUND;
        bgIcon.getBlitter().dest(x - 1, y + yOffset, 18, 20).zOffset(2).blit(guiGraphics);
        Icon.WHITE_ARROW_DOWN.getBlitter().dest(x, y + 1 + yOffset).zOffset(3).blit(guiGraphics);
    }

    private void drawScrollButtons(GuiGraphics guiGraphics, int x, int y) {
        this.drawSmallButton(guiGraphics, x + MACHINE_UP_X, y + SCROLL_BTN_Y, Icon.S_ARROW_UP);
        this.drawSmallButton(guiGraphics, x + MACHINE_DN_X, y + SCROLL_BTN_Y, Icon.S_ARROW_DOWN);
        this.drawSmallButton(guiGraphics, x + RECIPE_UP_X, y + SCROLL_BTN_Y, Icon.S_ARROW_UP);
        this.drawSmallButton(guiGraphics, x + RECIPE_DN_X, y + SCROLL_BTN_Y, Icon.S_ARROW_DOWN);
    }

    private void drawSmallButton(GuiGraphics guiGraphics, int x, int y, Icon icon) {
        guiGraphics.fill(x, y, x + 35, y + 13, 0xFF9EA3B5);
        guiGraphics.fill(x, y, x + 35, y + 1, 0xFFE7E9F0);
        guiGraphics.fill(x, y, x + 1, y + 13, 0xFFE7E9F0);
        guiGraphics.fill(x + 34, y, x + 35, y + 13, 0xFF6E748A);
        guiGraphics.fill(x, y + 12, x + 35, y + 13, 0xFF6E748A);
        icon.getBlitter().dest(x + 13, y + 3).blit(guiGraphics);
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
            if (this.font.width(displayName) > 115) {
                displayName = this.font.plainSubstrByWidth(displayName, 110) + "..";
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
            if (this.font.width(name) > 103) name = this.font.plainSubstrByWidth(name, 98) + "..";
            guiGraphics.drawString(this.font, name, RecipeTerminalMenu.RECIPE_LIST_X + 42, rowY, 0x42475A, false);
        }
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = mouseX - this.leftPos;
        int y = mouseY - this.topPos;

        if (this.encodingMode == EncodingMode.CRAFTING) {
            if (this.isInside(x, y, CRAFT_SUB_X, CRAFT_SUB_Y, BUTTON_MINI, BUTTON_MINI)) {
                var title = Component.translatable(this.substitutionsEnabled ? "gui.tooltips.ae2.SubstitutionsOn" : "gui.tooltips.ae2.SubstitutionsOff");
                var desc = Component.translatable(this.substitutionsEnabled ? "gui.tooltips.ae2.SubstitutionsDescEnabled" : "gui.tooltips.ae2.SubstitutionsDescDisabled");
                this.renderCustomTooltip(guiGraphics, title, desc, mouseX, mouseY);
                return;
            }
            if (this.isInside(x, y, CRAFT_CLEAR_X, CRAFT_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.renderCustomTooltip(guiGraphics, Component.translatable("gui.tooltips.ae2.Clear"), Component.translatable("gui.tooltips.ae2.ClearSettings"), mouseX, mouseY);
                return;
            }
            if (this.isInside(x, y, CRAFT_FLUID_X, CRAFT_FLUID_Y, BUTTON_MINI, BUTTON_MINI)) {
                var title = Component.translatable("gui.tooltips.ae2.FluidSubstitutions");
                var desc = Component.translatable(this.fluidSubstitutionsEnabled ? "gui.tooltips.ae2.FluidSubstitutionsDescEnabled" : "gui.tooltips.ae2.FluidSubstitutionsDescDisabled");
                this.renderCustomTooltip(guiGraphics, title, desc, mouseX, mouseY);
                return;
            }
        } else if (this.encodingMode == EncodingMode.SMITHING_TABLE) {
            if (this.isInside(x, y, SMITH_SUB_X, SMITH_SUB_Y, BUTTON_MINI, BUTTON_MINI)) {
                var title = Component.translatable(this.substitutionsEnabled ? "gui.tooltips.ae2.SubstitutionsOn" : "gui.tooltips.ae2.SubstitutionsOff");
                var desc = Component.translatable(this.substitutionsEnabled ? "gui.tooltips.ae2.SubstitutionsDescEnabled" : "gui.tooltips.ae2.SubstitutionsDescDisabled");
                this.renderCustomTooltip(guiGraphics, title, desc, mouseX, mouseY);
                return;
            }
            if (this.isInside(x, y, SMITH_CLEAR_X, SMITH_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.renderCustomTooltip(guiGraphics, Component.translatable("gui.tooltips.ae2.Clear"), Component.translatable("gui.tooltips.ae2.ClearSettings"), mouseX, mouseY);
                return;
            }
        } else if (this.encodingMode == EncodingMode.PROCESSING) {
            if (this.isInside(x, y, PROC_CLEAR_X, PROC_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.renderCustomTooltip(guiGraphics, Component.translatable("gui.tooltips.ae2.Clear"), Component.translatable("gui.tooltips.ae2.ClearSettings"), mouseX, mouseY);
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
            var desc = Component.translatable("gui.tooltips.ae2.EncodeDescription");
            this.renderCustomTooltip(guiGraphics, title, desc, mouseX, mouseY);
            return;
        }

        var hoveredRecipe = this.getHoveredRecipe(x, y);
        if (hoveredRecipe != null) {
            var displayStack = hoveredRecipe.outputStack().isEmpty() ? hoveredRecipe.patternStack() : hoveredRecipe.outputStack();
            guiGraphics.renderTooltip(this.font, displayStack, mouseX, mouseY);
            return;
        }

        var stoneRecipe = this.getStonecuttingRecipeAt(x, y);
        if (stoneRecipe != null && this.minecraft != null && this.minecraft.level != null) {
            var level = this.minecraft.level;
            guiGraphics.renderTooltip(this.font, stoneRecipe.value().getResultItem(level.registryAccess()), mouseX, mouseY);
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.drag_click.clear();
        int x = (int) mouseX - this.leftPos;
        int y = (int) mouseY - this.topPos;

        if (this.encodingMode == EncodingMode.CRAFTING && button == 0) {
            if (this.isInside(x, y, CRAFT_SUB_X, CRAFT_SUB_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.substitutionsEnabled = !this.substitutionsEnabled;
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
                this.fluidSubstitutionsEnabled = !this.fluidSubstitutionsEnabled;
                this.playClick();
                return true;
            }
        }

        if (this.encodingMode == EncodingMode.SMITHING_TABLE && button == 0) {
            if (this.isInside(x, y, SMITH_SUB_X, SMITH_SUB_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.substitutionsEnabled = !this.substitutionsEnabled;
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

        if (this.encodingMode == EncodingMode.PROCESSING && button == 0) {
            if (this.isInside(x, y, PROC_CLEAR_X, PROC_CLEAR_Y, BUTTON_MINI, BUTTON_MINI)) {
                this.menu.clearEncodingSlots();
                PacketDistributor.sendToServer(new RecipeTerminalClearPacket());
                this.playClick();
                return true;
            }

            if (this.isInside(x, y, PROC_SCROLL_X - 2, PROC_SCROLL_Y, PROC_SCROLL_W, PROC_SCROLL_H)) {
                this.draggingScrollbar = true;
                this.activeScrollbarType = 1;

                int handleHeight = 15;
                int currentScroll = this.menu.getProcessingScrollOffset();
                int availableHeight = PROC_SCROLL_H - handleHeight;
                int currentHandleY = currentScroll * availableHeight / PROC_SCROLL_MAX;
                int relY = y - PROC_SCROLL_Y;

                if (relY >= currentHandleY && relY < currentHandleY + handleHeight) {
                    this.dragYOffset = relY - currentHandleY;
                } else {
                    this.dragYOffset = handleHeight / 2.0;
                    double position = Mth.clamp((relY - this.dragYOffset) / (double) availableHeight, 0.0, 1.0);
                    int newScroll = (int) Math.round(position * PROC_SCROLL_MAX);
                    this.menu.setProcessingScrollOffset(newScroll);
                }
                return true;
            }
        }

        if (this.encodingMode == EncodingMode.STONECUTTING && button == 0) {
            int maxScroll = this.getMaxStoneScroll(this.getStonecuttingRecipes());
            if (maxScroll > 0) {
                if (this.isInside(x, y, STONE_SCROLL_X - 2, STONE_SCROLL_Y, STONE_SCROLL_W, STONE_SCROLL_H)) {
                    this.draggingScrollbar = true;
                    this.activeScrollbarType = 2;

                    int handleHeight = 15;
                    int currentScroll = this.stoneScrollOffset;
                    int availableHeight = STONE_SCROLL_H - handleHeight;
                    int currentHandleY = currentScroll * availableHeight / maxScroll;
                    int relY = y - STONE_SCROLL_Y;

                    if (relY >= currentHandleY && relY < currentHandleY + handleHeight) {
                        this.dragYOffset = relY - currentHandleY;
                    } else {
                        this.dragYOffset = handleHeight / 2.0;
                        double position = Mth.clamp((relY - this.dragYOffset) / (double) availableHeight, 0.0, 1.0);
                        this.stoneScrollOffset = (int) Math.round(position * maxScroll);
                    }
                    return true;
                }
            }
        }

        for (int i = 0; i < MODE_ORDER.length; i++) {
            if (this.isInside(x, y, TABS_X, TABS_Y + i * TAB_STEP_Y, TAB_W, TAB_H)) {
                this.encodingMode = MODE_ORDER[i];
                this.menu.setEncodingMode(this.encodingMode);
                this.playClick();
                return true;
            }
        }

        if (this.isInside(x, y, SAVE_X, SAVE_Y, SAVE_W, SAVE_H)) {
            String selected = this.menu.getSelectedGroup();
            if (!selected.isEmpty()) {
                PacketDistributor.sendToServer(new RecipeTerminalSavePacket(selected, this.encodingMode, this.selectedStonecuttingRecipeId, this.substitutionsEnabled, this.fluidSubstitutionsEnabled));
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
            if (maxScroll > 0) {
                if (this.isInside(x, y, STONE_UP_ARROW_X, STONE_UP_ARROW_Y, STONE_ARROW_W, STONE_ARROW_H)) {
                    this.stoneScrollOffset = Math.max(0, this.stoneScrollOffset - 1);
                    this.playClick();
                    return true;
                }
                if (this.isInside(x, y, STONE_DN_ARROW_X, STONE_DN_ARROW_Y, STONE_ARROW_W, STONE_ARROW_H)) {
                    this.stoneScrollOffset = Math.min(maxScroll, this.stoneScrollOffset + 1);
                    this.playClick();
                    return true;
                }
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

        if (this.isInside(x, y, MACHINE_UP_X, SCROLL_BTN_Y, SCROLL_BTN_W, SCROLL_BTN_H)) {
            if (this.groupScrollOffset > 0) {
                this.groupScrollOffset--;
                this.playClick();
            }
            return true;
        }
        if (this.isInside(x, y, MACHINE_DN_X, SCROLL_BTN_Y, SCROLL_BTN_W, SCROLL_BTN_H)) {
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

        if (this.isInside(x, y, RECIPE_UP_X, SCROLL_BTN_Y, SCROLL_BTN_W, SCROLL_BTN_H)) {
            if (this.recipeScrollOffset > 0) {
                this.recipeScrollOffset--;
                this.playClick();
            }
            return true;
        }
        if (this.isInside(x, y, RECIPE_DN_X, SCROLL_BTN_Y, SCROLL_BTN_W, SCROLL_BTN_H)) {
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
    protected void slotClicked(@NotNull Slot slot, int slotId, int mouseButton, @NotNull ClickType clickType) {
        if (slot instanceof RecipeTerminalMenu.RecipePhantomSlot) {
            if (this.drag_click.size() > 1) return;
            if (this.minecraft != null && this.minecraft.gameMode != null && this.minecraft.player != null) {
                this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, slotId, mouseButton, clickType, this.minecraft.player);
            }
            return;
        }
        super.slotClicked(slot, slotId, mouseButton, clickType);
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
            int oldScroll = this.menu.getProcessingScrollOffset();
            this.menu.setProcessingScrollOffset(Math.clamp(this.menu.getProcessingScrollOffset() + direction, 0, PROC_SCROLL_MAX));
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
        this.drag_click.clear();
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
                double position = Mth.clamp(handleUpperEdgeY / availableHeight, 0.0, 1.0);
                int newScroll = (int) Math.round(position * PROC_SCROLL_MAX);
                this.menu.setProcessingScrollOffset(newScroll);
                return true;
            } else if (this.activeScrollbarType == 2) {
                int maxScroll = this.getMaxStoneScroll(this.getStonecuttingRecipes());
                int handleHeight = 15;

                if (maxScroll > 0) {
                    double handleUpperEdgeY = y - STONE_SCROLL_Y - this.dragYOffset;
                    double availableHeight = STONE_SCROLL_H - handleHeight;
                    double position = Mth.clamp(handleUpperEdgeY / availableHeight, 0.0, 1.0);
                    this.stoneScrollOffset = (int) Math.round(position * maxScroll);
                    return true;
                }
            }
        }

        var slot = ((AbstractContainerScreenAccessor) this).ae2craftcore$findSlot(mouseX, mouseY);
        var itemstack = this.menu.getCarried();
        if (slot instanceof RecipeTerminalMenu.RecipePhantomSlot && !itemstack.isEmpty()) {
            if (this.drag_click.add(slot)) {
                if (this.minecraft != null && this.minecraft.gameMode != null && this.minecraft.player != null) {
                    this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, slot.index, button, ClickType.PICKUP, this.minecraft.player);
                }
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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
        this.menu.setProcessingScrollOffset(Math.clamp(this.menu.getProcessingScrollOffset(), 0, PROC_SCROLL_MAX));
    }

    @Nullable
    private RecipeInfo getHoveredRecipe(int x, int y) {
        if (!this.isInside(x, y, RecipeTerminalMenu.RECIPE_LIST_X, RecipeTerminalMenu.RECIPE_LIST_Y,
                RecipeTerminalMenu.RECIPE_LIST_WIDTH, RecipeTerminalMenu.RECIPE_LIST_HEIGHT)) return null;
        int index = (y - RecipeTerminalMenu.RECIPE_LIST_Y) / RECIPE_ROW_HEIGHT + this.recipeScrollOffset;
        var recipes = this.getFilteredRecipes();
        return index >= 0 && index < recipes.size() ? recipes.get(index) : null;
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
        int localX = x - STONE_GRID_X;
        int localY = y - STONE_GRID_Y;
        if (localX < 0 || localY < 0) return null;

        int col = localX / STONE_SLOT_W;
        int row = localY / STONE_SLOT_H;
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