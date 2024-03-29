package ninjaphenix.container_library.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ninjaphenix.container_library.Utils;

public final class ScreenPickButton extends ButtonWidget {
    private static final Identifier WARNING_TEXTURE = Utils.id("textures/gui/warning.png");
    private final Identifier texture;
    private final boolean showWarningSymbol;
    private final boolean isCurrentPreference;

    public ScreenPickButton(int x, int y, int width, int height, Identifier texture, Text message, boolean showWarningSymbol, boolean isCurrentPreference, PressAction onPress, TooltipSupplier onTooltip) {
        super(x, y, width, height, message, onPress, onTooltip);
        this.texture = texture;
        this.showWarningSymbol = showWarningSymbol;
        this.isCurrentPreference = isCurrentPreference;
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        DrawableHelper.drawTexture(stack, x, y, 0, height * (this.isHovered() ? 1 : isCurrentPreference ? 2 : 0), width, height, width, height * 3);
        if (showWarningSymbol) {
            RenderSystem.setShaderTexture(0, WARNING_TEXTURE);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
            DrawableHelper.drawTexture(stack, x + width - 28, y + 9, 0, 0, 16, 32, 16, 32);
        }
    }

    public void renderButtonTooltip(MatrixStack stack, int mouseX, int mouseY) {
        if (hovered) {
            this.renderTooltip(stack, mouseX, mouseY);
        } else if (this.isFocused()) {
            this.renderTooltip(stack, x, y);
        }
    }
}
