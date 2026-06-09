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
import java.util.Random;

import static net.minecraft.core.Direction.UP;
import static net.minecraft.world.level.block.Blocks.SNOW;

@RegisterBlock(name = "helium_4", strength = 3.0f, resistance = 3.0f, sound = "stone", requiresCorrectTool = true)
public class Helium4Block extends Block {
    public Helium4Block(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide) {
            level.removeBlock(pos, false);
            this.explode(level, pos);
            this.spawnSnow(level, pos);
        }
        super.playerWillDestroy(level, pos, state, player);
        return state;
    }

    private void explode(Level level, BlockPos pos) {
        var vec3 = pos.getCenter();
        var explosionDamageCalculator = new ExplosionDamageCalculator() {
            @Override
            public @NotNull Optional<Float> getBlockExplosionResistance(@NotNull Explosion explosion,
                                                                        @NotNull BlockGetter blockGetter,
                                                                        BlockPos blockPos,
                                                                        @NotNull BlockState blockState,
                                                                        @NotNull FluidState fluidState) {
                return blockPos.equals(pos) && blockState.is(Helium4Block.this) ? Optional.of(0.0F)
                        : super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState);
            }
        };

        level.explode(null, level.damageSources().badRespawnPointExplosion(vec3), explosionDamageCalculator,
                vec3, 4.0F, false, Level.ExplosionInteraction.BLOCK);
    }

    private void spawnSnow(Level level, BlockPos pos) {
        var random = new Random();
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radius * radius) if (random.nextFloat() < 0.7f) {
                        var spawnPos = pos.offset(x, y, z);
                        var belowState = level.getBlockState(spawnPos.below());
                        if (level.getBlockState(spawnPos).isAir() && !belowState.isAir() && belowState.isFaceSturdy(level, spawnPos.below(), UP)) {
                            level.setBlockAndUpdate(spawnPos, SNOW.defaultBlockState());
                        }
                    }
                }
            }
        }
    }
}