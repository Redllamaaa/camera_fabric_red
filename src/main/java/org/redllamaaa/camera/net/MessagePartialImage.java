
package org.redllamaaa.camera.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.redllamaaa.camera.CameraMod;

import java.util.UUID;

public record MessagePartialImage(UUID imgUUID, int offset, int length, byte[] bytes)
        implements CustomPacketPayload {

    private static final int MAX_CHUNK_SIZE = 1_000_000;

    public static final Type<MessagePartialImage> TYPE = new Type<>(
            CameraMod.id("partial_image")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePartialImage> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeUUID(payload.imgUUID());
                        buf.writeInt(payload.offset());
                        buf.writeInt(payload.length());
                        buf.writeByteArray(payload.bytes());
                    },
                    buf -> {
                        UUID imgUUID = buf.readUUID();
                        int offset = buf.readInt();
                        int length = buf.readInt();
                        byte[] bytes = buf.readByteArray(MAX_CHUNK_SIZE);

                        return new MessagePartialImage(imgUUID, offset, length, bytes);
                    }
            );

    public MessagePartialImage {
        if (offset < 0
                || length <= 0
                || offset > length
                || bytes.length == 0
                || bytes.length > MAX_CHUNK_SIZE
                || bytes.length > length - offset) {
            throw new IllegalArgumentException("Invalid partial image chunk");
        }
    }

    @Override
    public Type<MessagePartialImage> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            ServerPlayer sender = context.player();

            CameraMod.PACKET_MANAGER.addBytes(
                    sender,
                    payload.imgUUID(),
                    payload.offset(),
                    payload.length(),
                    payload.bytes()
            );
        });
    }
}
