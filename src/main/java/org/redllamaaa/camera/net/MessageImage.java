
package org.redllamaaa.camera.net;

import io.netty.handler.codec.DecoderException;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.ImageTools;
import org.redllamaaa.camera.TextureCache;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

public record MessageImage(UUID uuid, byte[] image) implements CustomPacketPayload {

    private static final int MAX_IMAGE_SIZE = 1_000_000;

    public static final Type<MessageImage> TYPE = new Type<>(
            CameraMod.id("image")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageImage> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        if (payload.image().length > MAX_IMAGE_SIZE) {
                            throw new DecoderException("Image too large: " + payload.image().length);
                        }

                        buf.writeUUID(payload.uuid());
                        buf.writeByteArray(payload.image());
                    },
                    buf -> {
                        UUID uuid = buf.readUUID();
                        byte[] image = buf.readByteArray(MAX_IMAGE_SIZE);

                        return new MessageImage(uuid, image);
                    }
            );

    public MessageImage {
        if (image.length > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Image too large: " + image.length + " bytes (max " + MAX_IMAGE_SIZE + ")"
            );
        }
    }

    @Override
    public Type<MessageImage> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);

        ClientPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            try {
                BufferedImage img = ImageTools.fromBytes(payload.image());

                Minecraft.getInstance().execute(
                        () -> TextureCache.instance().addImage(payload.uuid(), img)
                );
            } catch (IOException e) {
                CameraMod.LOGGER.error("Failed to decode received image {}", payload.uuid(), e);
            }
        });
    }
}
