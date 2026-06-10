package org.ae2craftcore.items;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.AutoAttachmentRegistry;
import org.ae2craftcore.registry.annotations.RegisterItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@RegisterItem(name = "dewar_vessel", stacksTo = 1)
public class DewarVesselItem extends Item implements IAEItemPowerStorage {

    public static DeferredHolder<Item, DewarVesselItem> DEWAR_VESSEL;

    public DewarVesselItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var player = context.getPlayer();
        var hand = context.getHand();

        if (player == null) return InteractionResult.PASS;

        var state = level.getBlockState(pos);
        var stack = player.getItemInHand(hand);

        Integer stateVal = stack.get(AutoAttachmentRegistry.VESSEL_STATE.get());
        int vesselState = stateVal != null ? stateVal : 0;

        if (vesselState == 0) {
            var helium4Loc = ResourceLocation.tryParse("ae2craftcore:helium_4");
            if (helium4Loc != null && BuiltInRegistries.BLOCK.containsKey(helium4Loc)) {
                var helium4Block = BuiltInRegistries.BLOCK.get(helium4Loc);
                if (state.is(helium4Block)) {
                    if (!level.isClientSide) {
                        level.setBlockAndUpdate(pos, Blocks.END_STONE.defaultBlockState());
                        stack.set(AutoAttachmentRegistry.VESSEL_STATE.get(), 1);
                        stack.set(AutoAttachmentRegistry.VESSEL_ENERGY.get(), 0L);
                        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public double getAEMaxPower(ItemStack stack) {
        Integer stateVal = stack.get(AutoAttachmentRegistry.VESSEL_STATE.get());
        int vesselState = stateVal != null ? stateVal : 0;
        if (vesselState == 1) return 1_000_000_000.0;
        return 0.0;
    }

    @Override
    public double getAECurrentPower(ItemStack stack) {
        Integer stateVal = stack.get(AutoAttachmentRegistry.VESSEL_STATE.get());
        int vesselState = stateVal != null ? stateVal : 0;
        if (vesselState == 1) {
            Long currentVal = stack.get(AutoAttachmentRegistry.VESSEL_ENERGY.get());
            return currentVal != null ? currentVal.doubleValue() : 0.0;
        }
        return 0.0;
    }

    @Override
    public double injectAEPower(ItemStack stack, double amount, Actionable mode) {
        Integer stateVal = stack.get(AutoAttachmentRegistry.VESSEL_STATE.get());
        int vesselState = stateVal != null ? stateVal : 0;
        if (vesselState != 1) return amount;

        Long currentVal = stack.get(AutoAttachmentRegistry.VESSEL_ENERGY.get());
        double current = currentVal != null ? currentVal.doubleValue() : 0.0;
        double max = getAEMaxPower(stack);
        double space = max - current;
        double injected = Math.min(space, amount);

        if (mode == Actionable.MODULATE && injected > 0) {
            double next = current + injected;
            if (next >= max) {
                stack.set(AutoAttachmentRegistry.VESSEL_STATE.get(), 2);
                stack.set(AutoAttachmentRegistry.VESSEL_ENERGY.get(), 0L);
            } else {
                stack.set(AutoAttachmentRegistry.VESSEL_ENERGY.get(), (long) next);
            }
        }
        return amount - injected;
    }

    @Override
    public double extractAEPower(ItemStack stack, double amount, Actionable mode) {
        return 0.0;
    }

    @Override
    public AccessRestriction getPowerFlow(ItemStack stack) {
        Integer stateVal = stack.get(AutoAttachmentRegistry.VESSEL_STATE.get());
        int vesselState = stateVal != null ? stateVal : 0;
        if (vesselState == 1) return AccessRestriction.WRITE;
        return AccessRestriction.NO_ACCESS;
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        Integer stateVal = stack.get(AutoAttachmentRegistry.VESSEL_STATE.get());
        int vesselState = stateVal != null ? stateVal : 0;
        if (vesselState == 1) return 50_000_000.0;
        return 0.0;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        Integer stateVal = stack.get(AutoAttachmentRegistry.VESSEL_STATE.get());
        int vesselState = stateVal != null ? stateVal : 0;

        if (vesselState == 1) {
            tooltipComponents.add(Component.literal("§bContains Helium-4"));
            long current = Objects.requireNonNullElse(stack.get(AutoAttachmentRegistry.VESSEL_ENERGY.get()), 0L);
            tooltipComponents.add(Component.literal(String.format("§7Energy: §a%,d §7/ §a1,000,000,000 AE", current)));
        } else if (vesselState == 2) {
            tooltipComponents.add(Component.literal("§bContains Helium-3"));
        } else {
            tooltipComponents.add(Component.literal("§8Empty Vessel"));
        }
    }
}