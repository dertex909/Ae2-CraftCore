package org.ae2craftcore.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.PicInjectorBlock;
import org.ae2craftcore.blocks.menu.PicInjectorMenu;
import org.ae2craftcore.items.BaseResources;
import org.ae2craftcore.multiblock.IMultiblockComponent;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisterBlockEntity(name = "pic_injector", blocks = {PicInjectorBlock.class})
public class PicInjectorBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider, IMultiblockComponent {
    public static BlockEntityType<PicInjectorBlockEntity> TYPE;

    private ItemStack itemStack = ItemStack.EMPTY;

    @Override
    public void updateMultiblockState(CryostatBlockEntity core, boolean isValid) {
        this.setChanged();
    }

    public PicInjectorBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.itemStack.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return slot == 0 ? this.itemStack : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        if (slot == 0 && !this.itemStack.isEmpty()) {
            ItemStack res = this.itemStack.split(amount);
            if (this.itemStack.isEmpty()) this.itemStack = ItemStack.EMPTY;
            this.setChanged();
            return res;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        if (slot == 0) {
            ItemStack res = this.itemStack;
            this.itemStack = ItemStack.EMPTY;
            this.setChanged();
            return res;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        if (slot == 0) {
            this.itemStack = stack;
            if (stack.getCount() > this.getMaxStackSize()) stack.setCount(this.getMaxStackSize());
            this.setChanged();
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.itemStack = ItemStack.EMPTY;
        this.setChanged();
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return slot == 0 && stack.is(BaseResources.PHOTONIC_INTEGRATED_CIRCUIT.get());
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return index == 0;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.ae2craftcore.pic_injector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new PicInjectorMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.itemStack.isEmpty()) tag.put("ItemSlot", this.itemStack.save(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ItemSlot")) {
            this.itemStack = ItemStack.parseOptional(registries, tag.getCompound("ItemSlot"));
        } else {
            this.itemStack = ItemStack.EMPTY;
        }
    }
}