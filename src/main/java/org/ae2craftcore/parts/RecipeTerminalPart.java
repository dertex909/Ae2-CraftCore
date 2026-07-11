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

package org.ae2craftcore.parts;

import appeng.api.parts.IPartItem;
import appeng.helpers.IPatternTerminalLogicHost;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.parts.reporting.AbstractTerminalPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;  // Добавлен импорт ValueInput
import net.minecraft.world.level.storage.ValueOutput; // Добавлен импорт ValueOutput
import org.ae2craftcore.registry.ModMenuTypes;

public class RecipeTerminalPart extends AbstractTerminalPart implements IPatternTerminalMenuHost, IPatternTerminalLogicHost {

    private final PatternEncodingLogic logic = new PatternEncodingLogic(this);

    public RecipeTerminalPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public PatternEncodingLogic getLogic() {
        return this.logic;
    }

    @Override
    public Level getLevel() {
        return super.getLevel();
    }

    @Override
    public void markForSave() {
        this.saveChanges();
    }

    @Override
    public MenuType<?> getMenuType(Player player) {
        return ModMenuTypes.RECIPE_TERMINAL.get();
    }

    @Override
    public void readFromNBT(ValueInput data) {
        super.readFromNBT(data);
        this.logic.readFromNBT(data);
    }

    @Override
    public void writeToNBT(ValueOutput data) {
        super.writeToNBT(data);
        this.logic.writeToNBT(data);
    }
}