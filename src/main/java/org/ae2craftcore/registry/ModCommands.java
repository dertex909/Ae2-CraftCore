package org.ae2craftcore.registry;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.block.*;

@EventBusSubscriber(modid = Ae2craftcore.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ae2craftcore")
                        .then(Commands.literal("build_cryostat")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    var source = context.getSource();
                                    var player = source.getPlayer();
                                    if (player == null) {
                                        source.sendFailure(Component.literal("Only players can execute this command."));
                                        return 0;
                                    }

                                    var level = source.getLevel();
                                    var hitResult = player.pick(20.0D, 0.0F, false);
                                    var center = BlockPos.containing(hitResult.getLocation());

                                    if (level.getBlockState(center).isAir() && center.distManhattan(player.blockPosition()) > 10) {
                                        center = player.blockPosition().relative(player.getDirection(), 5);
                                    }

                                    buildStructure(level, center);
                                    var finalCenter = center;
                                    source.sendSuccess(() -> Component.literal("§aSuccessfully built Cryostat multiblock structure at " + finalCenter.toShortString()), true);
                                    return 1;
                                })
                        )
        );
    }

    private static void buildStructure(ServerLevel level, BlockPos center) {
        level.setBlockAndUpdate(center, CryostatBlock.HOLDER.get().defaultBlockState());

        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    int dist = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
                    var currentPos = center.offset(x, y, z);

                    switch (dist) {
                        case 1 -> {
                            boolean isFace = (Math.abs(x) == 1 && y == 0 && z == 0) || (x == 0 && Math.abs(y) == 1 && z == 0) || (x == 0 && y == 0 && Math.abs(z) == 1);
                            if (isFace) {
                                level.setBlockAndUpdate(currentPos, ShockAbsorberSpringBlock.HOLDER.get().defaultBlockState());
                            } else {
                                level.setBlockAndUpdate(currentPos, MuMetalBlock.HOLDER.get().defaultBlockState());
                            }
                        }
                        case 2 -> {
                            boolean isInjector = (y == 0) && ((x == 2 && z == 0) || (x == -2 && z == 0) || (x == 0 && z == 2) || (x == 0 && z == -2));
                            if (isInjector) {
                                level.setBlockAndUpdate(currentPos, FisInjectorBlock.HOLDER.get().defaultBlockState());
                            } else {
                                level.setBlockAndUpdate(currentPos, MuMetalBlock.HOLDER.get().defaultBlockState());
                            }
                        }
                        case 3 -> level.setBlockAndUpdate(currentPos, Blocks.AIR.defaultBlockState());
                        case 4 -> level.setBlockAndUpdate(currentPos, VacuumCasingBlock.HOLDER.get().defaultBlockState());
                    }
                }
            }
        }
    }
}