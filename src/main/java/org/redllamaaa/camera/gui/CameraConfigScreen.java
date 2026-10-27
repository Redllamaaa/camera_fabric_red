package org.redllamaaa.camera.gui;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.redllamaaa.camera.net.ConfigSyncPayload;

public final class CameraConfigScreen {
    private static final Draft DRAFT = new Draft();

    private CameraConfigScreen() {
    }

    public static void open(ConfigSyncPayload.Open config) {
        DRAFT.imageCooldown = config.imageCooldown();
        DRAFT.cameraConsumeItemAmount = config.cameraConsumeItemAmount();
        DRAFT.maxImageSize = config.maxImageSize();
        DRAFT.imageCompression = config.imageCompression();
        DRAFT.allowImageUpload = config.allowImageUpload();
        DRAFT.frameOnlyOwnerModify = config.frameOnlyOwnerModify();
        DRAFT.advancedImageData = config.advancedImageData();
        DRAFT.advancedDataMaxEntities = config.advancedDataMaxEntities();

        Minecraft minecraft = Minecraft.getInstance();
        Screen parent = minecraft.gui.screen();

        minecraft.gui.setScreen(createScreen(parent));
    }

    private static Screen createScreen(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Camera Mod Server Settings"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Camera"))
                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("Image cooldown"))
                                .description(OptionDescription.of(
                                        Component.literal("Cooldown between taking images, in milliseconds.")
                                ))
                                .binding(5000, () -> DRAFT.imageCooldown,
                                        value -> DRAFT.imageCooldown = value)
                                .controller(option -> IntegerFieldControllerBuilder.create(option))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("Consumed item amount"))
                                .description(OptionDescription.of(
                                        Component.literal("Items consumed when taking an image.")
                                ))
                                .binding(1, () -> DRAFT.cameraConsumeItemAmount,
                                        value -> DRAFT.cameraConsumeItemAmount = value)
                                .controller(option -> IntegerFieldControllerBuilder.create(option))
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Images"))
                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("Maximum image size"))
                                .description(OptionDescription.of(
                                        Component.literal("Maximum image transfer size in bytes.")
                                ))
                                .binding(200_000, () -> DRAFT.maxImageSize,
                                        value -> DRAFT.maxImageSize = value)
                                .controller(option -> IntegerFieldControllerBuilder.create(option))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.literal("Image compression"))
                                .description(OptionDescription.of(
                                        Component.literal("JPEG compression setting from 0.1 to 1.0.")
                                ))
                                .binding(0.5D, () -> DRAFT.imageCompression,
                                        value -> DRAFT.imageCompression = value)
                                .controller(option -> DoubleSliderControllerBuilder.create(option)
                                        .range(0.1D, 1.0D)
                                        .step(0.05D))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Allow custom image uploads"))
                                .binding(true, () -> DRAFT.allowImageUpload,
                                        value -> DRAFT.allowImageUpload = value)
                                .controller(BooleanControllerBuilder::create)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Image frames"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Only owner can modify frames"))
                                .binding(false, () -> DRAFT.frameOnlyOwnerModify,
                                        value -> DRAFT.frameOnlyOwnerModify = value)
                                .controller(BooleanControllerBuilder::create)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Advanced data"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Enable advanced image data"))
                                .description(OptionDescription.of(
                                        Component.literal("Stores additional data in image items.")
                                ))
                                .binding(false, () -> DRAFT.advancedImageData,
                                        value -> DRAFT.advancedImageData = value)
                                .controller(BooleanControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("Maximum stored entities"))
                                .binding(16, () -> DRAFT.advancedDataMaxEntities,
                                        value -> DRAFT.advancedDataMaxEntities = value)
                                .controller(option -> IntegerFieldControllerBuilder.create(option))
                                .build())
                        .build())
                .save(() -> ClientPlayNetworking.send(new ConfigSyncPayload.Update(
                        DRAFT.imageCooldown,
                        DRAFT.cameraConsumeItemAmount,
                        DRAFT.maxImageSize,
                        DRAFT.imageCompression,
                        DRAFT.allowImageUpload,
                        DRAFT.frameOnlyOwnerModify,
                        DRAFT.advancedImageData,
                        DRAFT.advancedDataMaxEntities
                )))
                .build()
                .generateScreen(parent);
    }

    private static final class Draft {
        private int imageCooldown = 5000;
        private int cameraConsumeItemAmount = 1;
        private int maxImageSize = 200_000;
        private double imageCompression = 0.5D;
        private boolean allowImageUpload = true;
        private boolean frameOnlyOwnerModify;
        private boolean advancedImageData;
        private int advancedDataMaxEntities = 16;
    }
}
