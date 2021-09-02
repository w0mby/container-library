package ninjaphenix.container_library.internal.api.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import ninjaphenix.container_library.api.inventory.AbstractMenu;
import ninjaphenix.container_library.client.gui.PickScreen;
import ninjaphenix.container_library.wrappers.ConfigWrapper;
import ninjaphenix.container_library.wrappers.PlatformUtils;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@ApiStatus.Experimental
public abstract class AbstractScreen extends AbstractContainerScreen<AbstractMenu> {
    private final Integer inventoryLabelLeft;
    protected final int menuWidth, menuHeight, totalSlots;

    protected AbstractScreen(AbstractMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // todo: move to impl classes or re-add screenMeta
        inventoryLabelLeft = 0;
        totalSlots = menu.slots.size();
        menuWidth = ConfigWrapper.getInstance().getPreferredScreenWidth(totalSlots);
        menuHeight = ConfigWrapper.getInstance().getPreferredScreenHeight(totalSlots);
    }

    @Override
    protected void renderBg(PoseStack stack, float delta, int mouseX, int mouseY) {
        // RenderSystem.setShaderTexture(0, screenMeta.texture);
        // RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        // GuiComponent.blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight, screenMeta.textureWidth, screenMeta.textureHeight);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, delta);
        this.renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        font.draw(stack, title, 8, 6, 4210752);
        font.draw(stack, playerInventoryTitle, inventoryLabelLeft, imageHeight - 96 + 2, 4210752);
    }

    /**
     * Implementors of this method should call super to allow pick screen key bind to work.
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (PlatformUtils.getInstance().isConfigKeyPressed(keyCode, scanCode, modifiers)) {
            // todo: pick screen needs reworking as it's client only now, will need to open the new screen if an inventory is already open.
            minecraft.setScreen(new PickScreen(null, (selection) -> {}));
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE || minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected final void renderButtonTooltip(AbstractButton button, PoseStack stack, int x, int y) {
        this.renderTooltip(stack, button.getMessage(), x, y);
    }

    public abstract List<Rect2i> getExclusionZones();
}
