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

package org.ae2craftcore.items;

import appeng.items.parts.PartItem;
import org.ae2craftcore.parts.RecipeTerminalPart;
import org.ae2craftcore.registry.annotations.RegisterItem;

@RegisterItem(name = "recipe_terminal")
public class RecipeTerminalItem extends PartItem<RecipeTerminalPart> {
    public RecipeTerminalItem(Properties properties) {
        super(properties, RecipeTerminalPart.class, RecipeTerminalPart::new);
    }
}