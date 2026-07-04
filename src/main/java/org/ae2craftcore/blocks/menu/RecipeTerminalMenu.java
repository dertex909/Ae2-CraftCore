package org.ae2craftcore.blocks.menu;

import appeng.menu.me.common.IClientRepo;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.FakeSlot;
import appeng.parts.encoding.EncodingMode;
import appeng.api.inventories.InternalInventory;
import appeng.menu.guisync.GuiSync;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.neoforged.neoforge.network.PacketDistributor;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.blockentity.MeMachineInterfaceBlockEntity;
import org.ae2craftcore.mixin.SlotAccessor;
import org.ae2craftcore.network.packet.RecipeTerminalSyncPacket;
import org.ae2craftcore.parts.RecipeTerminalPart;
import org.ae2craftcore.items.RecipeStorageCellItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.ae2craftcore.registry.ModMenuTypes.RECIPE_TERMINAL;

public class RecipeTerminalMenu extends PatternEncodingTermMenu {
    public static final int IMAGE_WIDTH = 322;
    public static final int IMAGE_HEIGHT = 236;

    public static final int MACHINE_LIST_X = 6;
    public static final int MACHINE_LIST_Y = 16;
    public static final int MACHINE_LIST_WIDTH = 132;
    public static final int MACHINE_LIST_HEIGHT = 108;

    public static final int RECIPE_LIST_X = 171;
    public static final int RECIPE_LIST_Y = 16;
    public static final int RECIPE_LIST_WIDTH = 132;
    public static final int RECIPE_LIST_HEIGHT = 108;

    public static final int ENCODING_X = 173;
    public static final int ENCODING_Y = 156;
    public static final int ENCODING_WIDTH = 124;
    public static final int ENCODING_SLOT_X = ENCODING_X;
    public static final int ENCODING_SLOT_Y = ENCODING_Y;

    public static final int MODE_TABS_X = ENCODING_X + ENCODING_WIDTH + 22;
    public static final int MODE_TABS_Y = ENCODING_Y - 9;

    public static final int PLAYER_INV_X = 8;
    public static final int PLAYER_INV_Y = 152;
    public static final int HOTBAR_Y = 210;

    private final RecipeTerminalPart part;
    private final List<Slot> processingInputSlots = new ArrayList<>(81);
    private final List<Slot> processingOutputSlots = new ArrayList<>(27);

    private final Slot[] craftingInputSlots = new Slot[9];
    private Slot smithingTemplateSlot;
    private Slot smithingBaseSlot;
    private Slot smithingAdditionSlot;
    private Slot stonecutterInputSlot;

    private int processingScrollOffset = 0;
    private String selectedGroup = "";
    private final List<ItemStack> clientRecipes = new ArrayList<>();
    private final List<RecipeTerminalSyncPacket.MachineGroupInfo> clientGroups = new ArrayList<>();
    private boolean firstSync = true;
    private IClientRepo cachedProxyRepo;

    @GuiSync(297)
    public EncodingMode mode = EncodingMode.PROCESSING;
    @GuiSync(296)
    public boolean substitute = false;
    @GuiSync(295)
    public boolean substituteFluids = true;
    @GuiSync(294)
    @Nullable
    public ResourceLocation stonecuttingRecipeId;
    private static final String SETSTONECUTTINGRECIPEID = "setStonecuttingRecipeId";

