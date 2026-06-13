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

@RegisterBlockEntity(name = "fiber_optic_cable", blocks = {FiberOpticCableBlock.class})
public class FiberOpticCableBlockEntity extends BlockEntity {
    public static BlockEntityType<FiberOpticCableBlockEntity> TYPE;
    public static final ModelProperty<Integer> CONNECTION_MASK = new ModelProperty<>();

    private static final Direction[] DIRECTIONS = Direction.values();

    private static final int[] DIR_BITS = new int[6];
    private static final int[] OPPOSITE_BITS = new int[6];

    static {
        for (var dir : DIRECTIONS) {
            int ord = dir.ordinal();
            DIR_BITS[ord] = switch (dir) {
                case NORTH -> 1;
                case EAST -> 2;
                case SOUTH -> 4;
                case WEST -> 8;
                case UP -> 16;
                case DOWN -> 32;
            };
            OPPOSITE_BITS[ord] = switch (dir) {
                case NORTH -> 4;
                case EAST -> 8;
                case SOUTH -> 1;
                case WEST -> 2;
                case UP -> 32;
                case DOWN -> 16;
            };
        }
    }

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

        int potentialMask = 0;
        for (var dir : DIRECTIONS) if (canConnectToNeighbor(dir)) potentialMask |= DIR_BITS[dir.ordinal()];

        int existingMask = this.connectionMask & potentialMask;

        Direction firstDir = null;
        int newMask = 0;
        int finalCount = 0;

        for (var dir : DIRECTIONS) {
            int dirBit = DIR_BITS[dir.ordinal()];
            if ((existingMask & dirBit) != 0) {
                newMask |= dirBit;
                if (finalCount == 0) firstDir = dir;
                finalCount++;
                if (finalCount >= 2) break;
            }
        }

        if (finalCount == 1) {
            Direction opposite = firstDir.getOpposite();
            int oppositeBit = DIR_BITS[opposite.ordinal()];
            if ((potentialMask & oppositeBit) != 0) {
                newMask |= oppositeBit;
                finalCount++;
            }
        }

        if (finalCount < 2) for (var dir : DIRECTIONS) {
            int dirBit = DIR_BITS[dir.ordinal()];
            if ((potentialMask & dirBit) != 0 && (newMask & dirBit) == 0) {
                newMask |= dirBit;
                finalCount++;
                if (finalCount >= 2) break;
            }
        }

        if (this.connectionMask != newMask) {
            this.connectionMask = newMask;
            this.setChanged();

            if (this.level.isClientSide) {
                this.requestModelDataUpdate();
            } else {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                for (var dir : DIRECTIONS) {
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
            int oppositeBit = OPPOSITE_BITS[dir.ordinal()];
            return (neighborMask & oppositeBit) != 0;
        }

        return false;
    }
}