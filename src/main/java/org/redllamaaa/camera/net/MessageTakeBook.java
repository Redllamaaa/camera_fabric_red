
package org.redllamaaa.camera.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.gui.AlbumContainer;

public record MessageTakeBook() implements CustomPacketPayload {

    public static final Type<MessageTakeBook> TYPE = new Type<>(
            CameraMod.id("take_book")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageTakeBook> CODEC =
            StreamCodec.unit(new MessageTakeBook());

    @Override
    public Type<MessageTakeBook> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            ServerPlayer sender = context.player();

            if (sender.containerMenu instanceof AlbumContainer container) {
                container.takeBook(sender);
            }
        });
    }
}
