
package org.redllamaaa.camera.entities;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.EntityHitResult;
import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.TextureCache;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public class ImageRenderer extends EntityRenderer<ImageEntity, ImageEntityRenderState> {

    private static final Identifier DEFAULT_IMAGE = CameraMod.id("textures/images/default_image.png");
    private static final Identifier EMPTY_IMAGE = CameraMod.id("textures/images/empty_image.png");
    private static final Identifier FRAME_SIDE = CameraMod.id("textures/images/frame_side.png");
    private static final Identifier FRAME_BACK = CameraMod.id("textures/images/frame_back.png");

    private static final float THICKNESS = 1F / 16F;

    public static final UUID DEFAULT_IMAGE_UUID = new UUID(0L, 0L);

    private final Minecraft mc;

    public ImageRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.mc = Minecraft.getInstance();
    }

    @Override
    public ImageEntityRenderState createRenderState() {
        return new ImageEntityRenderState();
    }

    @Override
    public void submit(
            ImageEntityRenderState state,
            PoseStack stack,
            SubmitNodeCollector collector,
            CameraRenderState cameraRenderState
    ) {
        super.submit(state, stack, collector, cameraRenderState);

        stack.pushPose();
        stack.translate(-0.5D, 0D, -0.5D);

        submitImage(
                state.imageState,
                state.facing,
                state.frameWidth,
                state.frameHeight,
                state.light,
                stack,
                collector
        );

        stack.popPose();

        submitBoundingBox(state);
    }

    public static void submitImage(
            ImageEntityRenderState.ImageState imageState,
            Direction facing,
            float width,
            float height,
            int light,
            PoseStack stack,
            SubmitNodeCollector collector
    ) {
        stack.pushPose();

        Identifier resourceLocation = imageState.resourceLocation();
        float imageRatio = imageState.imageRatio();
        boolean stretch = DEFAULT_IMAGE.equals(resourceLocation);

        rotate(facing, stack);

        float frameRatio = width / height;
        float ratio = imageRatio / frameRatio;

        float ratioX;
        float ratioY;

        if (stretch) {
            ratioX = 0F;
            ratioY = 0F;
        } else if (ratio >= 1F) {
            ratioY = ((1F - 1F / ratio) / 2F) * height;
            ratioX = 0F;
        } else {
            ratioX = ((1F - ratio) / 2F) * width;
            ratioY = 0F;
        }

        collector.submitCustomGeometry(
                stack,
                RenderTypes.text(resourceLocation),
                (pose, consumer) -> {
                    vertex(consumer, pose, ratioX, ratioY, THICKNESS, 0F, 1F, light, Direction.SOUTH);
                    vertex(consumer, pose, width - ratioX, ratioY, THICKNESS, 1F, 1F, light, Direction.SOUTH);
                    vertex(consumer, pose, width - ratioX, height - ratioY, THICKNESS, 1F, 0F, light, Direction.SOUTH);
                    vertex(consumer, pose, ratioX, height - ratioY, THICKNESS, 0F, 0F, light, Direction.SOUTH);
                }
        );

        collector.submitCustomGeometry(
                stack,
                RenderTypes.entityCutout(FRAME_SIDE),
                (pose, consumer) -> {
                    vertex(consumer, pose, ratioX, ratioY, 0F, 1F, ratioY, light, Direction.WEST);
                    vertex(consumer, pose, ratioX, ratioY, THICKNESS, 1F - THICKNESS, ratioY, light, Direction.WEST);
                    vertex(consumer, pose, ratioX, height - ratioY, THICKNESS, 1F - THICKNESS, 1F - ratioY, light, Direction.WEST);
                    vertex(consumer, pose, ratioX, height - ratioY, 0F, 1F, 1F - ratioY, light, Direction.WEST);

                    vertex(consumer, pose, width - ratioX, ratioY, 0F, 0F, ratioY, light, Direction.EAST);
                    vertex(consumer, pose, width - ratioX, height - ratioY, 0F, 0F, 1F - ratioY, light, Direction.EAST);
                    vertex(consumer, pose, width - ratioX, height - ratioY, THICKNESS, THICKNESS, 1F - ratioY, light, Direction.EAST);
                    vertex(consumer, pose, width - ratioX, ratioY, THICKNESS, THICKNESS, ratioY, light, Direction.EAST);

                    vertex(consumer, pose, ratioX, height - ratioY, 0F, ratioX, 1F, light, Direction.UP);
                    vertex(consumer, pose, ratioX, height - ratioY, THICKNESS, ratioX, 1F - THICKNESS, light, Direction.UP);
                    vertex(consumer, pose, width - ratioX, height - ratioY, THICKNESS, 1F - ratioX, 1F - THICKNESS, light, Direction.UP);
                    vertex(consumer, pose, width - ratioX, height - ratioY, 0F, 1F - ratioX, 1F, light, Direction.UP);

                    vertex(consumer, pose, ratioX, ratioY, 0F, ratioX, 0F, light, Direction.DOWN);
                    vertex(consumer, pose, width - ratioX, ratioY, 0F, 1F - ratioX, 0F, light, Direction.DOWN);
                    vertex(consumer, pose, width - ratioX, ratioY, THICKNESS, 1F - ratioX, THICKNESS, light, Direction.DOWN);
                    vertex(consumer, pose, ratioX, ratioY, THICKNESS, ratioX, THICKNESS, light, Direction.DOWN);
                }
        );

        collector.submitCustomGeometry(
                stack,
                RenderTypes.entityCutout(FRAME_BACK),
                (pose, consumer) -> {
                    vertex(consumer, pose, width - ratioX, ratioY, 0F, 1F - ratioX, ratioY, light, Direction.NORTH);
                    vertex(consumer, pose, ratioX, ratioY, 0F, ratioX, ratioY, light, Direction.NORTH);
                    vertex(consumer, pose, ratioX, height - ratioY, 0F, ratioX, 1F - ratioY, light, Direction.NORTH);
                    vertex(consumer, pose, width - ratioX, height - ratioY, 0F, 1F - ratioX, 1F - ratioY, light, Direction.NORTH);
                }
        );

        stack.popPose();
    }

    @Override
    public void extractRenderState(
            ImageEntity image,
            ImageEntityRenderState state,
            float partialTicks
    ) {
        super.extractRenderState(image, state, partialTicks);

        state.imageEntityUUID = image.getUUID();
        state.frameWidth = image.getFrameWidth();
        state.frameHeight = image.getFrameHeight();
        state.facing = image.getFacing();
        state.imageState = extractImageState(image.getImageUUID().orElse(null));
        state.light = LightCoordsUtil.getLightCoords(image.level(), image.getCenterPosition());
        state.imageBoundingBox = image.getBoundingBox();
    }

    public static ImageEntityRenderState.ImageState extractImageState(@Nullable UUID imageId) {
        Identifier resourceLocation;
        float imageRatio;

        if (imageId == null) {
            resourceLocation = EMPTY_IMAGE;
            imageRatio = 1.5F;
        } else if (DEFAULT_IMAGE_UUID.equals(imageId)) {
            resourceLocation = DEFAULT_IMAGE;
            imageRatio = 1.5F;
        } else {
            Identifier identifier = TextureCache.instance().getImage(imageId);

            if (identifier != null) {
                resourceLocation = identifier;

                NativeImage nativeImage = TextureCache.instance().getNativeImage(imageId);
                imageRatio = (float) nativeImage.getWidth() / nativeImage.getHeight();
            } else {
                resourceLocation = DEFAULT_IMAGE;
                imageRatio = 1.5F;
            }
        }

        return new ImageEntityRenderState.ImageState(imageId, imageRatio, resourceLocation);
    }

    private static void vertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int light,
            Direction normal
    ) {
        consumer.addVertex(pose.pose(), x, y, z)
                .setColor(1F, 1F, 1F, 1F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, normal.getStepX(), normal.getStepY(), normal.getStepZ());
    }

    private void submitBoundingBox(ImageEntityRenderState state) {
        if (!(mc.hitResult instanceof EntityHitResult entityHitResult)
                || !entityHitResult.getEntity().getUUID().equals(state.imageEntityUUID)) {
            return;
        }

        if (mc.gui.hud.isHidden()) {
            return;
        }

        Gizmos.cuboid(
                state.imageBoundingBox,
                GizmoStyle.stroke(ARGB.colorFromFloat(0.4F, 0F, 0F, 0F)),
                false
        );
    }

    public static void rotate(Direction facing, PoseStack poseStack) {
        switch (facing) {
            case NORTH -> {
                poseStack.translate(1D, 0D, 1D);
                poseStack.rotateDegrees(Axis.YP, 180F);
            }
            case SOUTH -> {
            }
            case EAST -> {
                poseStack.translate(0D, 0D, 1D);
                poseStack.rotateDegrees(Axis.YP, 90F);
            }
            case WEST -> {
                poseStack.translate(1D, 0D, 0D);
                poseStack.rotateDegrees(Axis.YP, 270F);
            }
            default -> {
            }
        }
    }
}
