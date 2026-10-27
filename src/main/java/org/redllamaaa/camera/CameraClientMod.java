
package org.redllamaaa.camera;

import org.redllamaaa.camera.entities.ImageRenderer;
import org.redllamaaa.camera.gui.*;
import org.redllamaaa.camera.items.render.ImageSpecialRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class CameraClientMod implements ClientModInitializer {

    public static KeyMapping KEY_NEXT;
    public static KeyMapping KEY_PREVIOUS;

    @Override
    public void onInitializeClient() {
        registerKeyBinds();
        registerRenderers();
        registerItemModels();
        registerScreens();

        ClientEvents.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KEY_NEXT.consumeClick()) {
                ClientEvents.onNextImage(client);
            }

            while (KEY_PREVIOUS.consumeClick()) {
                ClientEvents.onPreviousImage(client);
            }
        });
    }

    private static void registerKeyBinds() {
        KEY_NEXT = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.camera.next_image",
                        KeyMapping.Category.MISC,
                        net.minecraft.client.input.KeyEvent.KEY_DOWN
                )
        );

        KEY_PREVIOUS = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.camera.previous_image",
                        KeyMapping.Category.MISC,
                        net.minecraft.client.input.KeyEvent.KEY_UP
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
                Identifier.fromNamespaceAndPath(CameraMod.MODID, "image"),
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

    public static void openImageScreen(ItemStack stack) {
        Minecraft.getInstance().setScreen(new ImageScreen(stack));
    }

    public static void openAlbumScreen(List<UUID> images) {
        Minecraft.getInstance().setScreen(new AlbumScreen(images));
    }

    public static void openResizeFrameScreen(UUID id) {
        Minecraft.getInstance().setScreen(new ResizeFrameScreen(id));
    }

    public static void openCameraScreen(String currentShader) {
        Minecraft.getInstance().setScreen(new CameraScreen(currentShader));
    }
}
