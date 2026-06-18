package org.ae2craftcore.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.MultiblockMonitorBlock;
import org.ae2craftcore.blocks.menu.MultiblockMonitorMenu;
import org.ae2craftcore.multiblock.IMultiblockComponent;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@RegisterBlockEntity(name = "multiblock_monitor", blocks = {MultiblockMonitorBlock.class})
public class MultiblockMonitorBlockEntity extends BlockEntity implements MenuProvider, IMultiblockComponent {
    public static BlockEntityType<MultiblockMonitorBlockEntity> TYPE;

    private boolean coreStructureValid = false;
    private boolean coreInventoriesValid = false;
    private boolean coreMePowered = false;
    private final int[] corePicDurabilities = new int[]{-1, -1, -1, -1};
    private int ticksSinceLastUpdate = 0;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> MultiblockMonitorBlockEntity.this.coreStructureValid ? 1 : 0;
                case 1 -> MultiblockMonitorBlockEntity.this.coreInventoriesValid ? 1 : 0;
                case 2 -> MultiblockMonitorBlockEntity.this.coreMePowered ? 1 : 0;
                case 3 -> MultiblockMonitorBlockEntity.this.corePicDurabilities[0];
                case 4 -> MultiblockMonitorBlockEntity.this.corePicDurabilities[1];
                case 5 -> MultiblockMonitorBlockEntity.this.corePicDurabilities[2];
                case 6 -> MultiblockMonitorBlockEntity.this.corePicDurabilities[3];
                default -> -1;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public MultiblockMonitorBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    public void updateMultiblockState(CryostatBlockEntity core, boolean isValid) {
        this.coreStructureValid = isValid;
        if (isValid && core != null) {
            this.coreInventoriesValid = core.areInventoriesValid();
            this.coreMePowered = core.isMePowered();
            System.arraycopy(core.getPicDurabilities(), 0, this.corePicDurabilities, 0, 4);
        } else {
            this.coreInventoriesValid = false;
            this.coreMePowered = false;
            Arrays.fill(this.corePicDurabilities, -1);
        }
        this.ticksSinceLastUpdate = 0;
        this.setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MultiblockMonitorBlockEntity blockEntity) {
        if (level.isClientSide) return;

        blockEntity.ticksSinceLastUpdate++;
        if ((blockEntity.ticksSinceLastUpdate > 40) && (blockEntity.coreStructureValid || blockEntity.coreInventoriesValid || blockEntity.coreMePowered)) {
            blockEntity.coreStructureValid = false;
            blockEntity.coreInventoriesValid = false;
            blockEntity.coreMePowered = false;
            for (int i = 0; i < 4; i++) blockEntity.corePicDurabilities[i] = -1;
            blockEntity.setChanged();
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.coreStructureValid = tag.getBoolean("CoreStructureValid");
        this.coreInventoriesValid = tag.getBoolean("CoreInventoriesValid");
        this.coreMePowered = tag.getBoolean("CoreMePowered");
        if (tag.contains("CorePicDurabilities", 11)) {
            int[] src = tag.getIntArray("CorePicDurabilities");
            if (src.length == 4) System.arraycopy(src, 0, this.corePicDurabilities, 0, 4);
        }
        this.ticksSinceLastUpdate = tag.getInt("TicksSinceLastUpdate");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("CoreStructureValid", this.coreStructureValid);
        tag.putBoolean("CoreInventoriesValid", this.coreInventoriesValid);
        tag.putBoolean("CoreMePowered", this.coreMePowered);
        tag.putIntArray("CorePicDurabilities", this.corePicDurabilities);
        tag.putInt("TicksSinceLastUpdate", this.ticksSinceLastUpdate);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Quantum Multiblock Monitor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new MultiblockMonitorMenu(containerId, this.dataAccess, this.worldPosition);
    }
}