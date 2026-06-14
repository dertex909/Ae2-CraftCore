package org.ae2craftcore.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.FiberOpticCableBlock;
import org.ae2craftcore.blocks.block.SfpModuleBlock;
import org.ae2craftcore.blocks.menu.SfpModuleMenu;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.util.inv.AppEngInternalInventory;

import java.util.HashSet;
import java.util.EnumSet;
import java.util.Set;

import static appeng.api.config.Actionable.MODULATE;
import static appeng.api.config.PowerMultiplier.ONE;
import static appeng.api.orientation.RelativeSide.FRONT;

@RegisterBlockEntity(name = "sfp_module", blocks = {SfpModuleBlock.class})
public class SfpModuleBlockEntity extends AENetworkedPoweredBlockEntity implements MenuProvider {
    public static BlockEntityType<SfpModuleBlockEntity> TYPE;

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final ResourceLocation CONTROLLER_ID = ResourceLocation.fromNamespaceAndPath("ae2", "controller");

    public enum SFPMode {
        INPUT,
        OUTPUT
    }

    private SFPMode sfpMode;
    private int channels = 64;

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 0);

    private boolean pathValid = false;

    private int delayTicks = 100;
    private int checkTimer = 0;
    private boolean needsTrace = true;

    public void markNeedsTrace() {
        this.needsTrace = true;
        this.checkTimer = 0;
    }

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> SfpModuleBlockEntity.this.channels;
                case 1 -> SfpModuleBlockEntity.this.sfpMode == SFPMode.INPUT ? 1 : 0;
                case 2 -> SfpModuleBlockEntity.this.pathValid ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> SfpModuleBlockEntity.this.channels = Math.clamp(value, 64, 8192);
                case 1 -> {}
                case 2 -> SfpModuleBlockEntity.this.pathValid = value == 1;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public SfpModuleBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        this.sfpMode = state.getValue(SfpModuleBlock.IS_INPUT) ? SFPMode.INPUT : SFPMode.OUTPUT;

        if (this.sfpMode == SFPMode.INPUT) {
            this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY).setIdlePowerUsage(5);
        } else {
            this.getMainNode().setFlags(GridFlags.CANNOT_CARRY, GridFlags.DENSE_CAPACITY).setIdlePowerUsage(5);
        }
        this.setInternalMaxPower(1000);
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return this.getBlockState().getBlock().asItem();
    }

    public SFPMode getSfpMode() {
        return this.sfpMode;
    }

    public void setChannels(int channels) {
        this.channels = Math.clamp(channels, 64, 8192);
        this.setChanged();
    }

    public int getChannels() {
        return this.channels;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("SFP Module Configuration");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new SfpModuleMenu(containerId, this.dataAccess, this.worldPosition);
    }

    private boolean isAdjacentToController() {
        if (this.level == null) return false;
        for (var d : DIRECTIONS) {
            var adjState = this.level.getBlockState(this.worldPosition.relative(d));
            if (BuiltInRegistries.BLOCK.getKey(adjState.getBlock()).equals(CONTROLLER_ID)) return true;
        }
        return false;
    }

    public record TraceResult(boolean isValid, BlockPos outputPos) {
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
            BlockPos singleOutputSfp = null;
            int outputSfpsCount = 0;

            for (var d : DIRECTIONS) {
                var neighbor = currentPos.relative(d);
                if (neighbor.equals(this.worldPosition)) continue;

                var state = this.level.getBlockState(neighbor);
                if (state.getBlock() instanceof FiberOpticCableBlock) {
                    if (!visitedCables.contains(neighbor)) {
                        singleNeighbor = neighbor.immutable();
                        neighborsCount++;
                    }
                } else if (state.getBlock() instanceof SfpModuleBlock) {
                    var be = this.level.getBlockEntity(neighbor);
                    if (be instanceof SfpModuleBlockEntity sfp && sfp.getSfpMode() == SFPMode.OUTPUT) {
                        var sfpFacing = state.getValue(SfpModuleBlock.FACING);
                        if (sfpFacing == d.getOpposite()) {
                            singleOutputSfp = neighbor.immutable();
                            outputSfpsCount++;
                        }
                    }
                }
            }

            int totalConnectionsAtCurrent = 0;
            for (var d : DIRECTIONS) {
                var neighbor = currentPos.relative(d);
                var state = this.level.getBlockState(neighbor);
                if (state.getBlock() instanceof FiberOpticCableBlock) {
                    totalConnectionsAtCurrent++;
                } else if (state.getBlock() instanceof SfpModuleBlock) {
                    var be = this.level.getBlockEntity(neighbor);
                    if (be instanceof SfpModuleBlockEntity) {
                        var sfpFacing = state.getValue(SfpModuleBlock.FACING);
                        if (sfpFacing == d.getOpposite()) totalConnectionsAtCurrent++;
                    }
                }
            }

            if (totalConnectionsAtCurrent != 2) return new TraceResult(false, null);

            if (outputSfpsCount > 0) if (outputSfpsCount == 1) {
                outputPos = singleOutputSfp;
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

        int outputSfpConnections = 0;
        var outState = this.level.getBlockState(outputPos);
        if (outState.hasProperty(SfpModuleBlock.FACING)) {
            var outFacing = outState.getValue(SfpModuleBlock.FACING);
            for (var d : DIRECTIONS) {
                var neighbor = outputPos.relative(d);
                var state = this.level.getBlockState(neighbor);
                if (state.getBlock() instanceof FiberOpticCableBlock) if (d == outFacing) outputSfpConnections++;
            }
        }
        if (outputSfpConnections != 1) return new TraceResult(false, null);

        return new TraceResult(true, outputPos);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SfpModuleBlockEntity blockEntity) {
        if (level.isClientSide) return;

        if (blockEntity.delayTicks > 0) {
            blockEntity.delayTicks--;
            return;
        }

        if (blockEntity.sfpMode == SFPMode.INPUT) {
            blockEntity.checkTimer--;
            if (blockEntity.needsTrace || blockEntity.checkTimer <= 0) {
                blockEntity.needsTrace = false;
                blockEntity.checkTimer = 40;

                boolean isAdjacent = blockEntity.isAdjacentToController();
                var result = blockEntity.traceConnection();

                boolean currentlyValid = isAdjacent && result.isValid;
                blockEntity.pathValid = currentlyValid;

                if (currentlyValid) {
                    var outputBe = level.getBlockEntity(result.outputPos);
                    if (outputBe instanceof SfpModuleBlockEntity outSfp) {
                        var gridA = blockEntity.getMainNode().getGrid();
                        var gridB = outSfp.getMainNode().getGrid();
                        if (gridA != null && gridB != null) {
                            double powerNeeded = gridB.getEnergyService().getEnergyDemand(1000);
                            if (powerNeeded > 0) {
                                double extracted = gridA.getEnergyService().extractAEPower(powerNeeded, MODULATE, ONE);
                                if (extracted > 0) gridB.getEnergyService().injectPower(extracted, MODULATE);
                            }
                        }
                    }
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
        tag.putInt("Channels", this.channels);
        tag.putString("SfpMode", this.sfpMode.name());
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        if (tag.contains("Channels")) this.channels = tag.getInt("Channels");
        if (tag.contains("SfpMode")) {
            this.sfpMode = SFPMode.valueOf(tag.getString("SfpMode"));
        }
    }
}