package org.redllamaaa.camera.net;

import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.gui.AlbumContainer;
import org.redllamaaa.corelib.net.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MessageTakeBook implements Message<MessageTakeBook> {

    public static final CustomPacketPayload.Type<MessageTakeBook> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CameraMod.MODID, "take_book"));

    public MessageTakeBook() {

    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.SERVERBOUND;
    }

    @Override
    public void executeServerSide(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }
        if (sender.containerMenu instanceof AlbumContainer container) {
            container.takeBook(sender);
        }
    }

    @Override
    public MessageTakeBook fromBytes(RegistryFriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
    }

    @Override
    public Type<MessageTakeBook> type() {
        return TYPE;
    }

}
