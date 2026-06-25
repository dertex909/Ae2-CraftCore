package org.ae2craftcore.network.packet;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.registry.AttachmentRegistry;
import org.ae2craftcore.registry.annotations.NetworkPayload;
import org.ae2craftcore.registry.annotations.PacketHandler;
import org.ae2craftcore.registry.annotations.PayloadDirection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@NetworkPayload(direction = PayloadDirection.TO_SERVER)
public record RecipeTerminalSavePacket(String groupName) implements CustomPacketPayload {

    public static final Type<RecipeTerminalSavePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_save"));

    public static final StreamCodec<FriendlyByteBuf, RecipeTerminalSavePacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> buf.writeUtf(value.groupName()),
            buf -> new RecipeTerminalSavePacket(buf.readUtf())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @PacketHandler
    public static void handle(RecipeTerminalSavePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu instanceof RecipeTerminalMenu menu) {
                var cellStack = menu.getSlot(12).getItem();
                if (cellStack.isEmpty()) return;

                var inputsList = new ArrayList<GenericStack>();
                for (int i = 0; i < 9; i++) {
                    var stack = menu.getPhantomContainer().getItem(i);
                    if (!stack.isEmpty()) inputsList.add(new GenericStack(AEItemKey.of(stack), stack.getCount()));
                }

                var outputsList = new ArrayList<GenericStack>();
                for (int i = 0; i < 3; i++) {
                    var stack = menu.getPhantomContainer().getItem(9 + i);
                    if (!stack.isEmpty()) outputsList.add(new GenericStack(AEItemKey.of(stack), stack.getCount()));
                }

                if (inputsList.isEmpty() || outputsList.isEmpty()) return;

                try {
                    var encodedPattern = PatternDetailsHelper.encodeProcessingPattern(inputsList, outputsList);
                    if (encodedPattern == null || encodedPattern.isEmpty()) return;

                    var customData = encodedPattern.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    var tag = customData.copyTag();
                    tag.putString("RecipeMachineGroup", packet.groupName());
                    encodedPattern.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                    var cellCustomData = cellStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    var cellTag = cellCustomData.copyTag();
                    if (!cellTag.hasUUID("RecipeCellUUID")) {
                        cellTag.putUUID("RecipeCellUUID", UUID.randomUUID());
                        cellStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cellTag));
                    }

                    var currentList = cellStack.get(AttachmentRegistry.STORED_PATTERNS.get());
                    var newList = new ArrayList<ItemStack>();
                    if (currentList != null) newList.addAll(currentList);
                    newList.add(encodedPattern);

                    cellStack.set(AttachmentRegistry.STORED_PATTERNS.get(), List.copyOf(newList));
                    cellStack.set(AttachmentRegistry.RECIPE_COUNT.get(), newList.size());

                    var uniqueTypes = new HashSet<Item>();
                    for (var p : newList) uniqueTypes.add(p.getItem());
                    cellStack.set(AttachmentRegistry.MACHINE_COUNT.get(), uniqueTypes.size());

                    menu.getSlot(12).setChanged();
                } catch (Exception e) {
                    Ae2craftcore.LOGGER.error("Failed to encode and save virtual recipe: ", e);
                }
            }
        });
    }
}