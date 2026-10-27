
package org.redllamaaa.camera.net;

import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.ImageData;
import org.redllamaaa.camera.ImageTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import org.redllamaaa.camera.items.ImageItem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

public class PacketManager {
    private static final int MAX_CACHED_IMAGES = 256;
    private static final int MAX_ACTIVE_UPLOADS = 128;
    private static final int MAX_UPLOADS_PER_PLAYER = 4;
    private static final long UPLOAD_TIMEOUT_MS = 120_000L;

    private final Map<UUID, UploadBuffer> clientDataMap = new HashMap<>();
    private final Map<UUID, UUID> uploadOwners = new HashMap<>();
    private final Map<UUID, Long> uploadStarted = new HashMap<>();
    private final Map<UUID, PendingUpload> pendingUploads = new HashMap<>();

    private final Map<UUID, BufferedImage> imageCache =
            new LinkedHashMap<>(32, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<UUID, BufferedImage> eldest) {
                    return size() > MAX_CACHED_IMAGES;
                }
            };

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public synchronized boolean authorizeUpload(ServerPlayer player, UUID imageId) {
        if (player == null || imageId == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        cleanupExpiredUploads(now);

        if (clientDataMap.containsKey(imageId)
                || pendingUploads.containsKey(imageId)
                || pendingUploads.size() + clientDataMap.size() >= MAX_ACTIVE_UPLOADS) {
            return false;
        }

        UUID playerId = player.getUUID();
        if (countUploads(playerId) + countPendingUploads(playerId) >= MAX_UPLOADS_PER_PLAYER) {
            return false;
        }

        pendingUploads.put(imageId, new PendingUpload(playerId, now));
        return true;
    }

    public synchronized void cancelAuthorizedUpload(ServerPlayer player, UUID imageId) {
        if (player == null || imageId == null) {
            return;
        }

        PendingUpload pending = pendingUploads.get(imageId);
        if (pending != null && pending.owner().equals(player.getUUID())) {
            pendingUploads.remove(imageId);
        }
    }

    public synchronized void addBytes(
            ServerPlayer player,
            UUID imageId,
            int offset,
            int length,
            byte[] bytes
    ) {
        if (player == null || imageId == null || bytes == null || bytes.length == 0) {
            return;
        }

        int maxSize = CameraMod.SERVER_CONFIG.maxImageSize.get();
        UUID playerId = player.getUUID();

        UUID activeOwner = uploadOwners.get(imageId);
        PendingUpload pendingUpload = pendingUploads.get(imageId);
        if ((activeOwner != null && !activeOwner.equals(playerId))
                || (activeOwner == null && pendingUpload != null
                && !pendingUpload.owner().equals(playerId))) {
            CameraMod.LOGGER.warn("Rejected image upload from a non-owner: {}",
                    player.getGameProfile().name());
            return;
        }

        if (length <= 0 || length > maxSize || offset < 0
                || offset > length || bytes.length > length - offset) {
            clearUpload(imageId);
            CameraMod.LOGGER.warn("Rejected invalid image upload from {}", player.getGameProfile().name());
            return;
        }

        UploadBuffer upload = clientDataMap.get(imageId);

        if (upload == null) {
            PendingUpload pending = pendingUploads.get(imageId);
            if (pending == null) {
                CameraMod.LOGGER.warn("Rejected unauthorized image upload from {}",
                        player.getGameProfile().name());
                return;
            }
            if (System.currentTimeMillis() - pending.startedAt() > UPLOAD_TIMEOUT_MS) {
                pendingUploads.remove(imageId);
                CameraMod.LOGGER.warn("Rejected expired image upload from {}",
                        player.getGameProfile().name());
                return;
            }

            if (clientDataMap.size() >= MAX_ACTIVE_UPLOADS
                    || countUploads(playerId) >= MAX_UPLOADS_PER_PLAYER) {
                CameraMod.LOGGER.warn("Rejected image upload: too many active uploads for {}",
                        player.getGameProfile().name());
                return;
            }

            pendingUploads.remove(imageId);
            upload = new UploadBuffer(length);
            clientDataMap.put(imageId, upload);
            uploadOwners.put(imageId, playerId);
            uploadStarted.put(imageId, System.currentTimeMillis());
        } else if (upload.data.length != length) {
            clearUpload(imageId);
            CameraMod.LOGGER.warn("Rejected conflicting image upload from {}",
                    player.getGameProfile().name());
            return;
        }

        for (int i = 0; i < bytes.length; i++) {
            int index = offset + i;

            if (!upload.received.get(index)) {
                upload.received.set(index);
                upload.receivedCount++;
            }

            upload.data[index] = bytes[i];
        }

        if (upload.receivedCount != upload.data.length) {
            return;
        }

        BufferedImage image = completeImage(imageId);

        if (image == null) {
            CameraMod.LOGGER.warn("Failed to decode uploaded image from {}",
                    player.getGameProfile().name());
            return;
        }

        imageCache.put(imageId, image);
        saveImageAndGiveItem(player, imageId, image);
    }

    public BufferedImage getExistingImage(ServerPlayer player, UUID uuid) throws IOException {
        synchronized (this) {
            BufferedImage cached = imageCache.get(uuid);

            if (cached != null) {
                return cached;
            }
        }

        BufferedImage image = ImageTools.loadImage(player, uuid);

        synchronized (this) {
            imageCache.put(uuid, image);
        }

        return image;
    }

    public synchronized BufferedImage completeImage(UUID imageId) {
        UploadBuffer upload = clientDataMap.get(imageId);

        if (upload == null || upload.receivedCount != upload.data.length) {
            return null;
        }

        clientDataMap.remove(imageId);
        uploadOwners.remove(imageId);
        uploadStarted.remove(imageId);

        try {
            BufferedImage image = ImageTools.fromBytes(upload.data);

            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                return null;
            }

            return image;
        } catch (IOException | RuntimeException e) {
            CameraMod.LOGGER.warn("Failed to decode image {}", imageId, e);
            return null;
        }
    }

    public synchronized boolean isImageOnCooldown(UUID playerId) {
        long now = System.nanoTime();
        cleanupExpiredUploads(System.currentTimeMillis());
        long cooldown = TimeUnit.MILLISECONDS.toNanos(
                CameraMod.SERVER_CONFIG.imageCooldown.get()
        );

        Long lastTaken = cooldowns.get(playerId);

        if (lastTaken == null || now - lastTaken >= cooldown) {
            return false;
        }

        return true;
    }

    public synchronized void recordImageTaken(UUID playerId) {
        long now = System.nanoTime();
        long cooldown = TimeUnit.MILLISECONDS.toNanos(
                CameraMod.SERVER_CONFIG.imageCooldown.get()
        );
        cooldowns.put(playerId, now);

        if (cooldowns.size() > 1024) {
            cooldowns.entrySet().removeIf(entry -> now - entry.getValue() > cooldown);
        }
        cleanupExpiredUploads(System.currentTimeMillis());
    }

    public synchronized void removePlayer(UUID playerId) {
        cooldowns.remove(playerId);

        Iterator<Map.Entry<UUID, UUID>> iterator = uploadOwners.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, UUID> entry = iterator.next();

            if (entry.getValue().equals(playerId)) {
                clientDataMap.remove(entry.getKey());
                uploadStarted.remove(entry.getKey());
                iterator.remove();
            }
        }

        pendingUploads.entrySet().removeIf(entry -> entry.getValue().owner().equals(playerId));
    }

    private void saveImageAndGiveItem(ServerPlayer player, UUID imageId, BufferedImage image) {
        MinecraftServer server = player.level().getServer();
        File imageFile = ImageTools.getImageFile(player, imageId);

        if (server == null) {
            return;
        }

        Thread.startVirtualThread(() -> {
            try {
                File parent = imageFile.getParentFile();

                if (parent != null) {
                    Files.createDirectories(parent.toPath());
                }

                if (!ImageIO.write(image, "jpg", imageFile)) {
                    throw new IOException("No JPEG writer is available");
                }

                server.execute(() -> {
                    if (server.getPlayerList().getPlayer(player.getUUID()) != player) {
                        return;
                    }

                    ItemStack stack = new ItemStack(CameraMod.IMAGE);
                    ImageData imageData = ImageData.create(player, imageId);
                    imageData.addToImage(stack);

                    if (!player.addItem(stack)) {
                        Containers.dropItemStack(
                                player.level(),
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                stack
                        );
                    }
                });
            } catch (IOException | RuntimeException e) {
                CameraMod.LOGGER.error("Failed to save uploaded image {}", imageId, e);
            }
        });
    }

    private int countUploads(UUID playerId) {
        int count = 0;

        for (UUID owner : uploadOwners.values()) {
            if (owner.equals(playerId)) {
                count++;
            }
        }

        return count;
    }

    private int countPendingUploads(UUID playerId) {
        int count = 0;

        for (PendingUpload pending : pendingUploads.values()) {
            if (pending.owner().equals(playerId)) {
                count++;
            }
        }

        return count;
    }

    private void clearUpload(UUID imageId) {
        clientDataMap.remove(imageId);
        uploadOwners.remove(imageId);
        uploadStarted.remove(imageId);
        pendingUploads.remove(imageId);
    }

    private void cleanupExpiredUploads(long now) {
        pendingUploads.entrySet().removeIf(entry -> now - entry.getValue().startedAt() > UPLOAD_TIMEOUT_MS);

        Iterator<Map.Entry<UUID, Long>> iterator = uploadStarted.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();

            if (now - entry.getValue() > UPLOAD_TIMEOUT_MS) {
                UUID imageId = entry.getKey();
                clientDataMap.remove(imageId);
                uploadOwners.remove(imageId);
                iterator.remove();
            }
        }
    }

    private static final class UploadBuffer {
        private final byte[] data;
        private final BitSet received;
        private int receivedCount;

        private UploadBuffer(int length) {
            this.data = new byte[length];
            this.received = new BitSet(length);
        }
    }

    private record PendingUpload(UUID owner, long startedAt) {
    }
}
