package org.redllamaaa.camera.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.gui.AlbumContainer;

public record MessageAlbumPage(int page) implements CustomPacketPayload {

    public static final Type<MessageAlbumPage> TYPE = new Type<>(
            CameraMod.id("album_page")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageAlbumPage> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    MessageAlbumPage::page,
                    MessageAlbumPage::new
            );

    @Override
    public Type<MessageAlbumPage> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) ->
                context.server().execute(() -> {
                    ServerPlayer sender = context.player();
                    if (sender.containerMenu instanceof AlbumContainer container) {
                        container.setPage(payload.page());
                    }
                })
        );
    }
}
