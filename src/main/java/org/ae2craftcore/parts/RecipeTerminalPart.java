package org.ae2craftcore.parts;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.helpers.IPatternTerminalLogicHost;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.parts.reporting.AbstractTerminalPart;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.registry.ModMenuTypes;

public class RecipeTerminalPart extends AbstractTerminalPart implements IPatternTerminalMenuHost, IPatternTerminalLogicHost {

    @PartModels
    public static final ResourceLocation MODEL_OFF = ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "part/recipe_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "part/recipe_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

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
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    @Override
    public MenuType<?> getMenuType(Player player) {
        return ModMenuTypes.RECIPE_TERMINAL.get();
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.logic.readFromNBT(data, registries);
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        this.logic.writeToNBT(data, registries);
    }
}