
package org.redllamaaa.camera;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;

public class CreativeTabEvents {

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> {
                    output.accept(CameraMod.CAMERA);
                    output.accept(CameraMod.ALBUM);
                    output.accept(CameraMod.FRAME_ITEM);
                });
    }
}
