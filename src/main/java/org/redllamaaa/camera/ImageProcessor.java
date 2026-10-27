
package org.redllamaaa.camera;

import com.mojang.blaze3d.platform.NativeImage;

import org.redllamaaa.camera.net.MessagePartialImage;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

public class ImageProcessor {

    private static final int CHUNK_SIZE = 30_000;

    public static void sendScreenshot(UUID uuid, BufferedImage image) {
        byte[] data;

        try {
            data = ImageTools.optimizeImage(image);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int size = data.length;

        if (size <= CHUNK_SIZE) {
            ClientPlayNetworking.send(
                    new MessagePartialImage(uuid, 0, size, data)
            );
            return;
        }

        for (int offset = 0; offset < size; offset += CHUNK_SIZE) {
            int length = Math.min(CHUNK_SIZE, size - offset);
            byte[] chunk = new byte[length];

            System.arraycopy(data, offset, chunk, 0, length);

            ClientPlayNetworking.send(
                    new MessagePartialImage(uuid, offset, size, chunk)
            );
        }
    }

    public static void sendScreenshotThreaded(UUID uuid, BufferedImage image) {
        new Thread(
                () -> sendScreenshot(uuid, image),
                "ProcessImageThread"
        ).start();
    }

    public static void sendScreenshotThreaded(UUID uuid, NativeImage image) {
        new Thread(
                () -> sendScreenshot(uuid, ImageTools.fromNativeImage(image)),
                "ProcessImageThread"
        ).start();
    }
}
