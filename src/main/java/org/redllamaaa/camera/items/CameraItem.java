package org.redllamaaa.camera.items;

import org.redllamaaa.camera.CameraClientMod;
import org.redllamaaa.camera.CameraMod;
import org.redllamaaa.camera.ModSounds;
import org.redllamaaa.camera.net.MessageTakeImage;
import org.redllamaaa.util.ItemUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CameraItem extends Item {

    public CameraItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown() && !isActive(stack)) {
            if (level.isClientSide()) {
                CameraClientMod.openCameraScreen(
                        stack.get(CameraMod.SHADER_DATA_COMPONENT)
                );
            }

            return InteractionResultHolder.success(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }

        if (!isActive(stack)) {
            setActive(stack, true);
        } else if (CameraMod.PACKET_MANAGER.canTakeImage(player.getUUID())) {
            if (consumePaper(player)) {
                level.playSound(
                        null,
                        player.blockPosition(),
                        ModSounds.TAKE_IMAGE,
                        SoundSource.AMBIENT,
                        1.0F,
                        1.0F
                );

                UUID uuid = UUID.randomUUID();

                // Replace with your Fabric networking implementation.
                MessageTakeImage.sendToPlayer(serverPlayer, uuid);

                setActive(stack, false);
            } else {
                player.displayClientMessage(
                        Component.translatable("message.no_consumable"),
                        true
                );
            }
        } else {
            player.displayClientMessage(
                    Component.translatable("message.image_cooldown"),
                    true
            );
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 50000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return isActive(stack) ? UseAnim.BOW : UseAnim.NONE;
    }

    public static boolean consumePaper(Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        int amountNeeded = CameraMod.SERVER_CONFIG.cameraConsumeItemAmount;
        List<ItemStack> consumeStacks = findPaper(player);

        int count = 0;

        for (ItemStack stack : consumeStacks) {
            count += stack.getCount();
        }

        if (count < amountNeeded) {
            return false;
        }

        for (ItemStack stack : consumeStacks) {
            if (amountNeeded <= 0) {
                break;
            }

            int amountToConsume = Math.min(stack.getCount(), amountNeeded);
            stack.shrink(amountToConsume);
            amountNeeded -= amountToConsume;
        }

        return true;
    }

    private static List<ItemStack> findPaper(Player player) {
        List<ItemStack> items = new ArrayList<>();

        if (isPaper(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            items.add(player.getItemInHand(InteractionHand.MAIN_HAND));
        }

        if (isPaper(player.getItemInHand(InteractionHand.OFF_HAND))) {
            items.add(player.getItemInHand(InteractionHand.OFF_HAND));
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (isPaper(stack)) {
                items.add(stack);
            }
        }

        return items;
    }

    protected static boolean isPaper(ItemStack stack) {
        return stack.is(CameraMod.IMAGE_PAPER);
    }

    public boolean isActive(ItemStack stack) {
        return stack.has(CameraMod.ACTIVE_DATA_COMPONENT);
    }

    public void setActive(ItemStack stack, boolean active) {
        if (active) {
            stack.set(CameraMod.ACTIVE_DATA_COMPONENT, Unit.INSTANCE);
        } else {
            stack.remove(CameraMod.ACTIVE_DATA_COMPONENT);
        }
    }
}