package org.redllamaaa.camera.items.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;

import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.ImageData;
import org.redllamaaa.camera.entities.ImageEntityRenderState;
import org.redllamaaa.camera.entities.ImageRenderer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import org.joml.Vector3fc;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ImageSpecialRenderer implements SpecialModelRenderer<ImageEntityRenderState.ImageState> {

    @Override
    public void submit(
            @Nullable ImageEntityRenderState.ImageState imageState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            int overlay,
            boolean hasFoil,
            int outlineColor
    ) {
        if (imageState == null) {
            return;
        }

        ImageRenderer.submitImage(
                imageState,
                Direction.SOUTH,
                1,
                1,
                light,
                poseStack,
                collector
        );
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
    }

    @Override
    @Nullable
    public ImageEntityRenderState.ImageState extractArgument(ItemStack stack) {
        if (CameraMod.CLIENT_CONFIG.renderImageItem.get()) {
            ImageData imageData = ImageData.fromStack(stack);

            if (imageData != null) {
                return ImageRenderer.extractImageState(imageData.getId());
            }
        }

        return ImageRenderer.extractImageState(ImageRenderer.DEFAULT_IMAGE_UUID);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<ImageEntityRenderState.ImageState> {

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        @Nullable
        public SpecialModelRenderer<ImageEntityRenderState.ImageState> bake(BakingContext context) {
            return new ImageSpecialRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}