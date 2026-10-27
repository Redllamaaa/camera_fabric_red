
package org.redllamaaa.camera.net;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.ClientImageUploadManager;
import org.redllamaaa.camera.ImageProcessor;

import java.awt.image.BufferedImage;
import java.util.UUID;

public record MessageUploadCustomImage(UUID uuid) implements CustomPacketPayload {

    public static final Type<MessageUploadCustomImage> TYPE = new Type<>(
            CameraMod.id("upload")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageUploadCustomImage> CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    MessageUploadCustomImage::uuid,
                    MessageUploadCustomImage::new
            );

    @Override
    public Type<MessageUploadCustomImage> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);

        ClientPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            BufferedImage image = ClientImageUploadManager.getAndRemoveImage(payload.uuid());

            if (image != null) {
                ImageProcessor.sendScreenshotThreaded(payload.uuid(), image);
            }
        });
    }
}
