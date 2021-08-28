package ninjaphenix.container_library.api.helpers;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import ninjaphenix.container_library.internal.api.function.InventorySlotAccessor;

import java.util.Arrays;
import java.util.Set;

public final class VariableInventory implements Container {
    private final Container[] parts;
    private final int size;
    private final int maxStackSize;

    private VariableInventory(Container... parts) {
        for (int i = 0; i < parts.length; i++) {
            assert parts[i] != null : "part at index " + i + " must not be null";
        }
        this.parts = parts;
        this.size = Arrays.stream(parts).mapToInt(Container::getContainerSize).sum();
        this.maxStackSize = parts[0].getMaxStackSize();
        for (Container part : parts) {
            assert part.getMaxStackSize() == maxStackSize : "all parts must have equal max stack sizes.";
        }
    }

    public static Container of(Container... parts) {
        assert parts.length > 0 : "parts must contain at least 1 item";
        if (parts.length == 1) {
            return parts[0];
        } else {
            return new VariableInventory(parts);
        }
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (Container part : parts) {
            if (!part.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        assert slot > 0 && slot <= this.getMaxStackSize() : "slot index out of range";
        return this.getPartAccessor(slot).apply(Container::getItem);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        assert slot > 0 && slot <= this.getMaxStackSize() : "slot index out of range";
        return this.getPartAccessor(slot).apply((part, rSlot) -> part.removeItem(rSlot, amount));
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        assert slot > 0 && slot <= this.getMaxStackSize() : "slot index out of range";
        return this.getPartAccessor(slot).apply(Container::removeItemNoUpdate);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        assert slot > 0 && slot <= this.getMaxStackSize() : "slot index out of range";
        this.getPartAccessor(slot).consume((part, rSlot) -> part.setItem(rSlot, stack));
    }

    @Override
    public int getMaxStackSize() {
        return maxStackSize;
    }

    @Override
    public void setChanged() {
        for (Container part : parts) {
            part.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        for (Container part : parts) {
            if (!part.stillValid(player)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void startOpen(Player player) {
        for (Container part : parts) {
            part.startOpen(player);
        }
    }

    @Override
    public void stopOpen(Player player) {
        for (Container part : parts) {
            part.stopOpen(player);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        assert slot > 0 && slot <= this.getMaxStackSize() : "slot index out of range";
        return this.getPartAccessor(slot).apply((part, rSlot) -> part.canPlaceItem(rSlot, stack));
    }

    @Override
    public int countItem(Item item) {
        int count = 0;
        for (Container part : parts) {
            count += part.countItem(item);
        }
        return count;
    }

    @Override
    public boolean hasAnyOf(Set<Item> set) {
        for (Container part : parts) {
            if (part.hasAnyOf(set)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearContent() {
        for (Container part : parts) {
            part.clearContent();
        }
    }

    private InventorySlotAccessor<Container> getPartAccessor(int slot) {
        for (Container part : parts) {
            int inventorySize = part.getContainerSize();
            if (slot > inventorySize) {
                slot -= inventorySize;
            } else {
                return new InventorySlotAccessor<>(part, slot);
            }
        }
        throw new IllegalStateException("getPartAccessor called without validating slot bounds.");
    }
}
