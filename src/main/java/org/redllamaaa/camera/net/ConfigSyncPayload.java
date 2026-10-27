package org.redllamaaa.camera.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.redllamaaa.camera.CameraMod;

public final class ConfigSyncPayload {
    private ConfigSyncPayload() {
    }

    public record Open(
            int imageCooldown,
            int cameraConsumeItemAmount,
            int maxImageSize,
            double imageCompression,
            boolean allowImageUpload,
            boolean frameOnlyOwnerModify,
            boolean advancedImageData,
            int advancedDataMaxEntities
    ) implements CustomPacketPayload {
        public static final Type<Open> TYPE = new Type<>(CameraMod.id("config_open"));

        public static final StreamCodec<RegistryFriendlyByteBuf, Open> CODEC = StreamCodec.of(
                (buf, value) -> {
                    buf.writeVarInt(value.imageCooldown());
                    buf.writeVarInt(value.cameraConsumeItemAmount());
                    buf.writeVarInt(value.maxImageSize());
                    buf.writeDouble(value.imageCompression());
                    buf.writeBoolean(value.allowImageUpload());
                    buf.writeBoolean(value.frameOnlyOwnerModify());
                    buf.writeBoolean(value.advancedImageData());
                    buf.writeVarInt(value.advancedDataMaxEntities());
                },
                buf -> new Open(
                        buf.readVarInt(),
                        buf.readVarInt(),
                        buf.readVarInt(),
                        buf.readDouble(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readVarInt()
                )
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Update(
            int imageCooldown,
            int cameraConsumeItemAmount,
            int maxImageSize,
            double imageCompression,
            boolean allowImageUpload,
            boolean frameOnlyOwnerModify,
            boolean advancedImageData,
            int advancedDataMaxEntities
    ) implements CustomPacketPayload {
        public static final Type<Update> TYPE = new Type<>(CameraMod.id("config_update"));

        public static final StreamCodec<RegistryFriendlyByteBuf, Update> CODEC = StreamCodec.of(
                (buf, value) -> {
                    buf.writeVarInt(value.imageCooldown());
                    buf.writeVarInt(value.cameraConsumeItemAmount());
                    buf.writeVarInt(value.maxImageSize());
                    buf.writeDouble(value.imageCompression());
                    buf.writeBoolean(value.allowImageUpload());
                    buf.writeBoolean(value.frameOnlyOwnerModify());
                    buf.writeBoolean(value.advancedImageData());
                    buf.writeVarInt(value.advancedDataMaxEntities());
                },
                buf -> new Update(
                        buf.readVarInt(),
                        buf.readVarInt(),
                        buf.readVarInt(),
                        buf.readDouble(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readVarInt()
                )
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}