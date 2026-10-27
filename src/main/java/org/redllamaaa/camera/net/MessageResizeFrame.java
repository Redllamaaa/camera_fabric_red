
package org.redllamaaa.camera.net;

import io.netty.handler.codec.DecoderException;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.entities.ImageEntity;

import java.util.UUID;

public record MessageResizeFrame(UUID uuid, Direction direction, boolean larger)
        implements CustomPacketPayload {

    public static final Type<MessageResizeFrame> TYPE = new Type<>(
            CameraMod.id("resize_frame")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageResizeFrame> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeUUID(payload.uuid());
                        buf.writeVarInt(payload.direction().ordinal());
                        buf.writeBoolean(payload.larger());
                    },
                    buf -> {
                        UUID uuid = buf.readUUID();
                        int ordinal = buf.readVarInt();

                        if (ordinal < 0 || ordinal >= Direction.values().length) {
                            throw new DecoderException("Invalid frame resize direction: " + ordinal);
                        }

                        return new MessageResizeFrame(
                                uuid,
                                Direction.values()[ordinal],
                                buf.readBoolean()
                        );
                    }
            );

    @Override
    public Type<MessageResizeFrame> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            ServerPlayer sender = context.player();

            if (!sender.getAbilities().mayBuild) {
                return;
            }

            if (!(sender.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            Entity entity = serverLevel.getEntity(payload.uuid());

            if (entity instanceof ImageEntity image) {
                image.resize(payload.direction(), payload.larger());
            }
        });
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
}
