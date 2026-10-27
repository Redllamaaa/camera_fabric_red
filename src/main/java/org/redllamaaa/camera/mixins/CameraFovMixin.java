package org.redllamaaa.camera.mixins;

import net.minecraft.client.Camera;
import org.redllamaaa.camera.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraFovMixin {

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void camera_fabric$applyZoom(float partialTick, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(ClientEvents.applyCameraZoom(cir.getReturnValue()));
    }
}
