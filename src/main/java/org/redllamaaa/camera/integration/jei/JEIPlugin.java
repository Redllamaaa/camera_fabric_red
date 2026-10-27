
package org.redllamaaa.camera.integration.jei;

import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.ImageCloningRecipe;
import org.redllamaaa.camera.gui.AlbumScreen;
import org.redllamaaa.camera.gui.CameraScreen;
import org.redllamaaa.camera.gui.ImageScreen;
import org.redllamaaa.camera.gui.LecternAlbumScreen;
import org.redllamaaa.camera.gui.ResizeFrameScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.IExtendableCraftingRecipeCategory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        return CameraMod.id("camera");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiScreenHandler(ImageScreen.class, screen -> null);
        registration.addGuiScreenHandler(AlbumScreen.class, screen -> null);
        registration.addGuiScreenHandler(LecternAlbumScreen.class, screen -> null);
        registration.addGuiScreenHandler(ResizeFrameScreen.class, screen -> null);
        registration.addGuiScreenHandler(CameraScreen.class, screen -> null);

        registration.addGlobalGuiHandler(new IGlobalGuiHandler() {
            @Override
            public java.util.Collection<Rect2i> getGuiExtraAreas() {
                var screen = Minecraft.getInstance().gui.screen();
                if (screen instanceof ImageScreen
                        || screen instanceof AlbumScreen
                        || screen instanceof LecternAlbumScreen
                        || screen instanceof ResizeFrameScreen
                        || screen instanceof CameraScreen) {
                    var window = Minecraft.getInstance().getWindow();
                    return List.of(new Rect2i(
                            0,
                            0,
                            window.getGuiScaledWidth(),
                            window.getGuiScaledHeight()
                    ));
                }
                return List.of();
            }
        });
    }

    @Override
    public void registerVanillaCategoryExtensions(
            IVanillaCategoryExtensionRegistration registration
    ) {
        IExtendableCraftingRecipeCategory craftingCategory =
                registration.getCraftingCategory();

        craftingCategory.addExtension(
                ImageCloningRecipe.class,
                new ImageCloneExtension<>()
        );
    }
}
