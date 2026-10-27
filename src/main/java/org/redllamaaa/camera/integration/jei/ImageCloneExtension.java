
package org.redllamaaa.camera.integration.jei;

import org.redllamaaa.camera.ImageCloningRecipe;
import org.redllamaaa.camera.ImageData;
import org.redllamaaa.camera.CameraMod;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageCloneExtension<T extends ImageCloningRecipe> implements ICraftingCategoryExtension<T> {

    @Override
    public List<SlotDisplay> getIngredients(RecipeHolder<T> recipeHolder) {
        List<SlotDisplay> result = new ArrayList<>();

        ItemStack image = new ItemStack(CameraMod.IMAGE);
        ImageData.dummy().addToImage(image);
        result.add(new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromStack(image)));

        return result;
    }

    @Override
    public void setRecipe(
            RecipeHolder<T> recipeHolder,
            IRecipeLayoutBuilder builder,
            ICraftingGridHelper craftingGridHelper,
            IFocusGroup focuses
    ) {
        ItemStack image = new ItemStack(CameraMod.IMAGE);
        ImageData.dummy().addToImage(image);

        List<ItemStack> paper = recipeHolder.value()
                .getPaper()
                .items()
                .map(Holder::value)
                .map(ItemStack::new)
                .toList();

        ItemStack out = image.copy();

        craftingGridHelper.createAndSetInputs(
                builder,
                VanillaTypes.ITEM_STACK,
                Arrays.asList(List.of(image), paper),
                0,
                0
        );

        craftingGridHelper.createAndSetOutputs(
                builder,
                VanillaTypes.ITEM_STACK,
                List.of(out)
        );
    }
}
