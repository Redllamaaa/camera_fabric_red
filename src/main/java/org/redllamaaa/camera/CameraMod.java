package org.redllamaaa.camera;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redllamaaa.camera.entities.ImageEntity;
import org.redllamaaa.camera.gui.AlbumContainer;
import org.redllamaaa.camera.gui.AlbumInventoryContainer;
import org.redllamaaa.camera.items.AlbumItem;
import org.redllamaaa.camera.items.CameraItem;
import org.redllamaaa.camera.items.ImageFrameItem;
import org.redllamaaa.camera.items.ImageItem;
import org.redllamaaa.camera.net.ConfigNetworking;
import org.redllamaaa.camera.net.PacketManager;
import org.redllamaaa.camera.net.MessageResizeFrame;
import org.redllamaaa.camera.net.MessageDisableCameraMode;
import org.redllamaaa.camera.net.MessagePartialImage;
import org.redllamaaa.camera.net.MessageRequestImage;
import org.redllamaaa.camera.net.MessageRequestUploadCustomImage;
import org.redllamaaa.camera.net.MessageSetShader;
import org.redllamaaa.camera.net.MessageTakeBook;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class CameraMod implements ModInitializer {

    public static final String MODID = "camera_fabric";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static PacketManager PACKET_MANAGER;

    // ---------------------------------------------------------------------
    // Entity type (registered before items, since items may reference it)
    // ---------------------------------------------------------------------

    public static final EntityType<ImageEntity> IMAGE_ENTITY_TYPE = registerEntityType("image_frame",
            EntityType.Builder.<ImageEntity>of(ImageEntity::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .clientTrackingRange(256)
                    .updateInterval(20));

    // ---------------------------------------------------------------------
    // Items
    // ---------------------------------------------------------------------

    public static final ImageFrameItem FRAME_ITEM = registerItem("image_frame", ImageFrameItem::new);
    public static final CameraItem CAMERA = registerItem("camera", CameraItem::new);
    public static final ImageItem IMAGE = registerItem("image", ImageItem::new);
    public static final AlbumItem ALBUM = registerItem("album", AlbumItem::new);

    // ---------------------------------------------------------------------
    // Menus
    // ---------------------------------------------------------------------

    public static final MenuType<AlbumInventoryContainer> ALBUM_INVENTORY_CONTAINER = registerMenu("album_inventory",
            AlbumInventoryContainer::new);
    public static final MenuType<AlbumContainer> ALBUM_CONTAINER = registerMenu("album",
            (windowId, inv) -> new AlbumContainer(windowId));

    // ---------------------------------------------------------------------
    // Recipe serializer
    // ---------------------------------------------------------------------

    public static final RecipeSerializer<ImageCloningRecipe> IMAGE_CLONING_SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER, id("image_cloning"), ImageCloningRecipe.RECIPE_SERIALIZER);

    // ---------------------------------------------------------------------
    // Data components
    // ---------------------------------------------------------------------

    public static final DataComponentType<ImageData> IMAGE_DATA_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, id("image"),
            DataComponentType.<ImageData>builder()
                    .persistent(ImageData.CODEC)
                    .networkSynchronized(ImageData.STREAM_CODEC)
                    .build());

    public static final DataComponentType<Unit> ACTIVE_DATA_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, id("active"),
            DataComponentType.<Unit>builder()
                    .networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
                    .build());

    public static final DataComponentType<String> SHADER_DATA_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, id("shader"),
            DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    // ---------------------------------------------------------------------
    // Entity data serializer (vanilla registry, not a Fabric/NeoForge one)
    // ---------------------------------------------------------------------

    public static final EntityDataSerializer<Optional<UUID>> UUID_ENTITY_DATA_SERIALIZER = EntityDataSerializer
            .forValueType(ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC));

    static {
        EntityDataSerializers.registerSerializer(UUID_ENTITY_DATA_SERIALIZER);
    }

    // ---------------------------------------------------------------------
    // Tags
    // ---------------------------------------------------------------------

    public static final TagKey<Item> IMAGE_PAPER = ItemTags.create(id("image_paper"));

    // ---------------------------------------------------------------------
    // Config
    // ---------------------------------------------------------------------

    public static final ServerConfig SERVER_CONFIG = new ServerConfig();

    @Override
    public void onInitialize() {
        PACKET_MANAGER = new PacketManager();

        ModSounds.init();

        SERVER_CONFIG.load();
        ConfigNetworking.register();

        registerPayloads();
    }

    private static void registerPayloads() {
        MessagePartialImage.register();
        MessageRequestImage.register();
        MessageRequestUploadCustomImage.register();
        MessageSetShader.register();
        MessageDisableCameraMode.register();
        MessageResizeFrame.register();
        MessageTakeBook.register();
        // TODO: replaces RegisterPayloadHandlersEvent + CommonRegistry.registerMessage.
        // For each message, register its payload type + codec on the correct side(s), e.g.:
        //   PayloadTypeRegistry.playC2S().register(MessageTakeImage.TYPE, MessageTakeImage.STREAM_CODEC);
        //   PayloadTypeRegistry.playS2C().register(MessageImage.TYPE, MessageImage.STREAM_CODEC);
        // then attach handlers with ServerPlayNetworking.registerGlobalReceiver (server)
        // and ClientPlayNetworking.registerGlobalReceiver (client, from your ClientModInitializer).
        //
        // Messages to port: MessagePartialImage, MessageTakeImage, MessageRequestImage, MessageImage,
        // MessageImageUnavailable, MessageSetShader, MessageDisableCameraMode, MessageResizeFrame,
        // MessageRequestUploadCustomImage, MessageUploadCustomImage, MessageAlbumPage, MessageTakeBook
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    private static <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(name));
        // Since 1.21.2 the item id must be set on the properties before construction
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
    }

    private static <T extends AbstractContainerMenu> MenuType<T> registerMenu(String name, MenuType.MenuSupplier<T> factory) {
        return Registry.register(BuiltInRegistries.MENU, id(name), new MenuType<>(factory, FeatureFlags.VANILLA_SET));
    }

    private static <T extends Entity> EntityType<T> registerEntityType(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

}