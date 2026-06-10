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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.blocks.menu.LogicAssemblerMenu;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisterBlockEntity(name = "logic_assembler", blocks = {LogicAssemblerBlock.class})
public class LogicAssemblerBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static BlockEntityType<LogicAssemblerBlockEntity> TYPE;

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    private int progress = 0;
    private int maxProgress = 100;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> LogicAssemblerBlockEntity.this.progress;
                case 1 -> LogicAssemblerBlockEntity.this.maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> LogicAssemblerBlockEntity.this.progress = value;
                case 1 -> LogicAssemblerBlockEntity.this.maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public LogicAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LogicAssemblerBlockEntity blockEntity) {
        if (level.isClientSide) return;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.ae2craftcore.logic_assembler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new LogicAssemblerMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (var itemstack : this.items) if (!itemstack.isEmpty()) return false;
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        var itemstack = ContainerHelper.removeItem(this.items, slot, amount);
        if (!itemstack.isEmpty()) this.setChanged();
        return itemstack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) stack.setCount(this.getMaxStackSize());
        this.setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putInt("Progress", this.progress);
        tag.putInt("MaxProgress", this.maxProgress);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.progress = tag.getInt("Progress");
        this.maxProgress = tag.getInt("MaxProgress");
    }
}