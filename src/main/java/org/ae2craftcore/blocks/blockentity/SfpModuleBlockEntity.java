package org.ae2craftcore.blocks.blockentity;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
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

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import static appeng.api.orientation.RelativeSide.FRONT;

@RegisterBlockEntity(name = "sfp_module", blocks = {SfpModuleBlock.class})
public class SfpModuleBlockEntity extends AENetworkedPoweredBlockEntity {
    public static BlockEntityType<SfpModuleBlockEntity> TYPE;

    private static final Direction[] DIRECTIONS = Direction.values();

    private static final int[] DIR_BITS = new int[6];

    static {
        for (var dir : DIRECTIONS) {
            DIR_BITS[dir.ordinal()] = switch (dir) {
                case NORTH -> 1;
                case EAST -> 2;
                case SOUTH -> 4;
                case WEST -> 8;
                case UP -> 16;
                case DOWN -> 32;
            };
        }
    }

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 0);

    private int delayTicks = 100;
    private int checkTimer = 0;
    private boolean needsTrace = true;
    private boolean connectionsCreated = false;

    private long lastOutputPacked = 0L;
    private boolean hasLastOutput = false;

    private static final ThreadLocal<LongOpenHashSet> VISITED_SET = ThreadLocal.withInitial(LongOpenHashSet::new);
    private static final ThreadLocal<BlockPos.MutableBlockPos> MUTABLE_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

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

    public TraceResult traceConnection() {
        if (this.level == null) return new TraceResult(false, null);

        var visitedCables = VISITED_SET.get();
        visitedCables.clear();
        var tempPos = MUTABLE_POS.get();
        long worldPosPacked = this.worldPosition.asLong();
        long currentPacked = worldPosPacked;
        BlockPos outputPos;

        long nextPacked = 0L;
        boolean hasNext = false;

        var blockState = getBlockState();
        if (blockState.hasProperty(SfpModuleBlock.FACING)) {
            var facing = blockState.getValue(SfpModuleBlock.FACING);
            long neighborPacked = BlockPos.offset(currentPacked, facing);
            tempPos.set(neighborPacked);
            var state = this.level.getBlockState(tempPos);
            if (state.getBlock() instanceof FiberOpticCableBlock) {
                nextPacked = neighborPacked;
                hasNext = true;
            }
        }

        if (!hasNext) return new TraceResult(false, null);

        visitedCables.add(nextPacked);

        while (true) {
            if (visitedCables.size() > 8192) return new TraceResult(false, null);
            currentPacked = nextPacked;

            long singleNeighbor = 0L;
            int neighborsCount = 0;

            long singleOutputPos = 0L;
            int outputCount = 0;

            tempPos.set(currentPacked);
            var currentState = this.level.getBlockState(tempPos);
            if (!(currentState.getBlock() instanceof FiberOpticCableBlock)) return new TraceResult(false, null);

            int currentMask = currentState.getValue(FiberOpticCableBlock.CONNECTION_MASK);
            for (var d : DIRECTIONS) {
                if ((currentMask & DIR_BITS[d.ordinal()]) == 0) continue;

                long neighborPacked = BlockPos.offset(currentPacked, d);
                if (neighborPacked == worldPosPacked) continue;

                tempPos.set(neighborPacked);
                var state = this.level.getBlockState(tempPos);
                if (state.getBlock() instanceof FiberOpticCableBlock) {
                    if (!visitedCables.contains(neighborPacked)) {
                        singleNeighbor = neighborPacked;
                        neighborsCount++;
                    }
                } else if (state.getBlock() == OpticalInterfaceBlock.HOLDER.get()) {
                    var facing = state.getValue(OpticalInterfaceBlock.FACING);
                    if (facing == d.getOpposite()) {
                        singleOutputPos = neighborPacked;
                        outputCount++;
                    }
                }
            }

            int totalConnectionsAtCurrent = 0;
            for (var d : DIRECTIONS) {
                if ((currentMask & DIR_BITS[d.ordinal()]) == 0) continue;

                long neighborPacked = BlockPos.offset(currentPacked, d);
                tempPos.set(neighborPacked);
                var state = this.level.getBlockState(tempPos);
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
                outputPos = BlockPos.of(singleOutputPos);
                break;
            } else {
                return new TraceResult(false, null);
            }

            if (neighborsCount == 1) {
                nextPacked = singleNeighbor;
                visitedCables.add(nextPacked);
            } else {
                return new TraceResult(false, null);
            }
        }

        int outputInterfaceConnections = 0;
        var outState = this.level.getBlockState(outputPos);
        if (outState.hasProperty(OpticalInterfaceBlock.FACING)) {
            var outFacing = outState.getValue(OpticalInterfaceBlock.FACING);
            for (var d : DIRECTIONS) {
                long neighborPacked = BlockPos.offset(outputPos.asLong(), d);
                tempPos.set(neighborPacked);
                var state = this.level.getBlockState(tempPos);
                if (state.getBlock() instanceof FiberOpticCableBlock) if (d == outFacing) outputInterfaceConnections++;
            }
        }
        if (outputInterfaceConnections != 1) return new TraceResult(false, null);

        return new TraceResult(true, outputPos);
    }

    public void triggerImmediateTrace() {
        if (this.level == null || this.level.isClientSide) return;

        var result = this.traceConnection();
        boolean currentlyValid = result.isValid;
        boolean isActive = this.getMainNode().isActive();

        boolean extraActive = true;
        for (var node : this.extraNodes) {
            if (node == null || !node.isActive()) {
                extraActive = false;
                break;
            }
        }

        boolean active = currentlyValid && isActive && extraActive;

        var currentState = this.level.getBlockState(this.worldPosition);
        if (currentState.hasProperty(SfpModuleBlock.ACTIVE) && currentState.getValue(SfpModuleBlock.ACTIVE) != active) {
            this.level.setBlock(this.worldPosition, currentState.setValue(SfpModuleBlock.ACTIVE, active), 3);
        }

        if (active) {
            long resultOutputPacked = result.outputPos.asLong();
            if (this.hasLastOutput && this.lastOutputPacked != resultOutputPacked) {
                this.setOpticalInterfacePower(BlockPos.of(this.lastOutputPacked), false);
            }
            this.lastOutputPacked = resultOutputPacked;
            this.hasLastOutput = true;
            this.setOpticalInterfacePower(result.outputPos, true);
        } else {
            if (this.hasLastOutput) {
                this.setOpticalInterfacePower(BlockPos.of(this.lastOutputPacked), false);
                this.lastOutputPacked = 0L;
                this.hasLastOutput = false;
            }
        }
    }

    private void setOpticalInterfacePower(BlockPos pos, boolean power) {
        if (this.level == null) return;

        if (this.level.isLoaded(pos)) {
            var be = this.level.getBlockEntity(pos);
            if (be instanceof OpticalInterfaceBlockEntity opt) opt.setPowered(power);
        }
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
            blockEntity.triggerImmediateTrace();
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
        if (this.hasLastOutput) tag.putLong("LastOutputPos", this.lastOutputPacked);
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        if (tag.contains("LastOutputPos")) {
            this.lastOutputPacked = tag.getLong("LastOutputPos");
            this.hasLastOutput = true;
        } else {
            this.lastOutputPacked = 0L;
            this.hasLastOutput = false;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (var node : this.extraNodes) if (node != null) node.destroy();
        if (this.level != null && !this.level.isClientSide && this.hasLastOutput) {
            this.setOpticalInterfacePower(BlockPos.of(this.lastOutputPacked), false);
            this.lastOutputPacked = 0L;
            this.hasLastOutput = false;
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        for (var node : this.extraNodes) if (node != null) node.destroy();
    }
}