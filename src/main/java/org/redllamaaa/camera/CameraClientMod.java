
package org.redllamaaa.camera;

import org.redllamaaa.camera.entities.ImageRenderer;
import org.redllamaaa.camera.gui.*;
import org.redllamaaa.camera.items.render.ImageSpecialRenderer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.redllamaaa.camera.gui.CameraConfigScreen;
import org.redllamaaa.camera.net.ConfigSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.redllamaaa.camera.net.MessageImage;
import org.redllamaaa.camera.net.MessageImageUnavailable;
import org.redllamaaa.camera.net.MessageTakeImage;
import org.redllamaaa.camera.net.MessageUploadCustomImage;
import java.util.List;
import java.util.UUID;

public class CameraClientMod implements ClientModInitializer {

    public static KeyMapping KEY_NEXT;
    public static KeyMapping KEY_PREVIOUS;

    @Override
    public void onInitializeClient() {
        CameraMod.CLIENT_CONFIG.load();
        registerConfigNetworking();

        registerKeyBinds();
        registerRenderers();
        registerItemModels();
        registerScreens();
        ImageTaker.register();
        MessageImage.register();
        MessageImageUnavailable.register();
        MessageTakeImage.registerClientReceiver();
        MessageUploadCustomImage.register();

        ClientEvents.register();

    }

    private static void registerKeyBinds() {
        KEY_NEXT = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.camera.next_image",
                        InputConstants.KEY_DOWN,
                        KeyMapping.Category.MISC
                )
        );

        KEY_PREVIOUS = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.camera.previous_image",
                        InputConstants.KEY_UP,
                        KeyMapping.Category.MISC
                )
        );
    }

    private static void registerRenderers() {
        EntityRendererRegistry.register(
                CameraMod.IMAGE_ENTITY_TYPE,
                ImageRenderer::new
        );
    }

    private static void registerItemModels() {
        SpecialModelRenderers.ID_MAPPER.put(
                CameraMod.id("image"),
                ImageSpecialRenderer.Unbaked.MAP_CODEC
        );
    }

    private static void registerScreens() {
        MenuScreens.register(
                CameraMod.ALBUM_INVENTORY_CONTAINER,
                AlbumInventoryScreen::new
        );

        MenuScreens.register(
                CameraMod.ALBUM_CONTAINER,
                LecternAlbumScreen::new
        );
    }

    private static void registerConfigNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(
                ConfigSyncPayload.Open.TYPE,
                (payload, context) -> context.client().execute(
                        () -> CameraConfigScreen.open(payload)
                )
        );
    }

    public static void openImageScreen(ItemStack stack) {
        Minecraft.getInstance().gui.setScreen(new ImageScreen(stack));
    }

    public static void openAlbumScreen(List<UUID> images) {
        Minecraft.getInstance().gui.setScreen(new AlbumScreen(images));
    }

    public static void openResizeFrameScreen(UUID id) {
        Minecraft.getInstance().gui.setScreen(new ResizeFrameScreen(id));
    }

    public static void openCameraScreen(String currentShader) {
        Minecraft.getInstance().gui.setScreen(new CameraScreen(currentShader));
    }
}
