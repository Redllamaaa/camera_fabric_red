
package org.redllamaaa.camera.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.ImageTools;

import java.io.IOException;
import java.util.UUID;

public record MessageRequestImage(UUID imgUUID) implements CustomPacketPayload {

    public static final Type<MessageRequestImage> TYPE = new Type<>(
            CameraMod.id("request_image")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageRequestImage> CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    MessageRequestImage::imgUUID,
                    MessageRequestImage::new
            );

    @Override
    public Type<MessageRequestImage> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            ServerPlayer sender = context.player();

            try {
                byte[] data = ImageTools.toBytes(
                        CameraMod.PACKET_MANAGER.getExistingImage(sender, payload.imgUUID())
                );

                context.responseSender().sendPacket(new MessageImage(payload.imgUUID(), data));
            } catch (IOException | IllegalArgumentException e) {
                CameraMod.LOGGER.error("Failed to retrieve image {}", payload.imgUUID(), e);
                context.responseSender().sendPacket(
                        new MessageImageUnavailable(payload.imgUUID())
                );
            }
        });
    }
}
