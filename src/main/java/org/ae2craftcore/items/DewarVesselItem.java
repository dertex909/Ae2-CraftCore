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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ae2craftcore.registry.AttachmentRegistry;
import org.ae2craftcore.registry.annotations.RegisterItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@RegisterItem(name = "dewar_vessel", stacksTo = 1)
public class DewarVesselItem extends Item implements IAEItemPowerStorage {

    public static DeferredHolder<Item, DewarVesselItem> DEWAR_VESSEL;

    private static final ResourceLocation HELIUM_4_LOC = ResourceLocation.fromNamespaceAndPath("ae2craftcore", "helium_4");

    private static final double MAX_POWER = 1_000_000_000.0;

    private static final Component HELIUM_4_TOOLTIP = Component.literal("§bContains Helium-4");
    private static final Component HELIUM_3_TOOLTIP = Component.literal("§bContains Helium-3");
    private static final Component EMPTY_TOOLTIP = Component.literal("§8Empty Vessel");

    private static Block cachedHelium4Block = null;

    private static Block getHelium4Block() {
        if (cachedHelium4Block == null) cachedHelium4Block = BuiltInRegistries.BLOCK.get(HELIUM_4_LOC);
        return cachedHelium4Block;
    }

    public DewarVesselItem(Properties properties) {
        super(properties);
    }

    private int getVesselState(ItemStack stack) {
        return Objects.requireNonNullElse(stack.get(AttachmentRegistry.VESSEL_STATE.get()), 0);
    }

    private double getVesselEnergy(ItemStack stack) {
        return Objects.requireNonNullElse(stack.get(AttachmentRegistry.VESSEL_ENERGY.get()), 0.0);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        var stack = player.getItemInHand(context.getHand());
        if (getVesselState(stack) != 0) return InteractionResult.PASS;

        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);

        var helium4Block = getHelium4Block();
        if (helium4Block != Blocks.AIR && state.is(helium4Block)) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, Blocks.END_STONE.defaultBlockState());
                stack.set(AttachmentRegistry.VESSEL_STATE.get(), 1);
                stack.set(AttachmentRegistry.VESSEL_ENERGY.get(), 0.0);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public double getAEMaxPower(ItemStack stack) {
        return getVesselState(stack) == 1 ? MAX_POWER : 0.0;
    }

    @Override
    public double getAECurrentPower(ItemStack stack) {
        return getVesselState(stack) == 1 ? getVesselEnergy(stack) : 0.0;
    }

    @Override
    public double injectAEPower(ItemStack stack, double amount, Actionable mode) {
        if (getVesselState(stack) != 1) return amount;

        double current = getVesselEnergy(stack);
        double space = MAX_POWER - current;
        double injected = Math.min(space, amount);

        if (mode == Actionable.MODULATE && injected > 0) {
            double next = current + injected;
            if (next >= MAX_POWER) {
                stack.set(AttachmentRegistry.VESSEL_STATE.get(), 2);
                stack.set(AttachmentRegistry.VESSEL_ENERGY.get(), 0.0);
            } else {
                stack.set(AttachmentRegistry.VESSEL_ENERGY.get(), next);
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
        return getVesselState(stack) == 1 ? AccessRestriction.WRITE : AccessRestriction.NO_ACCESS;
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return getVesselState(stack) == 1 ? 50_000_000.0 : 0.0;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        int vesselState = getVesselState(stack);

        if (vesselState == 1) {
            tooltipComponents.add(HELIUM_4_TOOLTIP);
            double current = getVesselEnergy(stack);
            tooltipComponents.add(Component.literal(String.format("§7Energy: §a%,d §7/ §a1,000,000,000 AE", (long) current)));
        } else if (vesselState == 2) {
            tooltipComponents.add(HELIUM_3_TOOLTIP);
        } else {
            tooltipComponents.add(EMPTY_TOOLTIP);
        }
    }
}