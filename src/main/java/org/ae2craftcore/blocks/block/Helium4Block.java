package org.ae2craftcore.blocks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.ae2craftcore.registry.annotations.RegisterBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static net.minecraft.core.Direction.UP;
import static net.minecraft.world.level.block.Blocks.SNOW;

@RegisterBlock(name = "helium_4", strength = 2.0f, resistance = 50.0f, sound = "stone", noOcclusion = true, requiresCorrectTool = true)
public class Helium4Block extends Block {

    private static final int[] SQRT_LUT = {0, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5};

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<Float> ZERO_EXPLOSION_RESISTANCE = Optional.of(0.0F);

    public Helium4Block(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            level.removeBlock(pos, false);
            this.explode(level, pos);
            this.spawnSnow(level, pos);
        }
        super.playerWillDestroy(level, pos, state, player);
        return state;
    }

    private void explode(Level level, BlockPos pos) {
        var vec3 = pos.getCenter();
        long targetPacked = pos.asLong();

        var explosionDamageCalculator = new ExplosionDamageCalculator() {
            @Override
            public @NotNull Optional<Float> getBlockExplosionResistance(@NotNull Explosion explosion,
                                                                        @NotNull BlockGetter blockGetter,
                                                                        BlockPos blockPos,
                                                                        @NotNull BlockState blockState,
                                                                        @NotNull FluidState fluidState) {
                return blockPos.asLong() == targetPacked && blockState.is(Helium4Block.this) ? ZERO_EXPLOSION_RESISTANCE
                        : super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState);
            }
        };

        level.explode(null, level.damageSources().badRespawnPointExplosion(vec3), explosionDamageCalculator,
                vec3, 4.0F, false, Level.ExplosionInteraction.BLOCK);
    }

    private void spawnSnow(Level level, BlockPos pos) {
        var random = level.getRandom();

        int radius = 5;
        int r2 = radius * radius;

        var spawnPos = new BlockPos.MutableBlockPos();
        var belowPos = new BlockPos.MutableBlockPos();

        int originX = pos.getX();
        int originY = pos.getY();
        int originZ = pos.getZ();

        for (int x = -radius; x <= radius; x++) {
            int x2 = x * x;
            for (int y = -radius; y <= radius; y++) {
                int x2y2 = x2 + y * y;
                if (x2y2 > r2) continue;
                int maxZ = SQRT_LUT[r2 - x2y2];

                for (int z = -maxZ; z <= maxZ; z++) {
                    if (random.nextFloat() < 0.7f) {
                        int tx = originX + x;
                        int ty = originY + y;
                        int tz = originZ + z;

                        spawnPos.set(tx, ty, tz);

                        if (!level.isLoaded(spawnPos)) continue;
                        var spawnState = level.getBlockState(spawnPos);
                        if (spawnState.isAir()) {
                            belowPos.set(tx, ty - 1, tz);
                            if (!level.isLoaded(belowPos)) continue;
                            var belowState = level.getBlockState(belowPos);
                            if (!belowState.isAir() && belowState.isFaceSturdy(level, belowPos, UP)) {
                                level.setBlockAndUpdate(spawnPos.immutable(), SNOW.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }
    }
}