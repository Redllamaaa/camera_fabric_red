package org.redllamaaa.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.UUID;

public class ImageTaker {

    private static UUID pendingImage;
    private static int delay;
    private static boolean hide;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ImageTaker::onClientTick);
    }

    public static boolean isTakingImage() {
        return pendingImage != null;
    }

    public static void takeScreenshot(UUID id) {
        if (id.equals(pendingImage)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();

        hide = mc.gui.hud.isHidden();
        if (!hide) {
            mc.gui.hud.toggle();
        }

        pendingImage = id;
        delay = 2;
        mc.gui.setScreen(null);
    }

    private static void onClientTick(Minecraft mc) {
        if (pendingImage == null) {
            return;
        }
        if (delay > 0) {
            delay--;
            return;
        }

        UUID id = pendingImage;
        pendingImage = null;

        Screenshot.takeScreenshot(mc.gameRenderer.mainRenderTarget(), image -> {
            if (mc.gui.hud.isHidden() != hide) {
                mc.gui.hud.toggle();
            }
            ImageProcessor.sendScreenshotThreaded(id, image);
        });
    }

}
