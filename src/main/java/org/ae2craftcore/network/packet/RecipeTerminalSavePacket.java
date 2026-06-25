package org.ae2craftcore.network.packet;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.ae2craftcore.Ae2craftcore;
import org.ae2craftcore.blocks.menu.RecipeTerminalMenu;
import org.ae2craftcore.registry.annotations.NetworkPayload;
import org.ae2craftcore.registry.annotations.PacketHandler;
import org.ae2craftcore.registry.annotations.PayloadDirection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@NetworkPayload(direction = PayloadDirection.TO_SERVER)
public record RecipeTerminalSavePacket(String groupName) implements CustomPacketPayload {

    public static final Type<RecipeTerminalSavePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Ae2craftcore.MODID, "recipe_terminal_save"));

    @SuppressWarnings("unused")
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

                    var part = menu.getPart();
                    if (part == null) return;
                    var node = part.getGridNode();
                    if (node == null) return;
                    var grid = node.getGrid();
                    if (grid == null) return;
                    var storage = grid.getStorageService();
                    if (storage == null) return;
                    var inv = storage.getInventory();
                    if (inv == null) return;

                    var key = AEItemKey.of(encodedPattern);
                    inv.insert(key, 1, appeng.api.config.Actionable.MODULATE, new appeng.me.helpers.PlayerSource(player));

                    menu.syncRecipesToClient();
                } catch (Exception e) {
                    Ae2craftcore.LOGGER.error("Failed to encode and save virtual recipe: ", e);
                }
            }
        });
    }
}