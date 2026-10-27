package org.redllamaaa.camera;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.Registry;
import org.redllamaaa.camera.CameraMod;

public class ModSounds {

    // https://www.soundjay.com/mechanical/sounds/camera-shutter-click-01.mp3
    public static final SoundEvent TAKE_IMAGE = Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            Identifier.fromNamespaceAndPath(CameraMod.MODID, "take_image"),
            SoundEvent.createVariableRangeEvent(
                    Identifier.fromNamespaceAndPath(CameraMod.MODID, "take_image")
            )
    );

}