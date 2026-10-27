
package org.redllamaaa.camera;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.redllamaaa.util.config.ConfigBase;
import org.redllamaaa.util.config.ConfigValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ServerConfig extends ConfigBase {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final Path configPath = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("camera_fabric_red.json");

    public final ConfigValue<Integer> imageCooldown =
            new ConfigValue<>(5000, value -> value >= 100);

    public final ConfigValue<Integer> cameraConsumeItemAmount =
            new ConfigValue<>(1, value -> value >= 1 && value <= Short.MAX_VALUE);

    public final ConfigValue<Integer> maxImageSize =
            new ConfigValue<>(200_000, value -> value >= 50_000 && value <= 1_000_000);

    public final ConfigValue<Double> imageCompression =
            new ConfigValue<>(0.5D, value ->
                    Double.isFinite(value) && value >= 0.1D && value <= 1.0D);

    public final ConfigValue<Boolean> allowImageUpload =
            new ConfigValue<>(true);

    public final ConfigValue<Boolean> frameOnlyOwnerModify =
            new ConfigValue<>(false);

    public final ConfigValue<Boolean> advancedImageData =
            new ConfigValue<>(false);

    public final ConfigValue<Integer> advancedDataMaxEntities =
            new ConfigValue<>(16, value -> value >= 1 && value <= 128);

    public synchronized void load() {
        if (!Files.exists(configPath)) {
            save();
            onLoad();
            return;
        }

        try (var reader = Files.newBufferedReader(configPath)) {
            JsonElement root = JsonParser.parseReader(reader);

            if (!root.isJsonObject()) {
                throw new IllegalStateException("Config root must be a JSON object");
            }

            JsonObject json = root.getAsJsonObject();

            read(json, "camera.cooldown", imageCooldown);
            read(json, "camera.consumed_item.amount", cameraConsumeItemAmount);
            read(json, "image.max_size", maxImageSize);
            read(json, "image.compression", imageCompression);
            read(json, "image.allow_upload", allowImageUpload);
            read(json, "image_frame.only_owner_modify", frameOnlyOwnerModify);
            read(json, "advanced_data.enable", advancedImageData);
            read(json, "advanced_data.max_entities", advancedDataMaxEntities);

            save();
            onLoad();
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException(
                    "Failed to load config from " + configPath, e);
        }
    }

    public synchronized void save() {
        JsonObject json = new JsonObject();

        write(json, "camera.cooldown", imageCooldown);
        write(json, "camera.consumed_item.amount", cameraConsumeItemAmount);
        write(json, "image.max_size", maxImageSize);
        write(json, "image.compression", imageCompression);
        write(json, "image.allow_upload", allowImageUpload);
        write(json, "image_frame.only_owner_modify", frameOnlyOwnerModify);
        write(json, "advanced_data.enable", advancedImageData);
        write(json, "advanced_data.max_entities", advancedDataMaxEntities);

        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(json));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to save config to " + configPath, e);
        }
    }

    private <T> void read(JsonObject json, String path, ConfigValue<T> value) {
        JsonElement element = getElement(json, path);

        if (element == null || element.isJsonNull()) {
            value.reset();
            return;
        }

        try {
            T parsed = GSON.fromJson(element, value.getDefault().getClass());

            if (value.isValid(parsed)) {
                value.set(parsed);
            } else {
                value.reset();
            }
        } catch (RuntimeException e) {
            value.reset();
        }
    }

    private <T> void write(JsonObject json, String path, ConfigValue<T> value) {
        setElement(json, path, GSON.toJsonTree(value.get()));
    }

    private JsonElement getElement(JsonObject json, String path) {
        String[] parts = path.split("\\.");
        JsonObject current = json;

        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement element = current.get(parts[i]);

            if (element == null || !element.isJsonObject()) {
                return null;
            }

            current = element.getAsJsonObject();
        }

        return current.get(parts[parts.length - 1]);
    }

    private void setElement(JsonObject json, String path, JsonElement value) {
        String[] parts = path.split("\\.");
        JsonObject current = json;

        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement element = current.get(parts[i]);

            if (element == null || !element.isJsonObject()) {
                JsonObject nested = new JsonObject();
                current.add(parts[i], nested);
                current = nested;
            } else {
                current = element.getAsJsonObject();
            }
        }

        current.add(parts[parts.length - 1], value);
    }
}
