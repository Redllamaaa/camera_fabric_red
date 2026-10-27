
package org.redllamaaa.camera;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;

public class CreativeTabEvents {

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> {
                    entries.accept(CameraMod.CAMERA);
                    entries.accept(CameraMod.ALBUM);
                    entries.accept(CameraMod.FRAME_ITEM);
                });
    }
}