    public RecipeTerminalMenu(int containerId, Inventory playerInventory, RecipeTerminalPart part) {
        super(RECIPE_TERMINAL.get(), containerId, playerInventory, part, false);

        this.slots.clear();
        this.part = part;

        this.addEncodingModeSlots();
        try {
            this.registerClientAction(SETSTONECUTTINGRECIPEID, ResourceLocation.class, this.part.getLogic()::setStonecuttingRecipeId);
        } catch (IllegalArgumentException ignored) {
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * 18, HOTBAR_Y));
        }

        this.syncParentSlots();
    }

    private void addEncodingModeSlots() {
        var logic = this.part.getLogic();
        var encodedInputs = logic.getEncodedInputInv().createMenuWrapper();
        var encodedOutputs = logic.getEncodedOutputInv().createMenuWrapper();

        int craftIndex = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                var slot = new RecipeTerminalPhantomSlot(encodedInputs, col + row * 3, ENCODING_SLOT_X + 7 + col * 18, ENCODING_SLOT_Y + 7 + row * 18, EncodingMode.CRAFTING);
                this.craftingInputSlots[craftIndex++] = this.addSlot(slot);
            }
        }
        this.addSlot(new RecipeResultSlot(EncodingMode.CRAFTING, ENCODING_SLOT_X + 98, ENCODING_SLOT_Y + 25));

        for (int row = 0; row < 27; row++) {
            for (int col = 0; col < 3; col++) {
                int index = col + row * 3;
                var slot = new RecipeTerminalProcessingInputSlot(encodedInputs, index, ENCODING_SLOT_X + 16 + col * 18, ENCODING_SLOT_Y + 7 + row * 18);
                this.processingInputSlots.add(this.addSlot(slot));
            }
        }
        for (int i = 0; i < 27; i++) {
            var slot = new RecipeTerminalProcessingOutputSlot(encodedOutputs, i, ENCODING_SLOT_X + 101, ENCODING_SLOT_Y + 7 + i * 18);
            this.processingOutputSlots.add(this.addSlot(slot));
        }

        this.smithingTemplateSlot = this.addSlot(new RecipeTerminalPhantomSlot(encodedInputs, 0, ENCODING_SLOT_X + 7, ENCODING_SLOT_Y + 25, EncodingMode.SMITHING_TABLE));
        this.smithingBaseSlot = this.addSlot(new RecipeTerminalPhantomSlot(encodedInputs, 1, ENCODING_SLOT_X + 25, ENCODING_SLOT_Y + 25, EncodingMode.SMITHING_TABLE));
        this.smithingAdditionSlot = this.addSlot(new RecipeTerminalPhantomSlot(encodedInputs, 2, ENCODING_SLOT_X + 43, ENCODING_SLOT_Y + 25, EncodingMode.SMITHING_TABLE));
        this.addSlot(new RecipeResultSlot(EncodingMode.SMITHING_TABLE, ENCODING_SLOT_X + 101, ENCODING_SLOT_Y + 25));

        this.stonecutterInputSlot = this.addSlot(new RecipeTerminalPhantomSlot(encodedInputs, 0, ENCODING_SLOT_X + 7, ENCODING_SLOT_Y + 25, EncodingMode.STONECUTTING));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (isServerSide() && this.part != null) {
            var logic = this.part.getLogic();
            if (this.mode != logic.getMode()) this.mode = logic.getMode();
            this.substitute = logic.isSubstitution();
            this.substituteFluids = logic.isFluidSubstitution();
            this.stonecuttingRecipeId = logic.getStonecuttingRecipeId();
        }

        if (this.firstSync) {
            this.firstSync = false;
            this.syncRecipesToClient();
        }
    }

    public void syncRecipesToClient() {
        if (this.part == null) return;
        var node = this.part.getGridNode();
        if (node == null) return;
        var grid = node.getGrid();
        if (grid == null) return;

        var list = RecipeStorageCellItem.getAllPatternsForGrid(grid);
        var groups = new ArrayList<RecipeTerminalSyncPacket.MachineGroupInfo>();
        var addedNames = new HashSet<String>();

        try {
            var machines = grid.getMachines(MeMachineInterfaceBlockEntity.class);
            if (machines != null) for (var machine : machines) {
                String name = machine.getInterfaceName();
                if (name != null && !name.isEmpty() && addedNames.add(name.toLowerCase(Locale.ROOT))) {
                    var level = machine.getLevel();
                    var icon = ItemStack.EMPTY;
                    if (level != null) {
                        var targetPos = machine.getBlockPos().relative(machine.getMachineDirection());
                        if (level.isLoaded(targetPos)) {
                            var state = level.getBlockState(targetPos);
                            if (!state.isAir()) icon = new ItemStack(state.getBlock().asItem());
                        }
                    }
                    groups.add(new RecipeTerminalSyncPacket.MachineGroupInfo(name, icon));
                }
            }
        } catch (Exception e) {
            Ae2craftcore.LOGGER.error("Failed to query ME Machine Interfaces on grid: ", e);
        }

        if (this.getPlayer() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new RecipeTerminalSyncPacket(list, groups));
        }
    }

    public void setPhantomSlotCount(int slotId, int count) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            var slot = this.getSlot(slotId);
            if (slot instanceof FakeSlot) {
                var stack = slot.getItem();
                if (!stack.isEmpty()) {
                    var copy = stack.copy();
                    copy.setCount(Math.clamp(count, 1, 999999));
                    slot.set(copy);
                }
            }
        }
    }

    public boolean isProcessingOutputSlot(Slot slot) {
        return this.processingOutputSlots.contains(slot);
    }

    public void setClientRecipes(List<ItemStack> recipes) {
        this.clientRecipes.clear();
        this.clientRecipes.addAll(recipes);
    }

    public List<ItemStack> getClientRecipes() {
        return this.clientRecipes;
    }

    public void setClientGroups(List<RecipeTerminalSyncPacket.MachineGroupInfo> groups) {
        this.clientGroups.clear();
        this.clientGroups.addAll(groups);
    }

    public List<RecipeTerminalSyncPacket.MachineGroupInfo> getClientGroups() {
        return this.clientGroups;
    }

    public RecipeTerminalPart getPart() {
        return this.part;
    }

    public int getProcessingScrollOffset() {
        return this.processingScrollOffset;
    }

    public void setProcessingScrollOffset(int scrollOffset) {
        this.processingScrollOffset = scrollOffset;
        this.updateProcessingSlots();
    }

    public void updateProcessingSlots() {
        int row = 0;
        int col = 0;
        for (var slot : this.processingInputSlots) {
            int effectiveRow = row - this.processingScrollOffset;
            ((SlotAccessor) slot).ae2craftcore$setY(ENCODING_SLOT_Y + 7 + effectiveRow * 18);
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }
        for (int i = 0; i < this.processingOutputSlots.size(); i++) {
            var slot = this.processingOutputSlots.get(i);
            int effectiveRow = i - this.processingScrollOffset;
            ((SlotAccessor) slot).ae2craftcore$setY(ENCODING_SLOT_Y + 7 + effectiveRow * 18);
        }
    }

    public String getSelectedGroup() {
        return this.selectedGroup;
    }

    public void setSelectedGroup(String group) {
        this.selectedGroup = group != null ? group : "";
    }

    public EncodingMode getEncodingMode() {
        return this.mode;
    }

    public void setEncodingMode(EncodingMode mode) {
        this.mode = mode != null ? mode : EncodingMode.PROCESSING;
        if (this.part != null) this.part.getLogic().setMode(this.mode);
    }

    public void clearEncodingSlots() {
        this.part.getLogic().getEncodedInputInv().clear();
        this.part.getLogic().getEncodedOutputInv().clear();
    }

    public boolean canCycleProcessingOutputs() {
        if (this.mode != EncodingMode.PROCESSING) return false;

        int nonEmptyCount = 0;
        for (var processingOutputSlot : this.processingOutputSlots) {
            if (!processingOutputSlot.getItem().isEmpty()) {
                nonEmptyCount++;
                if (nonEmptyCount > 1) return true;
            }
        }
        return false;
    }

    public void cycleProcessingOutputs() {
        if (this.mode != EncodingMode.PROCESSING) return;

        int size = this.processingOutputSlots.size();
        var newOutputs = new ItemStack[size];

        for (int i = 0; i < size; i++) {
            newOutputs[i] = ItemStack.EMPTY;
            if (!this.processingOutputSlots.get(i).getItem().isEmpty()) {
                boolean found = false;
                for (int j = i + 1; j < size; j++) {
                    var nextItem = this.processingOutputSlots.get(j).getItem();
                    if (!nextItem.isEmpty()) {
                        newOutputs[i] = nextItem;
                        found = true;
                        break;
                    }
                }
                if (!found) for (int j = 0; j < i; j++) {
                    var nextItem = this.processingOutputSlots.get(j).getItem();
                    if (!nextItem.isEmpty()) {
                        newOutputs[i] = nextItem;
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < newOutputs.length; i++) this.processingOutputSlots.get(i).set(newOutputs[i]);
    }

    @Override
    public void initializeContents(int stateId, @NotNull List<ItemStack> items, @NotNull ItemStack carried) {
        if (items.size() > this.slots.size()) items = items.subList(0, this.slots.size());
        super.initializeContents(stateId, items, carried);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        var slot = this.slots.get(index);
        if (slot instanceof RecipeResultSlot) return ItemStack.EMPTY;
        var itemstack = ItemStack.EMPTY;
        if (slot.hasItem()) {
            var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            int playerInventoryStart = 123;
            int hotbarStart = playerInventoryStart + 27;
            int hotbarEnd = hotbarStart + 9;

            if (index >= playerInventoryStart && index < hotbarStart) {
                if (!this.moveItemStackTo(itemstack1, hotbarStart, hotbarEnd, false)) return ItemStack.EMPTY;
            } else if (index >= hotbarStart && index < hotbarEnd) {
                if (!this.moveItemStackTo(itemstack1, playerInventoryStart, hotbarStart, false)) return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    public ItemStack getCraftingResult() {
        var player = this.getPlayer();
        if (player == null) return ItemStack.EMPTY;
        var level = player.level();

        var grid = NonNullList.withSize(9, ItemStack.EMPTY);
        var hasInput = false;

        for (int i = 0; i < 9; i++) {
            var slot = this.craftingInputSlots[i];
            var itemStack = slot.getItem();
            if (!itemStack.isEmpty()) {
                grid.set(i, itemStack.copyWithCount(1));
                hasInput = true;
            }
        }
        if (!hasInput) return ItemStack.EMPTY;

        var input = CraftingInput.of(3, 3, grid);
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElse(null);
        if (recipe == null) return ItemStack.EMPTY;
        return recipe.value().assemble(input, level.registryAccess());
    }

    public ItemStack getSmithingResult() {
        var player = this.getPlayer();
        if (player == null) return ItemStack.EMPTY;
        var level = player.level();

        var templateStack = this.smithingTemplateSlot.getItem();
        var baseStack = this.smithingBaseSlot.getItem();
        var additionStack = this.smithingAdditionSlot.getItem();

        if (templateStack.isEmpty() || baseStack.isEmpty() || additionStack.isEmpty()) return ItemStack.EMPTY;

        var input = new SmithingRecipeInput(templateStack, baseStack, additionStack);
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, level).orElse(null);
        if (recipe == null) return ItemStack.EMPTY;
        return recipe.value().assemble(input, level.registryAccess());
    }

    public class RecipeTerminalPhantomSlot extends FakeSlot {
        final EncodingMode mode;

        public RecipeTerminalPhantomSlot(InternalInventory inv, int index, int x, int y, EncodingMode mode) {
            super(inv, index);
            ((SlotAccessor) this).ae2craftcore$setX(x);
            ((SlotAccessor) this).ae2craftcore$setY(y);
            this.mode = mode;
        }

        public EncodingMode getMode() {
            return this.mode;
        }

        @Override
        public int getMaxStackSize() {
            return this.mode == EncodingMode.PROCESSING ? 999999 : 1;
        }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack) {
            return this.mode == EncodingMode.PROCESSING ? 999999 : 1;
        }

        @Override
        public boolean isActive() {
            return RecipeTerminalMenu.this.mode == this.mode;
        }
    }

    public void selectStonecutterRecipeOnServer(ResourceLocation recipeId) {
        this.sendClientAction(SETSTONECUTTINGRECIPEID, recipeId);
    }

    public class RecipeTerminalProcessingInputSlot extends FakeSlot {
        public RecipeTerminalProcessingInputSlot(InternalInventory inv, int index, int x, int y) {
            super(inv, index);
            ((SlotAccessor) this).ae2craftcore$setX(x);
            ((SlotAccessor) this).ae2craftcore$setY(y);
        }

        @Override
        public int getMaxStackSize() {
            return 999999;
        }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack) {
            return 999999;
        }

        @Override
        public boolean isActive() {
            if (RecipeTerminalMenu.this.mode != EncodingMode.PROCESSING) return false;
            int row = this.getContainerSlot() / 3;
            int scroll = RecipeTerminalMenu.this.processingScrollOffset;
            int effectiveRow = row - scroll;
            return effectiveRow >= 0 && effectiveRow < 3;
        }
    }

    public class RecipeTerminalProcessingOutputSlot extends FakeSlot {
        public RecipeTerminalProcessingOutputSlot(InternalInventory inv, int index, int x, int y) {
            super(inv, index);
            ((SlotAccessor) this).ae2craftcore$setX(x);
            ((SlotAccessor) this).ae2craftcore$setY(y);
        }

        @Override
        public int getMaxStackSize() {
            return 999999;
        }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack) {
            return 999999;
        }

        @Override
        public boolean isActive() {
            if (RecipeTerminalMenu.this.mode != EncodingMode.PROCESSING) return false;
            int row = this.getContainerSlot();
            int scroll = RecipeTerminalMenu.this.processingScrollOffset;
            int effectiveRow = row - scroll;
            return effectiveRow >= 0 && effectiveRow < 3;
        }
    }

    public class RecipeResultSlot extends Slot {
        private final EncodingMode mode;

        public RecipeResultSlot(EncodingMode mode, int x, int y) {
            super(new SimpleContainer(1), 0, x, y);
            this.mode = mode;
        }

        @Override
        public @NotNull ItemStack getItem() {
            if (this.mode == EncodingMode.CRAFTING) {
                return RecipeTerminalMenu.this.getCraftingResult();
            } else if (this.mode == EncodingMode.SMITHING_TABLE) {
                return RecipeTerminalMenu.this.getSmithingResult();
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(@NotNull Player player) {
            return false;
        }

        @Override
        public boolean isActive() {
            return RecipeTerminalMenu.this.mode == this.mode;
        }
    }

    @Override
    public IClientRepo getClientRepo() {
        var repo = super.getClientRepo();
        if (repo != null) return repo;

        if (this.cachedProxyRepo == null) try {
            this.cachedProxyRepo = (IClientRepo) Proxy.newProxyInstance(RecipeTerminalMenu.class.getClassLoader(), new Class<?>[]{IClientRepo.class}, (proxy, method, args) -> {
                var retType = method.getReturnType();
                if (method.getName().equals("getAllEntries")) {
                    if (retType == Set.class) return Collections.emptySet();
                    return Collections.emptyList();
                }
                if (retType == Set.class) return Collections.emptySet();
                if (retType == Collection.class || retType == List.class) return Collections.emptyList();
                if (retType == void.class) return null;
                if (retType == boolean.class) return false;
                if (retType == int.class) return 0;
                if (retType == long.class) return 0L;
                if (retType == float.class) return 0.0f;
                if (retType == double.class) return 0.0d;
                return null;
            });
        } catch (Throwable e) {
            return null;
        }

        return this.cachedProxyRepo;
    }

    @Override
    public @NotNull MenuType<?> getType() {
        if (this.isClientSide()) return PatternEncodingTermMenu.TYPE;
        return RECIPE_TERMINAL.get();
    }

    private void syncParentSlots() {
        try {
            FakeSlot[] parentCrafting = this.getCraftingGridSlots();
            for (int i = 0; i < 9; i++) {
                if (i < parentCrafting.length) parentCrafting[i] = (FakeSlot) this.craftingInputSlots[i];
            }

            FakeSlot[] parentProcessingInputs = this.getProcessingInputSlots();
            int inputSize = Math.min(parentProcessingInputs.length, this.processingInputSlots.size());
            for (int i = 0; i < inputSize; i++) {
                parentProcessingInputs[i] = (FakeSlot) this.processingInputSlots.get(i);
            }

            FakeSlot[] parentProcessingOutputs = this.getProcessingOutputSlots();
            int outputSize = Math.min(parentProcessingOutputs.length, this.processingOutputSlots.size());
            for (int i = 0; i < outputSize; i++) {
                parentProcessingOutputs[i] = (FakeSlot) this.processingOutputSlots.get(i);
            }

            setPrivateParentField("smithingTableTemplateSlot", this.smithingTemplateSlot);
            setPrivateParentField("smithingTableBaseSlot", this.smithingBaseSlot);
            setPrivateParentField("smithingTableAdditionSlot", this.smithingAdditionSlot);
            setPrivateParentField("stonecuttingInputSlot", this.stonecutterInputSlot);

        } catch (Exception e) {
            Ae2craftcore.LOGGER.error("Failed to sync slots with parent PatternEncodingTermMenu: ", e);
        }
    }

    private void setPrivateParentField(String fieldName, Object value) throws Exception {
        var field = PatternEncodingTermMenu.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(this, value);
    }
}