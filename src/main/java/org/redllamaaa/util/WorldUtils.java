package org.redllamaaa.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;

public final class WorldUtils {

    private WorldUtils() {
    }

    /**
     * Gets a folder located in the world directory.
     *
     * @param level  the server level
     * @param folder the world-relative folder resource
     * @return a file pointing to the folder in the world directory
     */
    public static File getWorldFolder(ServerLevel level, LevelResource folder) {
        return level.getServer().getWorldPath(folder).toFile();
    }

}
