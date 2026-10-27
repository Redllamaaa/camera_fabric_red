package org.redllamaaa.camera;

import net.minecraft.resources.Identifier;

import java.util.*;

public class Shaders {

    public static Identifier BLACK_AND_WHITE = CameraMod.id("black_and_white");
    public static Identifier SEPIA = CameraMod.id("sepia");
    public static Identifier DESATURATED = CameraMod.id("desaturated");
    public static Identifier OVEREXPOSED = CameraMod.id("overexposed");
    public static Identifier OVERSATURATED = CameraMod.id("oversaturated");
    public static Identifier BLURRY = CameraMod.id("blurry");
    public static Identifier INVERTED = CameraMod.id("inverted");

    private static Map<String, Identifier> shaders;
    public static final List<String> SHADER_LIST;

    static {
        shaders = new HashMap<>();
        shaders.put("none", null);
        shaders.put("black_and_white", BLACK_AND_WHITE);
        shaders.put("sepia", SEPIA);
        shaders.put("desaturated", DESATURATED);
        shaders.put("overexposed", OVEREXPOSED);
        shaders.put("oversaturated", OVERSATURATED);
        shaders.put("blurry", BLURRY);
        shaders.put("inverted", INVERTED);
        SHADER_LIST = Collections.unmodifiableList(new ArrayList<>(shaders.keySet()));
    }

    public static Identifier getShader(String name) {
        return shaders.get(name);
    }

}
