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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.LogicAssemblerBlock;
import org.ae2craftcore.blocks.menu.LogicAssemblerMenu;
import org.ae2craftcore.items.BaseResources;
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
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

import java.util.EnumSet;
import java.util.Set;

import static appeng.core.definitions.AEItems.SPEED_CARD;

@RegisterBlockEntity(name = "logic_assembler", blocks = {LogicAssemblerBlock.class})
public class LogicAssemblerBlockEntity extends AENetworkedPoweredBlockEntity implements WorldlyContainer, MenuProvider {
    public static BlockEntityType<LogicAssemblerBlockEntity> TYPE;

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 7, 64, new IAEItemFilter() {
        @Override
        public boolean allowInsert(InternalInventory inventory, int slot, ItemStack stack) {
            if (slot == 2) return false;
            if (slot >= 3 && slot <= 6)
                return LogicAssemblerMenu.canInstallUpgradeCard(LogicAssemblerBlockEntity.this, slot, stack);
            return LogicAssemblerBlockEntity.this.isValidInput(slot, stack);
        }
    });

    private ItemStack rolledResult = ItemStack.EMPTY;
    private double progress = 0.0;
    private int maxProgress = 100;
    private int activeRecipeChance = 0;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) Math.round(LogicAssemblerBlockEntity.this.progress);
                case 1 -> LogicAssemblerBlockEntity.this.maxProgress;
                case 2 -> LogicAssemblerBlockEntity.this.activeRecipeChance;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> LogicAssemblerBlockEntity.this.progress = value;
                case 1 -> LogicAssemblerBlockEntity.this.maxProgress = value;
                case 2 -> LogicAssemblerBlockEntity.this.activeRecipeChance = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public LogicAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        this.getMainNode().setFlags().setIdlePowerUsage(10);
        this.setInternalMaxPower(10000);
        this.setPowerSides(getGridConnectableSides(getOrientation()));
    }

    public ItemStack getRolledResult() {
        return this.rolledResult;
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
            if (!stack.isEmpty() && SPEED_CARD.is(stack)) count += stack.getCount();
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

    public float calculateRecipeBonus(LogicAssemblerRecipe recipe) {
        float bonus = 0.0f;
        for (var upgrade : recipe.getUpgrades()) if (hasUpgradeCard(upgrade.card())) bonus += upgrade.chanceBonus();
        return bonus;
    }

    private boolean hasUpgradeCard(Item cardItem) {
        for (int i = 3; i < 7; i++) {
            var stack = this.getItem(i);
            if (!stack.isEmpty() && stack.is(cardItem)) return true;
        }
        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LogicAssemblerBlockEntity blockEntity) {
        if (level.isClientSide) return;

        blockEntity.chargeInternalBuffer();

        if (blockEntity.rolledResult.isEmpty()) {
            var top = blockEntity.getItem(0);
            var bottom = blockEntity.getItem(1);

            if (!top.isEmpty() && !bottom.isEmpty()) {
                var input = new LogicAssemblerRecipe.LogicAssemblerInput(top, bottom);
                var optionalRecipe = level.getRecipeManager().getRecipeFor(ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get(), input, level);

                if (optionalRecipe.isPresent()) {
                    var holder = optionalRecipe.get();
                    var recipe = holder.value();
                    var recipeResult = recipe.getResultItem(level.registryAccess());

                    top.shrink(1);
                    bottom.shrink(1);

                    float recipeBonus = blockEntity.calculateRecipeBonus(recipe);
                    int speedCards = blockEntity.getSpeedCardsCount();
                    float penalty = speedCards * 0.01f;
                    float finalChance = Math.clamp(recipe.getChance() + recipeBonus - penalty, 0.0f, 1.0f);

                    if (level.random.nextFloat() <= finalChance) {
                        blockEntity.rolledResult = recipeResult.copy();
                    } else {
                        blockEntity.rolledResult = new ItemStack(BaseResources.QUANTUM_SCRAP);
                    }

                    blockEntity.activeRecipeChance = Math.round(finalChance * 100);
                    blockEntity.progress = 0;
                    blockEntity.setChanged();
                }
            }
        }

        if (!blockEntity.rolledResult.isEmpty()) {
            int speedCards = blockEntity.getSpeedCardsCount();
            double progressStep = switch (speedCards) {
                case 1 -> 2.5;
                case 2 -> 5.0;
                case 3 -> 7.5;
                case 4 -> 10.0;
                default -> 1.0;
            };
            double powerRequired = 26.7 * progressStep;

            double powerExtracted = blockEntity.extractPower(powerRequired);
            if (powerExtracted >= powerRequired - 0.01) {

                if (blockEntity.progress < blockEntity.maxProgress) {
                    blockEntity.progress += progressStep;
                    blockEntity.setChanged();
                }

                if (blockEntity.progress >= blockEntity.maxProgress) {
                    var outputStack = blockEntity.getItem(2);
                    var craftResult = blockEntity.rolledResult;

                    if (outputStack.isEmpty() || (ItemStack.isSameItemSameComponents(outputStack, craftResult)
                            && outputStack.getCount() + craftResult.getCount() <= outputStack.getMaxStackSize())) {

                        if (outputStack.isEmpty()) {
                            blockEntity.setItem(2, craftResult.copy());
                        } else {
                            outputStack.grow(craftResult.getCount());
                        }

                        blockEntity.rolledResult = ItemStack.EMPTY;
                        blockEntity.progress = 0;
                        blockEntity.activeRecipeChance = 0;
                        blockEntity.setChanged();
                    }
                }
            }
        } else {
            if (blockEntity.progress > 0) {
                blockEntity.progress = 0;
                blockEntity.setChanged();
            }
            if (blockEntity.activeRecipeChance != 0) {
                blockEntity.activeRecipeChance = 0;
                blockEntity.setChanged();
            }
        }
    }

    public boolean isValidInput(int slot, @NotNull ItemStack stack) {
        if (this.level == null) return true;
        var otherStack = this.getItem(slot == 0 ? 1 : 0);
        var recipes = this.level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.LOGIC_ASSEMBLING_TYPE.get());

        for (var holder : recipes) {
            var recipe = holder.value();
            boolean matchesCurrent = (slot == 0) ? recipe.getTop().test(stack) : recipe.getBottom().test(stack);
            if (matchesCurrent) {
                if (otherStack.isEmpty()) return true;
                boolean matchesOther = (slot == 0) ? recipe.getBottom().test(otherStack) : recipe.getTop().test(otherStack);
                if (matchesOther) return true;
            }
        }
        return false;
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
        if (side == Direction.DOWN) return new int[]{2};
        else return new int[]{0, 1};
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        if (slot == 2) return false;
        if (slot >= 3 && slot <= 6) return LogicAssemblerMenu.canInstallUpgradeCard(this, slot, stack);
        return this.isValidInput(slot, stack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
        if (direction == Direction.DOWN) return false;
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return index == 2 && direction == Direction.DOWN;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("ProgressDouble", this.progress);
        tag.putInt("Progress", (int) Math.round(this.progress));
        tag.putInt("MaxProgress", this.maxProgress);
        tag.putInt("ActiveRecipeChance", this.activeRecipeChance);
        if (!this.rolledResult.isEmpty()) tag.put("RolledResult", this.rolledResult.save(registries));
    }

    @Override
    public void loadTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadTag(tag, registries);
        if (tag.contains("ProgressDouble")) {
            this.progress = tag.getDouble("ProgressDouble");
        } else {
            this.progress = tag.getInt("Progress");
        }
        this.maxProgress = tag.getInt("MaxProgress");
        this.activeRecipeChance = tag.getInt("ActiveRecipeChance");
        if (tag.contains("RolledResult")) {
            this.rolledResult = ItemStack.parseOptional(registries, tag.getCompound("RolledResult"));
        } else this.rolledResult = ItemStack.EMPTY;
    }
}