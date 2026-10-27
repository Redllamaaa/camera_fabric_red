package org.redllamaaa.camera;

import net.fabricmc.loader.api.FabricLoader;
import org.redllamaaa.util.config.ConfigValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Properties;

public final class ClientConfig {
    private final Path configPath = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("camera_fabric-client.properties");

    public final ConfigValue<String> lastImagePath = new ConfigValue<>("");
    public final ConfigValue<Float> resizeGuiOpacity = new ConfigValue<>(0.75F,
            value -> Float.isFinite(value) && value >= 0F && value <= 1F);
    public final ConfigValue<Boolean> renderImageItem = new ConfigValue<>(true);
    public final SimpleDateFormat imageDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public synchronized void load() {
        if (!Files.exists(configPath)) {
            save();
            return;
        }

        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(configPath)) {
            properties.load(reader);
            lastImagePath.set(properties.getProperty("lastImagePath", ""));
            resizeGuiOpacity.set(Float.parseFloat(properties.getProperty("resizeGuiOpacity", "0.75")));
            renderImageItem.set(Boolean.parseBoolean(properties.getProperty("renderImageItem", "true")));
        } catch (IOException | RuntimeException e) {
            CameraMod.LOGGER.warn("Failed to load client config from {}", configPath, e);
        }
    }

    public synchronized void save() {
        Properties properties = new Properties();
        properties.setProperty("lastImagePath", lastImagePath.get());
        properties.setProperty("resizeGuiOpacity", Float.toString(resizeGuiOpacity.get()));
        properties.setProperty("renderImageItem", Boolean.toString(renderImageItem.get()));

        try {
            Files.createDirectories(configPath.getParent());
            try (var writer = Files.newBufferedWriter(configPath)) {
                properties.store(writer, "Camera Mod client settings");
            }
        } catch (IOException e) {
            CameraMod.LOGGER.warn("Failed to save client config to {}", configPath, e);
        }
    }
}
