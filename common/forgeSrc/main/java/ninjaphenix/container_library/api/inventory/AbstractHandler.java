package ninjaphenix.container_library.api.inventory;

import ninjaphenix.container_library.CommonMain;
import ninjaphenix.container_library.Utils;

import java.util.function.IntUnaryOperator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AbstractHandler extends AbstractContainerMenu {
    private final Container inventory;

    public AbstractHandler(int syncId, Container inventory, Inventory playerInventory) {
        super(CommonMain.getScreenHandlerType(), syncId);
        this.inventory = inventory;
        inventory.startOpen(playerInventory.player);
        if (playerInventory.player instanceof ServerPlayer) {
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                this.addSlot(new Slot(inventory, i, i * Utils.SLOT_SIZE, 0));
            }
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 3; y++) {
                    this.addSlot(new Slot(playerInventory, y * 9 + x + 9, Utils.SLOT_SIZE * x, y * Utils.SLOT_SIZE));
                }
            }
            for (int i = 0; i < 9; i++) {
                this.addSlot(new Slot(playerInventory, i, i * Utils.SLOT_SIZE, 2 * Utils.SLOT_SIZE));
            }
        }
    }

    // Client only
    public static AbstractHandler createClientMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new AbstractHandler(syncId, new SimpleContainer(buffer.readInt()), playerInventory);
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

    // Public API, required for mods to check if blocks should be considered open
    public Container getInventory() {
        return inventory;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack newStack = slot.getItem();
            originalStack = newStack.copy();
            if (index < inventory.getContainerSize()) {
                if (!this.moveItemStackTo(newStack, inventory.getContainerSize(), inventory.getContainerSize() + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(newStack, 0, inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }
            if (newStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return originalStack;
    }

    // Below are client only methods
    public void resetSlotPositions(boolean createSlots, int menuWidth, int menuHeight) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            int slotXPos = i % menuWidth;
            int slotYPos = Mth.ceil((((double) (i - slotXPos)) / menuWidth));
            int realYPos = slotYPos >= menuHeight ? (Utils.SLOT_SIZE * (slotYPos % menuHeight)) - 2000 : slotYPos * Utils.SLOT_SIZE;
            if (createSlots) {
                this.addSlot(new Slot(inventory, i, slotXPos * Utils.SLOT_SIZE + 8, realYPos + Utils.SLOT_SIZE));
            } else {
                slots.get(i).y = realYPos + Utils.SLOT_SIZE;
            }
        }
    }

    public void moveSlotRange(int minSlotIndex, int maxSlotIndex, int yDifference) {
        for (int i = minSlotIndex; i < maxSlotIndex; i++) {
            slots.get(i).y += yDifference;
        }
    }

    public void setSlotRange(int minSlotIndex, int maxSlotIndex, IntUnaryOperator yMutator) {
        for (int i = minSlotIndex; i < maxSlotIndex; i++) {
            slots.get(i).y = yMutator.applyAsInt(i);
        }
    }

    public void clearSlots() {
        this.slots.clear();
        this.remoteSlots.clear();
        this.lastSlots.clear();
    }

    public void addClientSlot(Slot slot) {
        this.addSlot(slot);
    }
}
