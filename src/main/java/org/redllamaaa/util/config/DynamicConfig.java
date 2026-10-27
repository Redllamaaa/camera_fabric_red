
package org.redllamaaa.util.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class DynamicConfig {

    protected static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    protected JsonObject config;
    private Path configFile;

    public void init(Path configFile, Path defaultConfigFile) {
        this.configFile = configFile;

        boolean createDefaults = !Files.exists(configFile)
                && (defaultConfigFile == null || !Files.exists(defaultConfigFile));

        config = new JsonObject();

        try {
            if (defaultConfigFile != null && Files.exists(defaultConfigFile)) {
                try (Reader reader = Files.newBufferedReader(defaultConfigFile)) {
                    config = JsonParser.parseReader(reader).getAsJsonObject();
                }
            }

            if (Files.exists(configFile)) {
                JsonObject existing;

                try (Reader reader = Files.newBufferedReader(configFile)) {
                    existing = JsonParser.parseReader(reader).getAsJsonObject();
                }

                merge(config, existing);
            }
        } catch (IOException | IllegalStateException e) {
            throw new IllegalStateException("Failed to load config: " + configFile, e);
        }

        if (createDefaults) {
            setDefaults();
        }

        onLoad();
        save();
    }

    public void init(Path configFile) {
        init(configFile, null);
    }

    protected void setDefaults() {
    }

    protected void onLoad() {
    }

    protected void onReload() {
    }

    public <T> T get(String path, T defaultValue) {
        checkLoaded();

        JsonElement element = getElement(path);

        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }

        if (defaultValue == null) {
            @SuppressWarnings("unchecked")
            T value = (T) GSON.fromJson(element, Object.class);
            return value;
        }

        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) defaultValue.getClass();

        return GSON.fromJson(element, type);
    }

    public List<String> getSubValues(String path) {
        checkLoaded();

        JsonElement element = path == null || path.isEmpty()
                ? config
                : getElement(path);

        if (element == null || !element.isJsonObject()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(element.getAsJsonObject().keySet());
    }

    public List<String> getSubValues() {
        return getSubValues(null);
    }

    public <T> T getObject(String path, Supplier<T> object, Supplier<T> defaultValue) {
        checkLoaded();

        JsonElement element = getElement(path);

        if (element == null || !element.isJsonObject()) {
            return defaultValue.get();
        }

        T instance = object.get();

        if (instance == null) {
            return defaultValue.get();
        }

        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) instance.getClass();

        return GSON.fromJson(element, type);
    }

    public <T> T getObject(String path, Supplier<T> object) {
        return getObject(path, object, () -> null);
    }

    public <T> T setObject(String path, T object) {
        checkLoaded();
        setElement(path, GSON.toJsonTree(object));
        save();
        return object;
    }

    public <T> T set(String path, T value) {
        checkLoaded();

        JsonElement previous = getElement(path);
        setElement(path, GSON.toJsonTree(value));
        save();

        if (previous == null || previous.isJsonNull()) {
            return value;
        }

        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) value.getClass();

        return GSON.fromJson(previous, type);
    }

    public boolean isLoaded() {
        return config != null;
    }

    public void reload() {
        checkLoaded();

        try (Reader reader = Files.newBufferedReader(configFile)) {
            config = JsonParser.parseReader(reader).getAsJsonObject();
            onReload();
        } catch (IOException | IllegalStateException e) {
            throw new IllegalStateException("Failed to reload config: " + configFile, e);
        }
    }

    public void save() {
        checkLoaded();

        try {
            Path parent = configFile.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(configFile, GSON.toJson(config));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save config: " + configFile, e);
        }
    }

    private JsonElement getElement(String path) {
        if (path == null || path.isEmpty()) {
            return config;
        }

        String[] parts = path.split("\\.");
        JsonObject current = config;

        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement element = current.get(parts[i]);

            if (element == null || !element.isJsonObject()) {
                return null;
            }

            current = element.getAsJsonObject();
        }

        return current.get(parts[parts.length - 1]);
    }

    private void setElement(String path, JsonElement value) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Config path cannot be empty");
        }

        String[] parts = path.split("\\.");
        JsonObject current = config;

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

    private void checkLoaded() {
        if (!isLoaded()) {
            throw new IllegalStateException("Config not loaded");
        }
    }

    private static void merge(JsonObject target, JsonObject source) {
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            JsonElement existing = target.get(entry.getKey());
            JsonElement incoming = entry.getValue();

            if (existing != null && existing.isJsonObject() && incoming.isJsonObject()) {
                merge(existing.getAsJsonObject(), incoming.getAsJsonObject());
            } else {
                target.add(entry.getKey(), incoming.deepCopy());
            }
        }
    }

    public enum DynamicConfigType {
        SERVER,
        COMMON
    }
}
