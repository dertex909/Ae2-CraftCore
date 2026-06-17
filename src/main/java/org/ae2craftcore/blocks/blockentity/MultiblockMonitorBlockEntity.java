package org.ae2craftcore.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.MultiblockMonitorBlock;
import org.ae2craftcore.blocks.block.OpticalInterfaceBlock;
import org.ae2craftcore.blocks.menu.MultiblockMonitorMenu;
import org.ae2craftcore.multiblock.MultiblockValidator;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisterBlockEntity(name = "multiblock_monitor", blocks = {MultiblockMonitorBlock.class})
public class MultiblockMonitorBlockEntity extends BlockEntity implements MenuProvider {
    public static BlockEntityType<MultiblockMonitorBlockEntity> TYPE;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            if (MultiblockMonitorBlockEntity.this.level == null) return -1;
            var state = MultiblockMonitorBlockEntity.this.getBlockState();
            if (!state.hasProperty(MultiblockMonitorBlock.FACING)) return -1;

            var center = MultiblockMonitorBlockEntity.this.worldPosition.relative(state.getValue(MultiblockMonitorBlock.FACING).getOpposite(), 4);
            return switch (index) {
                case 0 -> MultiblockMonitorBlockEntity.this.checkStructureValid(center) ? 1 : 0;
                case 1 -> MultiblockMonitorBlockEntity.this.checkInventoriesValid(center) ? 1 : 0;
                case 2 -> MultiblockMonitorBlockEntity.this.checkMePowered(center) ? 1 : 0;
                case 3 -> MultiblockMonitorBlockEntity.this.getPicDurability(center.offset(2, 0, 0)); // East Injector
                case 4 -> MultiblockMonitorBlockEntity.this.getPicDurability(center.offset(-2, 0, 0)); // West Injector
                case 5 -> MultiblockMonitorBlockEntity.this.getPicDurability(center.offset(0, 0, 2)); // South Injector
                case 6 -> MultiblockMonitorBlockEntity.this.getPicDurability(center.offset(0, 0, -2)); // North Injector
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

    private boolean checkStructureValid(BlockPos center) {
        if (this.level == null) return false;
        var r = MultiblockValidator.validateStructure(this.level, center);
        return r.isValid();
    }

    private boolean checkInventoriesValid(BlockPos center) {
        if (this.level == null) return false;
        var r = MultiblockValidator.validateInventories(this.level, center);
        return r.isValid();
    }

    private boolean checkMePowered(BlockPos center) {
        if (this.level == null) return false;
        for (int y = -3; y <= 3; y++) {
            for (int i = -3; i <= 3; i++) {
                var pos1 = center.offset(4, y, i);
                if (this.level.getBlockState(pos1).is(OpticalInterfaceBlock.HOLDER.get())) {
                    var be = this.level.getBlockEntity(pos1);
                    if (be instanceof OpticalInterfaceBlockEntity opt && opt.isPowered()) return true;
                }
                var pos2 = center.offset(-4, y, i);
                if (this.level.getBlockState(pos2).is(OpticalInterfaceBlock.HOLDER.get())) {
                    var be = this.level.getBlockEntity(pos2);
                    if (be instanceof OpticalInterfaceBlockEntity opt && opt.isPowered()) return true;
                }
                var pos3 = center.offset(i, y, 4);
                if (this.level.getBlockState(pos3).is(OpticalInterfaceBlock.HOLDER.get())) {
                    var be = this.level.getBlockEntity(pos3);
                    if (be instanceof OpticalInterfaceBlockEntity opt && opt.isPowered()) return true;
                }
                var pos4 = center.offset(i, y, -4);
                if (this.level.getBlockState(pos4).is(OpticalInterfaceBlock.HOLDER.get())) {
                    var be = this.level.getBlockEntity(pos4);
                    if (be instanceof OpticalInterfaceBlockEntity opt && opt.isPowered()) return true;
                }
            }
        }
        return false;
    }

    private int getPicDurability(BlockPos injectorPos) {
        if (this.level == null) return -1;
        var be = this.level.getBlockEntity(injectorPos);
        if (be instanceof PicInjectorBlockEntity injector) {
            var stack = injector.getItem(0);
            if (stack.isEmpty() || !stack.is(org.ae2craftcore.items.BaseResources.PHOTONIC_INTEGRATED_CIRCUIT.get())) {
                return -1;
            }
            int maxDamage = stack.getMaxDamage();
            if (maxDamage <= 0) return 100;
            int currentDurability = maxDamage - stack.getDamageValue();
            return Math.clamp((int) ((currentDurability * 100L) / maxDamage), 0, 100);
        }
        return -1;
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