package ninjaphenix.container_library.api.v2.client;

import net.minecraft.block.Block;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.server.SBlockActionPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import ninjaphenix.container_library.api.client.ScreenConstructor;
import ninjaphenix.container_library.api.client.function.ScreenSizePredicate;
import ninjaphenix.container_library.api.client.function.ScreenSizeRetriever;
import ninjaphenix.container_library.api.client.gui.AbstractScreen;
import ninjaphenix.container_library.client.gui.PickScreen;
import ninjaphenix.container_library.wrappers.ConfigWrapper;
import ninjaphenix.container_library.wrappers.NetworkWrapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;

public final class NCL_ClientApiV2 {
    private NCL_ClientApiV2() {
        throw new IllegalStateException("NCL_ClientApi cannot be instantiated.");
    }

    /**
     * Call on client side to attempt to open an inventory, sort of internal, should be accessed through:
     * {@link ninjaphenix.container_library.api.v2.OpenableBlockEntityProviderV2}.
     *
     * @return true if a valid screen type is already selected.
     */
    public static boolean openInventoryAt(BlockPos pos, Hand hand, BlockRayTraceResult hit) {
        Objects.requireNonNull(pos, "pos must not be null");
        if (!AbstractScreen.isScreenTypeDeclared(ConfigWrapper.getInstance().getPreferredScreenType())) {
            Minecraft.getInstance().setScreen(new PickScreen(() -> {
                Minecraft.getInstance().getConnection().send(new CPlayerTryUseItemOnBlockPacket(hand, hit));
            }));
            return false;
        }
        return true;
    }

    /**
     * Register button for screen type pick screen with an optional error message.
     * Note: texture must be 96 x 288 ( 3 images: normal, hovered, current )
     */
    public static void registerScreenButton(ResourceLocation type, ResourceLocation texture, ITextComponent title, ScreenSizePredicate warningTest, List<ITextComponent> warningText) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(texture, "texture must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(warningTest, "warningTest must not be null");
        Objects.requireNonNull(warningText, "warningText must not be null");
        //noinspection deprecation
        PickScreen.declareButtonSettings(type, texture, title, warningTest, warningText);
    }

    /**
     * Register button for screen type pick screen
     * Note: texture must be 96 x 288 ( 3 images: normal, hovered, current )
     */
    public static void registerScreenButton(ResourceLocation type, ResourceLocation texture, ITextComponent title) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(texture, "texture must not be null");
        Objects.requireNonNull(title, "title must not be null");
        //noinspection deprecation
        PickScreen.declareButtonSettings(type, texture, title, ScreenSizePredicate::noTest, Collections.emptyList());
    }

    /**
     * Register screen constructor.
     */
    public static void registerScreenType(ResourceLocation type, ScreenConstructor<?> screenConstructor) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(screenConstructor, "screenConstructor must not be null");
        //noinspection deprecation
        AbstractScreen.declareScreenType(type, screenConstructor);
    }

    /**
     * Register default screen sizes, it is planned to allow players to override the default screen sizes in the future.
     */
    public static void registerDefaultScreenSize(ResourceLocation type, ScreenSizeRetriever retriever) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(retriever, "retriever must not be null");
        //noinspection deprecation
        AbstractScreen.declareScreenSizeRetriever(type, retriever);
    }

    /**
     * Uses the single screen type over the specified type if the single screen will visually fit in the game window.
     * Note: May be renamed in the future.
     */
    public static void setPrefersSingleScreen(ResourceLocation type) {
        Objects.requireNonNull(type, "type must not be null");
        AbstractScreen.setPrefersSingleScreen(type);
    }
}
