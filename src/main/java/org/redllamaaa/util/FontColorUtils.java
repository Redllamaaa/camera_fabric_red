package org.redllamaaa.util;

import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;

/**
 * Font colour constants and conversion. Text drawing in current Minecraft needs a fully opaque ARGB value.
 */
public final class FontColorUtils {

    public static final int BLACK = getFontColor(TextColor.BLACK);
    public static final int WHITE = getFontColor(TextColor.WHITE);

    private FontColorUtils() {
    }

    public static int getFontColor(TextColor color) {
        return rgbToArgb(color.getValue());
    }

    public static int rgbToArgb(int rgb) {
        return ARGB.color(255, rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
    }

}
