package org.ae2craftcore.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.ae2craftcore.blocks.block.CryostatBlock;
import org.ae2craftcore.multiblock.MultiblockValidator;
import org.ae2craftcore.registry.annotations.RegisterBlockEntity;
import org.jetbrains.annotations.NotNull;

@RegisterBlockEntity(name = "cryostat", blocks = {CryostatBlock.class})
public class CryostatBlockEntity extends BlockEntity {
    public static BlockEntityType<CryostatBlockEntity> TYPE;

    private boolean structureValid = false;

    public CryostatBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public void checkStructureAndNotify(Player player) {
        if (this.level == null) return;

        var result = MultiblockValidator.validate(this.level, this.worldPosition);
        this.structureValid = result.isValid();
        this.setChanged();

        if (this.structureValid) {
            player.sendSystemMessage(Component.literal("§a[Cryostat] Multiblock structure is valid!"));
        } else {
            String relCoordStr = String.format("(%d, %d, %d)", result.relativePos().getX(), result.relativePos().getY(), result.relativePos().getZ());
            String absCoordStr = String.format("(%d, %d, %d)", result.absolutePos().getX(), result.absolutePos().getY(), result.absolutePos().getZ());
            player.sendSystemMessage(Component.literal(String.format("§c[Cryostat] Structure invalid at relative %s, absolute %s: %s",
                    relCoordStr, absCoordStr, result.errorReason())));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("StructureValid", this.structureValid);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.structureValid = tag.getBoolean("StructureValid");
    }
}