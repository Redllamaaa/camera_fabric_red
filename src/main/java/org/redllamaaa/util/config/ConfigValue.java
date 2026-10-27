
package org.redllamaaa.util.config;

import java.util.Objects;
import java.util.function.Predicate;

public final class ConfigValue<T> {
    private final T defaultValue;
    private final Predicate<T> validator;
    private T value;

    public ConfigValue(T defaultValue) {
        this(defaultValue, Objects::nonNull);
    }

    public ConfigValue(T defaultValue, Predicate<T> validator) {
        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.validator = Objects.requireNonNull(validator);

        if (!isValid(defaultValue)) {
            throw new IllegalArgumentException("Default config value is invalid");
        }

        this.value = defaultValue;
    }

    public synchronized T get() {
        return value;
    }

    public synchronized void set(T value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid config value: " + value);
        }

        this.value = value;
    }

    public T getDefault() {
        return defaultValue;
    }

    public synchronized void reset() {
        value = defaultValue;
    }

    public boolean isValid(T value) {
        return value != null && validator.test(value);
    }
}
