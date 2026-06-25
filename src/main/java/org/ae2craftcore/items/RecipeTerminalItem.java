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