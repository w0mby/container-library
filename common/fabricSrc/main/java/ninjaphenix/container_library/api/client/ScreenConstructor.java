package ninjaphenix.container_library.api.client;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import ninjaphenix.container_library.api.client.function.ScreenSize;
import ninjaphenix.container_library.api.client.gui.AbstractScreen;
import ninjaphenix.container_library.api.inventory.AbstractHandler;
import org.jetbrains.annotations.ApiStatus;

public interface ScreenConstructor<T extends AbstractScreen> {
    @Deprecated
    @ApiStatus.Internal
    @SuppressWarnings("DeprecatedIsStillUsed")
    ScreenConstructor<AbstractScreen> NULL = (handler, playerInventory, title, screenSize) -> null;

    T createScreen(AbstractHandler handler, PlayerInventory playerInventory, Text title, ScreenSize screenSize);
}