package org.ae2craftcore.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.ae2craftcore.blocks.block.FiberOpticCableBlock;
import org.ae2craftcore.blocks.block.SfpModuleBlock;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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

    public void recalculateConnections() {
        if (this.level == null) return;

        var potentialDirs = new ArrayList<Direction>();
        for (var dir : Direction.values()) if (canConnectToNeighbor(dir)) potentialDirs.add(dir);

        var finalDirs = new ArrayList<Direction>();
        for (var dir : potentialDirs) {
            int dirBit = getDirectionBit(dir);
            if ((this.connectionMask & dirBit) != 0) finalDirs.add(dir);
        }

        for (var dir : potentialDirs) {
            if (finalDirs.size() >= 2) break;
            if (!finalDirs.contains(dir)) {
                if (finalDirs.size() == 1) if (dir == finalDirs.getFirst().getOpposite()) finalDirs.add(dir);
            }
        }

        for (var dir : potentialDirs) {
            if (finalDirs.size() >= 2) break;
            if (!finalDirs.contains(dir)) finalDirs.add(dir);
        }

        int newMask = 0;
        for (var dir : finalDirs) newMask |= getDirectionBit(dir);

        if (this.connectionMask != newMask) {
            this.connectionMask = newMask;
            this.setChanged();

            if (this.level.isClientSide) {
                this.requestModelDataUpdate();
            } else {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                for (var dir : Direction.values()) {
                    var neighborBe = this.level.getBlockEntity(this.worldPosition.relative(dir));
                    if (neighborBe instanceof FiberOpticCableBlockEntity nCable) nCable.recalculateConnections();
                }
            }
        }
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

    private int getDirectionBit(Direction dir) {
        return switch (dir) {
            case NORTH -> 1;
            case EAST -> 2;
            case SOUTH -> 4;
            case WEST -> 8;
            case UP -> 16;
            case DOWN -> 32;
        };
    }

    private int getOppositeBit(Direction dir) {
        return switch (dir) {
            case NORTH -> 4;
            case EAST -> 8;
            case SOUTH -> 1;
            case WEST -> 2;
            case UP -> 32;
            case DOWN -> 16;
        };
    }

    private boolean canConnectToNeighbor(Direction dir) {
        var neighborPos = this.worldPosition.relative(dir);
        if (this.level == null) return false;

        var state = this.level.getBlockState(neighborPos);
        if (!(state.getBlock() instanceof FiberOpticCableBlock || state.getBlock() instanceof SfpModuleBlock))
            return false;
        if (state.getBlock() instanceof SfpModuleBlock)
            return state.getValue(SfpModuleBlock.FACING) == dir.getOpposite();

        var be = this.level.getBlockEntity(neighborPos);
        if (be instanceof FiberOpticCableBlockEntity neighborCable) {
            int neighborMask = neighborCable.getConnectionMask();
            int neighborConnectionsCount = Integer.bitCount(neighborMask);
            if (neighborConnectionsCount < 2) return true;
            int oppositeBit = getOppositeBit(dir);
            return (neighborMask & oppositeBit) != 0;
        }

        return false;
    }
}