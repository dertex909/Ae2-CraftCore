package org.ae2craftcore.blocks.blockentity;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.FiberOpticCableBlock;
import org.ae2craftcore.blocks.block.SfpModuleBlock;
import org.ae2craftcore.blocks.block.OpticalInterfaceBlock;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.util.inv.AppEngInternalInventory;

import java.util.HashSet;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import static appeng.api.orientation.RelativeSide.FRONT;

@RegisterBlockEntity(name = "sfp_module", blocks = {SfpModuleBlock.class})
public class SfpModuleBlockEntity extends AENetworkedPoweredBlockEntity {
    public static BlockEntityType<SfpModuleBlockEntity> TYPE;

    private static final Direction[] DIRECTIONS = Direction.values();

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 0);

    private int delayTicks = 100;
    private int checkTimer = 0;
    private boolean needsTrace = true;
    private boolean connectionsCreated = false;

    private final IManagedGridNode[] extraNodes = new IManagedGridNode[31];

    private static final IGridNodeListener<SfpModuleBlockEntity> MULTI_CHANNEL_LISTENER = (host, node) -> {
    };

    public void markNeedsTrace() {
        this.needsTrace = true;
        this.checkTimer = 0;
    }

    public SfpModuleBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY).setIdlePowerUsage(10);
        this.setInternalMaxPower(1000);
        for (int i = 0; i < 31; i++) {
            this.extraNodes[i] = GridHelper.createManagedNode(this, MULTI_CHANNEL_LISTENER);
            this.extraNodes[i].setFlags(GridFlags.REQUIRE_CHANNEL);
        }
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return this.getBlockState().getBlock().asItem();
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    public record TraceResult(boolean isValid, BlockPos outputPos) {
    }

    private static int getDirectionBit(Direction dir) {
        return switch (dir) {
            case NORTH -> 1;
            case EAST -> 2;
            case SOUTH -> 4;
            case WEST -> 8;
            case UP -> 16;
            case DOWN -> 32;
        };
    }

    public TraceResult traceConnection() {
        if (this.level == null) return new TraceResult(false, null);

        var visitedCables = new HashSet<BlockPos>();
        var currentPos = this.worldPosition;
        BlockPos outputPos;

        BlockPos nextPos = null;
        var blockState = getBlockState();
        if (blockState.hasProperty(SfpModuleBlock.FACING)) {
            var neighbor = currentPos.relative(blockState.getValue(SfpModuleBlock.FACING));
            var state = this.level.getBlockState(neighbor);
            if (state.getBlock() instanceof FiberOpticCableBlock) nextPos = neighbor.immutable();
        }

        if (nextPos == null) return new TraceResult(false, null);

        visitedCables.add(nextPos);

        while (true) {
            if (visitedCables.size() > 8192) return new TraceResult(false, null);
            currentPos = nextPos;
            BlockPos singleNeighbor = null;
            int neighborsCount = 0;
            BlockPos singleOutputPos = null;
            int outputCount = 0;

            var currentState = this.level.getBlockState(currentPos);
            if (!(currentState.getBlock() instanceof FiberOpticCableBlock)) return new TraceResult(false, null);
            int currentMask = currentState.getValue(FiberOpticCableBlock.CONNECTION_MASK);
            for (var d : DIRECTIONS) {
                if ((currentMask & getDirectionBit(d)) == 0) continue;
                var neighbor = currentPos.relative(d);
                if (neighbor.equals(this.worldPosition)) continue;

                var state = this.level.getBlockState(neighbor);
                if (state.getBlock() instanceof FiberOpticCableBlock) {
                    if (!visitedCables.contains(neighbor)) {
                        singleNeighbor = neighbor.immutable();
                        neighborsCount++;
                    }
                } else if (state.getBlock() == OpticalInterfaceBlock.HOLDER.get()) {
                    var facing = state.getValue(OpticalInterfaceBlock.FACING);
                    if (facing == d.getOpposite()) {
                        singleOutputPos = neighbor.immutable();
                        outputCount++;
                    }
                }
            }

            int totalConnectionsAtCurrent = 0;
            for (var d : DIRECTIONS) {
                if ((currentMask & getDirectionBit(d)) == 0) continue;
                var neighbor = currentPos.relative(d);
                var state = this.level.getBlockState(neighbor);
                if (state.getBlock() instanceof FiberOpticCableBlock) {
                    totalConnectionsAtCurrent++;
                } else if (state.getBlock() == OpticalInterfaceBlock.HOLDER.get()) {
                    var facing = state.getValue(OpticalInterfaceBlock.FACING);
                    if (facing == d.getOpposite()) totalConnectionsAtCurrent++;
                } else if (state.getBlock() instanceof SfpModuleBlock) {
                    var facing = state.getValue(SfpModuleBlock.FACING);
                    if (facing == d.getOpposite()) totalConnectionsAtCurrent++;
                }
            }

            if (totalConnectionsAtCurrent != 2) return new TraceResult(false, null);

            if (outputCount > 0) if (outputCount == 1) {
                outputPos = singleOutputPos;
                break;
            } else {
                return new TraceResult(false, null);
            }

            if (neighborsCount == 1) {
                nextPos = singleNeighbor;
                visitedCables.add(nextPos);
            } else {
                return new TraceResult(false, null);
            }
        }

        int outputInterfaceConnections = 0;
        var outState = this.level.getBlockState(outputPos);
        if (outState.hasProperty(OpticalInterfaceBlock.FACING)) {
            var outFacing = outState.getValue(OpticalInterfaceBlock.FACING);
            for (var d : DIRECTIONS) {
                var neighbor = outputPos.relative(d);
                var state = this.level.getBlockState(neighbor);
                if (state.getBlock() instanceof FiberOpticCableBlock) if (d == outFacing) outputInterfaceConnections++;
            }
        }
        if (outputInterfaceConnections != 1) return new TraceResult(false, null);

        return new TraceResult(true, outputPos);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SfpModuleBlockEntity blockEntity) {
        if (level.isClientSide) return;

        if (blockEntity.delayTicks > 0) {
            blockEntity.delayTicks--;
            return;
        }

        for (var node : blockEntity.extraNodes) if (node != null && !node.isReady()) node.create(level, pos);

        if (blockEntity.getMainNode().isReady() && !blockEntity.connectionsCreated) {
            boolean allExtraReady = true;
            for (var node : blockEntity.extraNodes) {
                if (node == null || !node.isReady()) {
                    allExtraReady = false;
                    break;
                }
            }
            if (allExtraReady) {
                blockEntity.connectionsCreated = true;
                for (var node : blockEntity.extraNodes) {
                    if (node != null) try {
                        GridHelper.createConnection(Objects.requireNonNull(blockEntity.getMainNode().getNode()), Objects.requireNonNull(node.getNode()));
                    } catch (Exception e) {
                        blockEntity.connectionsCreated = false;
                    }
                }
            }
        }

        blockEntity.checkTimer--;
        if (blockEntity.needsTrace || blockEntity.checkTimer <= 0) {
            blockEntity.needsTrace = false;
            blockEntity.checkTimer = 40;

            var result = blockEntity.traceConnection();
            boolean currentlyValid = result.isValid;
            boolean isActive = blockEntity.getMainNode().isActive();

            boolean extraActive = true;
            for (var node : blockEntity.extraNodes) {
                if (node == null || !node.isActive()) {
                    extraActive = false;
                    break;
                }
            }

            boolean active = currentlyValid && isActive && extraActive;

            var currentState = level.getBlockState(pos);
            if (currentState.hasProperty(SfpModuleBlock.ACTIVE) && currentState.getValue(SfpModuleBlock.ACTIVE) != active) {
                level.setBlock(pos, currentState.setValue(SfpModuleBlock.ACTIVE, active), 3);
            }

            if (currentlyValid) {
                var outputBe = level.getBlockEntity(result.outputPos);
                if (outputBe instanceof OpticalInterfaceBlockEntity outInterface) {
                    if (blockEntity.getMainNode().isActive()) outInterface.receivePower();
                }
            }
        }
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.complementOf(EnumSet.of(orientation.getSide(FRONT)));
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        if (dir == getOrientation().getSide(FRONT)) return AECableType.NONE;
        return AECableType.DENSE_SMART;
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        onGridConnectableSidesChanged();
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (var node : this.extraNodes) if (node != null) node.destroy();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        for (var node : this.extraNodes) if (node != null) node.destroy();
    }
}