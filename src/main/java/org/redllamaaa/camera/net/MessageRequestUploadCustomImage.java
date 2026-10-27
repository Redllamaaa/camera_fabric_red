
package org.redllamaaa.camera.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.items.CameraItem;

import java.util.UUID;

public record MessageRequestUploadCustomImage(UUID uuid) implements CustomPacketPayload {

    public static final Type<MessageRequestUploadCustomImage> TYPE = new Type<>(
            CameraMod.id("request_upload")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageRequestUploadCustomImage> CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    MessageRequestUploadCustomImage::uuid,
                    MessageRequestUploadCustomImage::new
            );

    @Override
    public Type<MessageRequestUploadCustomImage> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            ServerPlayer sender = context.player();

            if (!CameraMod.SERVER_CONFIG.allowImageUpload.get()) {
                sender.sendOverlayMessage(Component.translatable("message.upload_disabled"));
            } else if (CameraMod.PACKET_MANAGER.isImageOnCooldown(sender.getUUID())) {
                sender.sendOverlayMessage(Component.translatable("message.image_cooldown"));
            } else {
                if (!CameraMod.PACKET_MANAGER.authorizeUpload(sender, payload.uuid())) {
                    sender.sendOverlayMessage(Component.translatable("message.image_upload_busy"));
                } else if (CameraItem.consumePaper(sender)) {
                    CameraMod.PACKET_MANAGER.recordImageTaken(sender.getUUID());
                    context.responseSender().sendPacket(
                            new MessageUploadCustomImage(payload.uuid())
                    );
                } else {
                    CameraMod.PACKET_MANAGER.cancelAuthorizedUpload(sender, payload.uuid());
                    sender.sendOverlayMessage(Component.translatable("message.no_consumable"));
                }
            }
        });
    }
}
