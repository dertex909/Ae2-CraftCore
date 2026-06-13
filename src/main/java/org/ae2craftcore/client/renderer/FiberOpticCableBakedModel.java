package org.ae2craftcore.client.renderer;

import appeng.api.orientation.BlockOrientation;
import appeng.client.render.cablebus.QuadRotator;

import appeng.thirdparty.fabric.MeshBuilderImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.blockentity.FiberOpticCableBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FiberOpticCableBakedModel implements IDynamicBakedModel {
    private final BakedModel baseModel;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache = new List[64];

    public FiberOpticCableBakedModel(BakedModel baseModel) {
        this.baseModel = baseModel;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        if (side != null) return List.of();

        Integer maskObj = data.get(FiberOpticCableBlockEntity.CONNECTION_MASK);
        int mask = maskObj != null ? maskObj : 0;
        if (mask < 0 || mask > 63) mask = 0;

        var quads = cache[mask];
        if (quads == null) {
            quads = generateQuads(mask);
            cache[mask] = quads;
        }
        return quads;
    }

    private List<BakedQuad> generateQuads(int mask) {
        var quads = new ArrayList<BakedQuad>();
        var modelManager = Minecraft.getInstance().getModelManager();
        var sideModel = modelManager.getModel(new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "block/fiber_optic_cable_side"), "standalone"));
        var coreEndModel = modelManager.getModel(new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "block/fiber_optic_cable_core_end"), "standalone"));

        var random = RandomSource.create();

        var meshBuilder = new MeshBuilderImpl();
        var emitter = meshBuilder.getEmitter();

        for (var dir : Direction.values()) {
            boolean isConnected = switch (dir) {
                case NORTH -> (mask & 1) != 0;
                case EAST -> (mask & 2) != 0;
                case SOUTH -> (mask & 4) != 0;
                case WEST -> (mask & 8) != 0;
                case UP -> (mask & 16) != 0;
                case DOWN -> (mask & 32) != 0;
            };

            var modelToRender = isConnected ? sideModel : coreEndModel;
            if (modelToRender != modelManager.getMissingModel()) {
                var baseQuads = modelToRender.getQuads(null, null, random, ModelData.EMPTY, null);

                var upDir = dir.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP;
                var orientation = BlockOrientation.get(dir, upDir);
                var rotator = QuadRotator.get(orientation);

                for (var quad : baseQuads) {
                    emitter.fromVanilla(quad, null);
                    rotator.transform(emitter);
                    quads.add(emitter.toBakedQuad(quad.getSprite()));
                }
                for (var cullDir : Direction.values()) {
                    var culledQuads = modelToRender.getQuads(null, cullDir, random, ModelData.EMPTY, null);
                    for (var quad : culledQuads) {
                        emitter.fromVanilla(quad, null);
                        rotator.transform(emitter);
                        quads.add(emitter.toBakedQuad(quad.getSprite()));
                    }
                }
            }
        }
        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return baseModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return baseModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return baseModel.getParticleIcon(data);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return baseModel.getParticleIcon();
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return baseModel.getOverrides();
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull ItemTransforms getTransforms() {
        return baseModel.getTransforms();
    }
}