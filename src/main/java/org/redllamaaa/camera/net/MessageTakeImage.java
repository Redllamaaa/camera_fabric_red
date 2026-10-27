
package org.redllamaaa.camera.net;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.ImageTaker;

import java.util.UUID;

public record MessageTakeImage(UUID uuid) implements CustomPacketPayload {

    public static final Type<MessageTakeImage> TYPE = new Type<>(
            CameraMod.id("take_image")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageTakeImage> CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    MessageTakeImage::uuid,
                    MessageTakeImage::new
            );

    @Override
    public Type<MessageTakeImage> type() {
        return TYPE;
    }

    public static void registerType() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }

    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) ->
                context.client().execute(() -> ImageTaker.takeScreenshot(payload.uuid()))
        );
    }

    public static void sendToPlayer(net.minecraft.server.level.ServerPlayer player, UUID uuid) {
        ServerPlayNetworking.send(player, new MessageTakeImage(uuid));
    }
}
