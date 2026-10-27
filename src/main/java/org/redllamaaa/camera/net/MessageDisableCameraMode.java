
package org.redllamaaa.camera.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.redllamaaa.camera.CameraMod;

public record MessageDisableCameraMode() implements CustomPacketPayload {

    public static final Type<MessageDisableCameraMode> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(CameraMod.MODID, "disable_camera_mode")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageDisableCameraMode> CODEC =
            StreamCodec.unit(new MessageDisableCameraMode());

    @Override
    public Type<MessageDisableCameraMode> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            ServerPlayer sender = context.player();

            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = sender.getItemInHand(hand);

                if (stack.is(CameraMod.CAMERA)) {
                    CameraMod.CAMERA.setActive(stack, false);
                }
            }
        });
    }
}
