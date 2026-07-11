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

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.me.common.ClientDisplaySlot;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.TabButton;
import appeng.menu.SlotSemantics;
import com.google.common.primitives.Longs;
import net.minecraft.network.chat.Component;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;

import java.util.function.Consumer;

import static appeng.client.gui.Icon.BACK;
import static appeng.core.localization.GuiText.Set;

public class RecipeTerminalSetAmountScreen extends AESubScreen<RecipeTerminalMenu, RecipeTerminalScreen> {
    private final NumberEntryWidget amount;
    private final GenericStack currentStack;
    private final Consumer<GenericStack> setter;

    public RecipeTerminalSetAmountScreen(RecipeTerminalScreen parentScreen, GenericStack currentStack, Consumer<GenericStack> setter) {
        super(parentScreen, "/screens/set_processing_pattern_amount.json");
        this.currentStack = currentStack;
        this.setter = setter;
        widgets.addButton("save", Set.text(), this::confirm);
        var button = new TabButton(BACK, Component.translatable("item.ae2.pattern_encoding_terminal"), btn -> returnToParent());
        widgets.add("back", button);
        this.amount = widgets.addNumberEntryWidget("amountToStock", NumberEntryType.of(currentStack.what()));
        this.amount.setLongValue(currentStack.amount());
        this.amount.setMaxValue(getMaxAmount());
        this.amount.setTextFieldStyle(style.getWidget("amountToStockInput"));
        this.amount.setMinValue(0);
        this.amount.setHideValidationIcon(true);
        this.amount.setOnConfirm(this::confirm);
        addClientSideSlot(new ClientDisplaySlot(currentStack), SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    protected void init() {
        super.init();
        setSlotsHidden(SlotSemantics.TOOLBOX, true);
    }

    private void confirm() {
        this.amount.getLongValue().ifPresent(newAmount -> {
            newAmount = Longs.constrainToRange(newAmount, 0, getMaxAmount());
            if (newAmount <= 0) {
                setter.accept(null);
            } else {
                setter.accept(new GenericStack(currentStack.what(), newAmount));
            }
            returnToParent();
        });
    }

    @Override
    protected boolean shouldAddToolbar() {
        return false;
    }

    private long getMaxAmount() {
        return 999999 * (long) currentStack.what().getAmountPerUnit();
    }
}