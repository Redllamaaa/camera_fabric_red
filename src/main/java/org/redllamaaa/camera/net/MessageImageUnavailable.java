
package org.redllamaaa.camera.net;

import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.TextureCache;

import java.awt.image.BufferedImage;
import java.util.UUID;

public record MessageImageUnavailable(UUID imgUUID) implements CustomPacketPayload {

    public static final Type<MessageImageUnavailable> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(CameraMod.MODID, "image_unavailable")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageImageUnavailable> CODEC =
            StreamCodec.composite(
                    net.minecraft.core.UUIDUtil.STREAM_CODEC,
                    MessageImageUnavailable::imgUUID,
                    MessageImageUnavailable::new
            );

    @Override
    public Type<MessageImageUnavailable> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);

        ClientPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            TextureCache.instance().addImage(
                    payload.imgUUID(),
                    new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
            );
        });
    }
}
