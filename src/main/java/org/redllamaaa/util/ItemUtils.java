package org.redllamaaa.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public final class ItemUtils {

    private ItemUtils() {
    }

    /**
     * Adds the given amount to the stack, clamped to [0, max stack size].
     * A negative amount removes items. Creative players keep the stack untouched.
     *
     * @param amount the amount to add (negative to remove)
     * @param stack  the stack to modify
     * @param player the player, or null
     * @return the resulting stack, or {@link ItemStack#EMPTY} if nothing is left
     */
    public static ItemStack itemStackAmount(int amount, @Nullable ItemStack stack, @Nullable Player player) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (player != null && player.getAbilities().instabuild) {
            return stack;
        }

        stack.setCount(stack.getCount() + amount);
        if (stack.getCount() <= 0) {
            stack.setCount(0);
            return ItemStack.EMPTY;
        }

        if (stack.getCount() > stack.getMaxStackSize()) {
            stack.setCount(stack.getMaxStackSize());
        }

        return stack;
    }

}
