
package org.redllamaaa.util.config;

import java.util.function.Predicate;

public class ConfigValue<T> {

    private final T defaultValue;
    private final Predicate<T> validator;
    private T value;

    public ConfigValue(T defaultValue) {
        this(defaultValue, ignored -> true);
    }

    public ConfigValue(T defaultValue, Predicate<T> validator) {
        this.defaultValue = defaultValue;
        this.validator = validator;
        this.value = defaultValue;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        if (!validator.test(value)) {
            throw new IllegalArgumentException("Invalid configuration value: " + value);
        }

        this.value = value;
    }

    public T getDefault() {
        return defaultValue;
    }

    public void reset() {
        value = defaultValue;
    }

    public boolean isValid(T value) {
        return validator.test(value);
    }
}
