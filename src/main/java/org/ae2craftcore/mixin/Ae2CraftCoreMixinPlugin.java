package org.ae2craftcore.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class Ae2CraftCoreMixinPlugin implements IMixinConfigPlugin {
    private boolean isExtendedAeLoaded = false;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            isExtendedAeLoaded = this.getClass().getClassLoader().getResource("com/glodblock/github/extendedae/common/tileentities/matrix/TileAssemblerMatrixPattern.class") != null;
        } catch (Exception e) {
            isExtendedAeLoaded = false;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("extendedae")) return isExtendedAeLoaded;
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
