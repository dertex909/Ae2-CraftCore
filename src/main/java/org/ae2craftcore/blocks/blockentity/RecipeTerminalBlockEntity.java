package org.ae2craftcore.blocks.blockentity;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.RecipeTerminalBlock;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;

@RegisterBlockEntity(name = "recipe_terminal", blocks = {RecipeTerminalBlock.class})
public class RecipeTerminalBlockEntity extends AENetworkedPoweredBlockEntity implements MenuProvider {
    public static BlockEntityType<RecipeTerminalBlockEntity> TYPE;

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 0);

    private final SimpleContainer cellInventory = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            RecipeTerminalBlockEntity.this.setChanged();
        }
    };

    public RecipeTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL).setIdlePowerUsage(100);
        this.setInternalMaxPower(1000);
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return this.getBlockState().getBlock().asItem();
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    public SimpleContainer getCellInventory() {
        return this.cellInventory;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.ae2craftcore.recipe_terminal");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new RecipeTerminalMenu(containerId, playerInventory, this);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        var itemTag = this.cellInventory.getItem(0).saveOptional(registries);
        tag.put("CellItem", itemTag);
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        if (tag.contains("CellItem")) {
            this.cellInventory.setItem(0, ItemStack.parseOptional(registries, tag.getCompound("CellItem")));
        } else {
            this.cellInventory.setItem(0, ItemStack.EMPTY);
        }
    }
}
