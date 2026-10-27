
package org.redllamaaa.camera.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.redllamaaa.camera.CameraMod;

public record MessageSetShader(String shader) implements CustomPacketPayload {

    public static final Type<MessageSetShader> TYPE = new Type<>(
            CameraMod.id("set_shader")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSetShader> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.stringUtf8(128),
                    MessageSetShader::shader,
                    MessageSetShader::new
            );

    @Override
    public Type<MessageSetShader> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            ServerPlayer sender = context.player();

            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = sender.getItemInHand(hand);

                if (stack.is(CameraMod.CAMERA)) {
                    stack.set(CameraMod.SHADER_DATA_COMPONENT, payload.shader());
                }
            }
        });
    }
}
