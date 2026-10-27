
package org.redllamaaa.camera;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ServerEvents {

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            for (Player player : world.players()) {
                if (player.getMainHandItem().is(CameraMod.CAMERA)
                        || player.getOffhandItem().is(CameraMod.CAMERA)) {
                    continue;
                }

                disableCamera(player.getInventory().getSelectedItem());

                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    disableCamera(player.getInventory().getItem(i));
                }
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            for (InteractionHand interactionHand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(interactionHand);

                if (stack.is(CameraMod.CAMERA) && CameraMod.CAMERA.isActive(stack)) {
                    return InteractionResult.FAIL;
                }
            }

            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            for (InteractionHand interactionHand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(interactionHand);

                if (stack.is(CameraMod.CAMERA) && CameraMod.CAMERA.isActive(stack)) {
                    return InteractionResult.FAIL;
                }
            }

            return InteractionResult.PASS;
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            Entity attacker = source.getDirectEntity();

            if (!(attacker instanceof Player player)) {
                return true;
            }

            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);

                if (stack.is(CameraMod.CAMERA) && CameraMod.CAMERA.isActive(stack)) {
                    return false;
                }
            }

            return true;
        });

        net.fabricmc.fabric.api.event.player.PlayerDropItemCallback.EVENT.register(
                (player, item) -> {
                    disableCamera(item.getItem());
                    return item;
                }
        );
    }

    private static void disableCamera(ItemStack stack) {
        if (stack.is(CameraMod.CAMERA)) {
            CameraMod.CAMERA.setActive(stack, false);
        }
    }
}
