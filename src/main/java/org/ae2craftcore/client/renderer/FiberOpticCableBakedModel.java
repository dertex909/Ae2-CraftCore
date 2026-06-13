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

    private static final Direction[] DIRECTIONS = Direction.values();

    private static final int[] DIR_BITS = {32, 16, 1, 4, 8, 2};

    public static final ModelResourceLocation SIDE_MODEL_RL = new ModelResourceLocation(
            ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "block/fiber_optic_cable_side"), "standalone"
    );
    public static final ModelResourceLocation CORE_END_MODEL_RL = new ModelResourceLocation(
            ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "block/fiber_optic_cable_core_end"), "standalone"
    );

    public FiberOpticCableBakedModel(BakedModel baseModel) {
        this.baseModel = baseModel;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        if (side != null) return List.of();

        int mask;
        if (state == null) {
            mask = 5;
        } else {
            Integer maskObj = data.get(FiberOpticCableBlockEntity.CONNECTION_MASK);
            mask = maskObj != null ? maskObj : 0;
            if (mask < 0 || mask > 63) mask = 0;
        }

        var quads = cache[mask];
        if (quads == null) {
            quads = generateQuads(mask, rand);
            cache[mask] = quads;
        }
        return quads;
    }

    private List<BakedQuad> generateQuads(int mask, RandomSource random) {
        var quads = new ArrayList<BakedQuad>();
        var modelManager = Minecraft.getInstance().getModelManager();
        var sideModel = modelManager.getModel(SIDE_MODEL_RL);
        var coreEndModel = modelManager.getModel(CORE_END_MODEL_RL);

        var meshBuilder = new MeshBuilderImpl();
        var emitter = meshBuilder.getEmitter();

        for (var dir : DIRECTIONS) {
            boolean isConnected = (mask & DIR_BITS[dir.ordinal()]) != 0;

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
                for (var cullDir : DIRECTIONS) {
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