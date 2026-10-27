
package org.redllamaaa.camera;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientHotbarScrollEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.redllamaaa.camera.items.CameraItem;
import org.redllamaaa.camera.net.MessageDisableCameraMode;

public class ClientEvents {

    private static final Identifier VIEWFINDER =
            CameraMod.id("textures/gui/viewfinder_overlay.png");
    private static final Identifier ZOOM =
            CameraMod.id("textures/gui/zoom.png");
    private static final Identifier HUD_ID =
            CameraMod.id("camera_overlay");

    public static final float MAX_FOV = 90F;
    public static final float MIN_FOV = 5F;

    private static final Minecraft mc = Minecraft.getInstance();

    private static boolean inCameraMode;
    private static float fov = MAX_FOV;

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            ItemStack activeCamera = getActiveCamera();
            if (client.player != null && client.gui.screen() == null && !activeCamera.isEmpty()
                    && client.options.keyAttack.consumeClick()) {
                CameraMod.CAMERA.setActive(activeCamera, false);
                ClientPlayNetworking.send(new MessageDisableCameraMode());
            }
        });

        ClientHotbarScrollEvents.ALLOW.register((inventory, currentSlot, newSlot, xOffset, yOffset) -> {
            if (yOffset == 0D || getActiveCamera().isEmpty()) {
                return true;
            }

            adjustZoom(yOffset);
            return false;
        });

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                HUD_ID,
                ClientEvents::renderOverlay
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) {
                inCameraMode = false;
                return;
            }

            inCameraMode = isInCameraMode();

            if (client.player != null && inCameraMode) {
                client.options.setCameraType(CameraType.FIRST_PERSON);
            }

            for (Player player : client.level.players()) {
                if (player == client.player) {
                    continue;
                }

                for (InteractionHand hand : InteractionHand.values()) {
                    ItemStack stack = player.getItemInHand(hand);

                    if (stack.getItem() instanceof CameraItem) {
                        if (CameraMod.CAMERA.isActive(stack)) {
                            player.startUsingItem(hand);
                        } else {
                            player.stopUsingItem();
                        }
                    }
                }
            }
        });
    }

    private static void renderOverlay(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        inCameraMode = isInCameraMode();

        if (!inCameraMode || mc.player == null || mc.gui.hud.isHidden()) {
            return;
        }

        drawViewFinder(graphics);
        drawZoom(graphics, getFOVPercentage());
    }

    private static void drawViewFinder(GuiGraphicsExtractor graphics) {
        int imageWidth = 192;
        int imageHeight = 100;

        float width = mc.getWindow().getGuiScaledWidth();
        float height = mc.getWindow().getGuiScaledHeight();

        float screenRatio = width / height;
        float imageRatio = (float) imageWidth / imageHeight;

        float newWidth;
        float newHeight;

        if (screenRatio > imageRatio) {
            newWidth = (float) imageWidth * height / imageHeight;
            newHeight = height;
        } else {
            newWidth = width;
            newHeight = (float) imageHeight * width / imageWidth;
        }

        int left = (int) ((width - newWidth) / 2F);
        int top = (int) ((height - newHeight) / 2F);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                VIEWFINDER,
                left,
                top,
                0F,
                0F,
                (int) newWidth,
                (int) newHeight,
                192,
                100,
                256,
                256
        );
    }

    private static void drawZoom(GuiGraphicsExtractor graphics, float percent) {
        int zoomWidth = 112;
        int zoomHeight = 10;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        int left = (width - zoomWidth) / 2;
        int top = height / 40;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                ZOOM,
                left,
                top,
                0F,
                0F,
                zoomWidth,
                zoomHeight,
                zoomWidth,
                zoomHeight,
                128,
                128
        );

        int percentWidth = (int) (Math.clamp(percent, 0F, 1F) * zoomWidth);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                ZOOM,
                left,
                top,
                0F,
                zoomHeight,
                percentWidth,
                zoomHeight,
                percentWidth,
                zoomHeight,
                128,
                128
        );
    }

    public static void adjustZoom(double scrollDelta) {
        if (scrollDelta == 0D || getActiveCamera().isEmpty()) {
            return;
        }

        if (scrollDelta < 0D) {
            fov = Math.min(fov + 5F, MAX_FOV);
        } else {
            fov = Math.max(fov - 5F, MIN_FOV);
        }
    }

    public static float getFOV() {
        return fov;
    }

    public static float applyCameraZoom(float vanillaFov) {
        if (getActiveCamera().isEmpty() && !ImageTaker.isTakingImage()) {
            return vanillaFov;
        }
        return vanillaFov * fov / MAX_FOV;
    }

    public static void setFOV(float value) {
        fov = value;
    }

    public static float getFOVPercentage() {
        return 1F - (fov - MIN_FOV) / (MAX_FOV - MIN_FOV);
    }

    public static ItemStack getActiveCamera() {
        if (mc.player == null) {
            return ItemStack.EMPTY;
        }

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = mc.player.getItemInHand(hand);

            if (stack.is(CameraMod.CAMERA) && CameraMod.CAMERA.isActive(stack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean isInCameraMode() {
        return !getActiveCamera().isEmpty() || ImageTaker.isTakingImage();
    }
}
