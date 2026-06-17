package org.ae2craftcore.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.CryostatBlock;
import org.ae2craftcore.blocks.menu.CryostatMenu;
import org.ae2craftcore.items.DewarVesselItem;
import org.ae2craftcore.multiblock.MultiblockValidator;
import org.ae2craftcore.registry.AutoAttachmentRegistry;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@RegisterBlockEntity(name = "cryostat", blocks = {CryostatBlock.class})
public class CryostatBlockEntity extends BlockEntity implements MenuProvider {
    public static BlockEntityType<CryostatBlockEntity> TYPE;

    private boolean structureValid = false;
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);

    private final Container container = new Container() {
        @Override
        public int getContainerSize() {
            return 4;
        }

        @Override
        public boolean isEmpty() {
            for (var itemstack : CryostatBlockEntity.this.items) if (!itemstack.isEmpty()) return false;
            return true;
        }

        @Override
        public @NotNull ItemStack getItem(int slot) {
            return CryostatBlockEntity.this.items.get(slot);
        }

        @Override
        public @NotNull ItemStack removeItem(int slot, int amount) {
            var itemstack = ContainerHelper.removeItem(CryostatBlockEntity.this.items, slot, amount);
            if (!itemstack.isEmpty()) CryostatBlockEntity.this.setChanged();
            return itemstack;
        }

        @Override
        public @NotNull ItemStack removeItemNoUpdate(int slot) {
            var itemstack = ContainerHelper.takeItem(CryostatBlockEntity.this.items, slot);
            if (!itemstack.isEmpty()) CryostatBlockEntity.this.setChanged();
            return itemstack;
        }

        @Override
        public void setItem(int slot, @NotNull ItemStack stack) {
            CryostatBlockEntity.this.items.set(slot, stack);
            if (stack.getCount() > this.getMaxStackSize()) stack.setCount(this.getMaxStackSize());
            CryostatBlockEntity.this.setChanged();
        }

        @Override
        public void setChanged() {
            CryostatBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(@NotNull Player player) {
            return Container.stillValidBlockEntity(CryostatBlockEntity.this, player);
        }

        @Override
        public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
            if (!stack.is(DewarVesselItem.DEWAR_VESSEL.get())) return false;
            int state = Objects.requireNonNullElse(stack.get(AutoAttachmentRegistry.VESSEL_STATE.get()), 0);
            if (slot == 0) {
                return state == 2;
            } else if (slot >= 1 && slot <= 3) {
                return state == 1;
            }
            return false;
        }

        @Override
        public void clearContent() {
            CryostatBlockEntity.this.items.clear();
            CryostatBlockEntity.this.setChanged();
        }
    };

    public Container getContainer() {
        return this.container;
    }

    public CryostatBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public void checkStructureAndNotify(Player player) {
        if (this.level == null) return;

        var result = MultiblockValidator.validate(this.level, this.worldPosition);
        this.structureValid = result.isValid();
        this.setChanged();

        if (this.structureValid) {
            player.sendSystemMessage(Component.literal("§a[Cryostat] Multiblock computer is launchable and ready for operation!"));
        } else {
            String relCoordStr = String.format("(%d, %d, %d)", result.relativePos().getX(), result.relativePos().getY(), result.relativePos().getZ());
            String absCoordStr = String.format("(%d, %d, %d)", result.absolutePos().getX(), result.absolutePos().getY(), result.absolutePos().getZ());
            player.sendSystemMessage(Component.literal(String.format("§c[Cryostat] Structure invalid at relative %s, absolute %s: %s",
                    relCoordStr, absCoordStr, result.errorReason())));
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.ae2craftcore.cryostat");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new CryostatMenu(containerId, playerInventory, this.getContainer());
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("StructureValid", this.structureValid);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.structureValid = tag.getBoolean("StructureValid");
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items, registries);
    }
}