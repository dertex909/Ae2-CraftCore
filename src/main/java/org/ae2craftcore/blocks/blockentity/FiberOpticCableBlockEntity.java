package org.ae2craftcore.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.ae2craftcore.blocks.block.FiberOpticCableBlock;
import org.ae2craftcore.blocks.block.SfpModuleBlock;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;

@RegisterBlockEntity(name = "fiber_optic_cable", blocks = {FiberOpticCableBlock.class})
public class FiberOpticCableBlockEntity extends BlockEntity {
    public static BlockEntityType<FiberOpticCableBlockEntity> TYPE;
    public static final ModelProperty<Integer> CONNECTION_MASK = new ModelProperty<>();

    private int connectionMask = 0;

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder().with(CONNECTION_MASK, this.connectionMask).build();
    }

    public FiberOpticCableBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public int getConnectionMask() {
        return connectionMask;
    }

    public void setConnectionMask(int mask) {
        this.connectionMask = mask;
    }

    public void recalculateConnections() {
        if (this.level == null) return;
        int newMask = 0;
        if (canConnect(this.level, this.worldPosition.north())) newMask |= 1;
        if (canConnect(this.level, this.worldPosition.east())) newMask |= 2;
        if (canConnect(this.level, this.worldPosition.south())) newMask |= 4;
        if (canConnect(this.level, this.worldPosition.west())) newMask |= 8;
        if (canConnect(this.level, this.worldPosition.above())) newMask |= 16;
        if (canConnect(this.level, this.worldPosition.below())) newMask |= 32;

        if (this.connectionMask != newMask) {
            this.connectionMask = newMask;
            this.setChanged();
            if (this.level.isClientSide) {
                this.requestModelDataUpdate();
            } else {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    private boolean canConnect(BlockGetter level, BlockPos neighborPos) {
        var state = level.getBlockState(neighborPos);
        return state.getBlock() instanceof FiberOpticCableBlock || state.getBlock() instanceof SfpModuleBlock;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("connections", this.connectionMask);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.connectionMask = tag.getInt("connections");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        var tag = super.getUpdateTag(registries);
        tag.putInt("connections", this.connectionMask);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, @NotNull HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        var tag = pkt.getTag();
        this.connectionMask = tag.getInt("connections");
        if (this.level != null && this.level.isClientSide) {
            this.requestModelDataUpdate();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
}