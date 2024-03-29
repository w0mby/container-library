package ninjaphenix.container_library.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

public interface ClientScreenHandlerFactory<T extends ScreenHandler> {
    T create(int syncId, PlayerInventory playerInventory, PacketByteBuf buffer);
}
