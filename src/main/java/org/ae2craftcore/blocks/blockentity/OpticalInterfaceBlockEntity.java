package org.ae2craftcore.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.OpticalInterfaceBlock;
import org.ae2craftcore.multiblock.IMultiblockComponent;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;

@RegisterBlockEntity(name = "optical_interface", blocks = {OpticalInterfaceBlock.class})
public class OpticalInterfaceBlockEntity extends BlockEntity implements IMultiblockComponent {
    public static BlockEntityType<OpticalInterfaceBlockEntity> TYPE;

    private boolean powered = false;

    private long linkedCorePacked = 0L;
    private boolean hasLinkedCore = false;

    public OpticalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    public void updateMultiblockState(CryostatBlockEntity core, boolean isValid) {
        if (isValid && core != null) {
            this.linkedCorePacked = core.getBlockPos().asLong();
            this.hasLinkedCore = true;
        } else {
            this.linkedCorePacked = 0L;
            this.hasLinkedCore = false;
        }
        this.setChanged();
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean powered) {
        if (this.powered != powered) {
            this.powered = powered;
            this.setChanged();
            this.notifyCore();
        }
    }

    private void notifyCore() {
        if (this.level != null && !this.level.isClientSide && this.hasLinkedCore) {
            var targetPos = BlockPos.of(this.linkedCorePacked);
            if (this.level.isLoaded(targetPos)) {
                var coreBe = this.level.getBlockEntity(targetPos);
                if (coreBe instanceof CryostatBlockEntity core) core.onInterfacePowerChanged(this.powered);
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("PoweredState", this.powered);
        if (this.hasLinkedCore) tag.putLong("LinkedCorePos", this.linkedCorePacked);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.powered = tag.getBoolean("PoweredState");
        if (tag.contains("LinkedCorePos")) {
            this.linkedCorePacked = tag.getLong("LinkedCorePos");
            this.hasLinkedCore = true;
        } else {
            this.linkedCorePacked = 0L;
            this.hasLinkedCore = false;
        }
    }
}