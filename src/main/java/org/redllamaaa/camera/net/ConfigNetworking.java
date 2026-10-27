package org.redllamaaa.camera.net;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.redllamaaa.camera.CameraMod;

public final class ConfigNetworking {
    private ConfigNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(
                ConfigSyncPayload.Open.TYPE,
                ConfigSyncPayload.Open.CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
                ConfigSyncPayload.Update.TYPE,
                ConfigSyncPayload.Update.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ConfigSyncPayload.Update.TYPE,
                (payload, context) -> {
                    ServerPlayer player = context.player();

                    if (!player.hasPermissions(2)) {
                        player.sendSystemMessage(Component.literal(
                                "You do not have permission to change camera settings."
                        ));
                        return;
                    }

                    var config = CameraMod.SERVER_CONFIG;

                    config.imageCooldown.set(Math.clamp(payload.imageCooldown(), 100, Integer.MAX_VALUE));
                    config.cameraConsumeItemAmount.set(
                            Math.clamp(payload.cameraConsumeItemAmount(), 1, Short.MAX_VALUE)
                    );
                    config.maxImageSize.set(Math.clamp(payload.maxImageSize(), 50_000, 1_000_000));
                    config.imageCompression.set(
                            Double.isFinite(payload.imageCompression())
                                    ? Math.clamp(payload.imageCompression(), 0.1D, 1.0D)
                                    : 0.5D
                    );
                    config.allowImageUpload.set(payload.allowImageUpload());
                    config.frameOnlyOwnerModify.set(payload.frameOnlyOwnerModify());
                    config.advancedImageData.set(payload.advancedImageData());
                    config.advancedDataMaxEntities.set(
                            Math.clamp(payload.advancedDataMaxEntities(), 1, 128)
                    );

                    config.save();

                    player.sendSystemMessage(Component.literal("Camera settings saved."));
                    ServerPlayNetworking.send(player, createOpenPayload());
                }
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        Commands.literal("camera")
                                .then(Commands.literal("config")
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            ServerPlayNetworking.send(player, createOpenPayload());
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                )
        );
    }

    private static ConfigSyncPayload.Open createOpenPayload() {
        var config = CameraMod.SERVER_CONFIG;

        return new ConfigSyncPayload.Open(
                config.imageCooldown.get(),
                config.cameraConsumeItemAmount.get(),
                config.maxImageSize.get(),
                config.imageCompression.get(),
                config.allowImageUpload.get(),
                config.frameOnlyOwnerModify.get(),
                config.advancedImageData.get(),
                config.advancedDataMaxEntities.get()
        );
    }
}