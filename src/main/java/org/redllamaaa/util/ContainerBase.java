package org.redllamaaa.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu with a block of container slots followed by the player inventory (3 rows plus hotbar).
 * Subclasses add their own slots first, then call {@link #addPlayerInventorySlots()}.
 */
public abstract class ContainerBase extends AbstractContainerMenu {

    protected final Container inventory;
    protected final Container playerInventory;

    public ContainerBase(MenuType<?> menuType, int id, Container playerInventory, Container inventory) {
        super(menuType, id);
        this.playerInventory = playerInventory;
        this.inventory = inventory;
    }

    protected void addPlayerInventorySlots() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18 + getInvOffset()));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142 + getInvOffset()));
        }
    }

    /**
     * Vertical offset applied to the player inventory slots.
     */
    public int getInvOffset() {
        return 0;
    }

    /**
     * Number of slots belonging to the container, used to split shift-click moves.
     */
    public int getInventorySize() {
        return inventory.getContainerSize();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < getInventorySize()) {
                if (!moveItemStackTo(stack, getInventorySize(), slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, getInventorySize(), false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        inventory.stopOpen(player);
    }

}
