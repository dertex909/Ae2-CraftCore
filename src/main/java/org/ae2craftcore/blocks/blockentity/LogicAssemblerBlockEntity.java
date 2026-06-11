package org.ae2craftcore.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.blocks.menu.LogicAssemblerMenu;
import org.ae2craftcore.recipe.LogicAssemblerRecipe;
import org.ae2craftcore.registry.ModRecipeTypes;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.inventories.InternalInventory;
import appeng.api.orientation.BlockOrientation;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

import java.util.EnumSet;
import java.util.Set;

@RegisterBlockEntity(name = "logic_assembler", blocks = {LogicAssemblerBlock.class})
public class LogicAssemblerBlockEntity extends AENetworkedPoweredBlockEntity implements WorldlyContainer, MenuProvider {
    public static BlockEntityType<LogicAssemblerBlockEntity> TYPE;

    // Используем встроенный инвентарь AE2, настроенный на 7 слотов с максимальным стаком 64
    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 7, 64, new IAEItemFilter() {
        @Override
        public boolean allowInsert(InternalInventory inventory, int slot, ItemStack stack) {
            if (slot == 2) return false; // Запрещено вручную/автоматически класть в выходной слот
            if (slot >= 3 && slot <= 6) return AEItems.SPEED_CARD.is(stack);
            // В слоты 3-6 можно класть только карты скорости
            return true; // Рабочие входы (0 и 1)
        }
    });

    private int progress = 0;
    private int maxProgress = 100;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> LogicAssemblerBlockEntity.this.progress;
                case 1 -> LogicAssemblerBlockEntity.this.maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> LogicAssemblerBlockEntity.this.progress = value;
                case 1 -> LogicAssemblerBlockEntity.this.maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public LogicAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        this.getMainNode().setFlags().setIdlePowerUsage(1); // Потребление в простое: 1 AE/t
        this.setInternalMaxPower(1600); // Максимальный буфер энергии прибора
        this.setPowerSides(getGridConnectableSides(getOrientation()));
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.complementOf(EnumSet.of(orientation.getSide(appeng.api.orientation.RelativeSide.FRONT)));
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        this.setPowerSides(getGridConnectableSides(orientation));
    }

    public int getSpeedCardsCount() {
        int count = 0;
        for (int i = 3; i < 7; i++) {
            var stack = this.getItem(i);
            if (!stack.isEmpty() && AEItems.SPEED_CARD.is(stack)) count += stack.getCount();
        }
        return Math.min(4, count);
    }

    private double extractPower(double amount) {
        double extracted = this.extractAEPower(amount, Actionable.MODULATE, PowerMultiplier.CONFIG);
        if (extracted >= amount - 0.01) return extracted;

        double missing = amount - extracted;
        var grid = this.getMainNode().getGrid();
        if (grid != null) {
            double gridExtracted = grid.getEnergyService().extractAEPower(missing, Actionable.MODULATE, PowerMultiplier.ONE);
            extracted += gridExtracted;
        }
        return extracted;
    }

    private void chargeInternalBuffer() {
        if (this.getInternalCurrentPower() < this.getInternalMaxPower() - 1) this.getMainNode().ifPresent(grid -> {
            double toExtract = Math.min(80.0, this.getInternalMaxPower() - this.getInternalCurrentPower());
            double extracted = grid.getEnergyService().extractAEPower(toExtract, Actionable.MODULATE, PowerMultiplier.ONE);
            this.injectExternalPower(PowerUnit.AE, extracted, Actionable.MODULATE);
        });
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LogicAssemblerBlockEntity blockEntity) {
        if (level.isClientSide) return;

        blockEntity.chargeInternalBuffer();
        var top = blockEntity.getItem(0);
        var bottom = blockEntity.getItem(1);

        if (top.isEmpty() || bottom.isEmpty()) {
            if (blockEntity.progress > 0) {
                blockEntity.progress = 0;
                blockEntity.setChanged();
            }
            return;
        }

        var input = new LogicAssemblerRecipe.LogicAssemblerInput(top, bottom);
        var optionalRecipe = level.getRecipeManager().getRecipeFor(ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get(), input, level);

        if (optionalRecipe.isPresent()) {
            var holder = optionalRecipe.get();
            var recipe = holder.value();
            var recipeResult = recipe.assemble(input, level.registryAccess());

            var outputStack = blockEntity.getItem(2);
            if (outputStack.isEmpty() || (ItemStack.isSameItemSameComponents(outputStack, recipeResult)
                    && outputStack.getCount() + recipeResult.getCount() <= outputStack.getMaxStackSize())) {

                int speedCards = blockEntity.getSpeedCardsCount();
                int progressStep = switch (speedCards) {
                    case 1 -> 2;
                    case 2 -> 4;
                    case 3 -> 8;
                    case 4 -> 16;
                    default -> 1;
                };
                double powerRequired = 5.0 * progressStep;

                double powerExtracted = blockEntity.extractPower(powerRequired);
                if (powerExtracted >= powerRequired - 0.01) {
                    blockEntity.progress += progressStep;
                    blockEntity.setChanged();

                    if (blockEntity.progress >= blockEntity.maxProgress) {
                        blockEntity.progress = 0;
                        blockEntity.getItem(0).shrink(1);
                        blockEntity.getItem(1).shrink(1);

                        if (outputStack.isEmpty()) {
                            blockEntity.setItem(2, recipeResult.copy());
                        } else {
                            outputStack.grow(recipeResult.getCount());
                        }
                        blockEntity.setChanged();
                    }
                }
            } else {
                if (blockEntity.progress > 0) {
                    blockEntity.progress = 0;
                    blockEntity.setChanged();
                }
            }
        } else {
            if (blockEntity.progress > 0) {
                blockEntity.progress = 0;
                blockEntity.setChanged();
            }
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.ae2craftcore.logic_assembler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new LogicAssemblerMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return this.inv.size();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < this.inv.size(); i++) if (!this.inv.getStackInSlot(i).isEmpty()) return false;
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return this.inv.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        var stack = this.inv.extractItem(slot, amount, false);
        if (!stack.isEmpty()) this.setChanged();
        return stack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        var stack = this.inv.getStackInSlot(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        this.inv.setItemDirect(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        this.inv.setItemDirect(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) stack.setCount(this.getMaxStackSize());
        this.setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.inv.size(); i++) this.inv.setItemDirect(i, ItemStack.EMPTY);
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        if (side == Direction.DOWN) {
            return new int[]{2};
        } else {
            return new int[]{0, 1};
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
        if (index >= 2) return false;
        return direction != Direction.DOWN;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return index == 2 && direction == Direction.DOWN;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", this.progress);
        tag.putInt("MaxProgress", this.maxProgress);
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        this.progress = tag.getInt("Progress");
        this.maxProgress = tag.getInt("MaxProgress");
    }
}