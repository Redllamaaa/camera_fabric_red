
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
            Identifier.fromNamespaceAndPath(CameraMod.MODID, "request_upload")
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

            if (CameraMod.PACKET_MANAGER.canTakeImage(sender.getUUID())) {
                if (CameraItem.consumePaper(sender)) {
                    context.responseSender().sendPacket(
                            new MessageUploadCustomImage(payload.uuid())
                    );
                } else {
                    sender.sendOverlayMessage(Component.translatable("message.no_consumable"));
                }
            } else {
                sender.sendOverlayMessage(Component.translatable("message.image_cooldown"));
            }
        });
    }
}
