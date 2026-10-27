package org.redllamaaa.camera.integration.waila;

import org.redllamaaa.camera.ImageData;
import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.entities.ImageEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

import java.util.Date;

public class HUDHandlerImageFrame implements IEntityComponentProvider {

    private static final Identifier OBJECT_NAME_TAG = Identifier.fromNamespaceAndPath("jade", "object_name");

    public static final HUDHandlerImageFrame INSTANCE = new HUDHandlerImageFrame();

    private static final Identifier UID = CameraMod.id("image_frame");

    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        if (entityAccessor.getEntity() instanceof ImageEntity image) {
            iTooltip.remove(OBJECT_NAME_TAG);
            iTooltip.add(image.getDisplayName().copy().withStyle(ChatFormatting.WHITE));
            ImageData imageData = ImageData.fromStack(image.getItem());
            if (imageData == null) {
                iTooltip.add(Component.translatable("tooltip.image_frame_empty"));
                return;
            }
            if (!imageData.getOwner().isEmpty()) {
                iTooltip.add(Component.translatable("tooltip.image_owner", ChatFormatting.DARK_GRAY + imageData.getOwner()).withStyle(ChatFormatting.GRAY));
            }
            if (imageData.getTime() > 0L) {
                iTooltip.add(Component.translatable("tooltip.image_time", ChatFormatting.DARK_GRAY + CameraMod.CLIENT_CONFIG.imageDateFormat.format(new Date(imageData.getTime()))).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
