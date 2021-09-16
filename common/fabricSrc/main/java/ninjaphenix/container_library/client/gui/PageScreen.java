package ninjaphenix.container_library.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import ninjaphenix.container_library.Utils;
import ninjaphenix.container_library.api.client.function.ScreenSize;
import ninjaphenix.container_library.api.client.gui.AbstractScreen;
import ninjaphenix.container_library.api.client.gui.TexturedRect;
import ninjaphenix.container_library.api.inventory.AbstractHandler;
import ninjaphenix.container_library.client.gui.widget.PageButton;
import ninjaphenix.container_library.wrappers.PlatformUtils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PageScreen extends AbstractScreen {
    private final Identifier textureLocation;
    private final int textureWidth, textureHeight;
    private final Set<TexturedRect> blankArea = new LinkedHashSet<>();
    private final int blankSlots, pages;
    private PageButton leftPageButton, rightPageButton;
    private int page;
    private TranslatableText currentPageText;
    private float pageTextX;

    public PageScreen(AbstractHandler handler, PlayerInventory playerInventory, Text title, ScreenSize screenSize) {
        super(handler, playerInventory, title, screenSize);

        this.initializeSlots(playerInventory);

        textureLocation = new Identifier("ninjaphenix_container_lib", "textures/gui/container/shared_" + menuWidth + "_" + menuHeight + ".png");
        textureWidth = switch (menuWidth) {
            case 9 -> 208;
            case 12 -> 256;
            case 15 -> 320;
            case 18 -> 368;
            default -> throw new IllegalStateException("Unexpected value: " + menuWidth);
        };
        textureHeight = switch (menuHeight) {
            case 3 -> 192;
            case 6 -> 240;
            case 9 -> 304;
            default -> throw new IllegalStateException("Unexpected value: " + menuHeight);
        };

        int slotsPerPage = menuWidth * menuHeight;
        pages = MathHelper.ceil((double) totalSlots / slotsPerPage);
        int lastPageSlots = totalSlots - (pages - 1) * slotsPerPage;
        blankSlots = slotsPerPage - lastPageSlots;

        backgroundWidth = 14 + 18 * menuWidth;
        backgroundHeight = 17 + 97 + 18 * menuHeight;
    }

    private static boolean regionIntersects(ClickableWidget widget, int x, int y, int width, int height) {
        return widget.x <= x + width && y <= widget.y + widget.getHeight() ||
                x <= widget.x + widget.getWidth() && widget.y <= y + height;
    }

    @Override
    protected void drawBackground(MatrixStack stack, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, textureLocation);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        DrawableHelper.drawTexture(stack, x, y, 0, 0, backgroundWidth, backgroundHeight, textureWidth, textureHeight);
        blankArea.forEach(image -> image.render(stack));
    }

    private void initializeSlots(PlayerInventory playerInventory) {
        handler.resetSlotPositions(true, menuWidth, menuHeight);
        int left = (menuWidth * Utils.SLOT_SIZE + 14) / 2 - 80;
        int top = Utils.SLOT_SIZE + 14 + (menuHeight * Utils.SLOT_SIZE);
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                handler.addClientSlot(new Slot(playerInventory, y * 9 + x + 9, left + Utils.SLOT_SIZE * x, top + y * Utils.SLOT_SIZE));
            }
        }
        for (int x = 0; x < 9; x++) {
            handler.addClientSlot(new Slot(playerInventory, x, left + Utils.SLOT_SIZE * x, top + 58));
        }
    }

    private void setPage(int oldPage, int newPage) {
        if (newPage == 0 || newPage > pages) {
            return;
        }
        page = newPage;
        if (newPage > oldPage) {
            if (page == pages) {
                rightPageButton.setActive(false);
                if (blankSlots > 0) {
                    int rows = MathHelper.floorDiv(blankSlots, menuWidth);
                    int remainder = (blankSlots - menuWidth * rows);
                    int yTop = y + Utils.CONTAINER_HEADER_HEIGHT + (menuHeight - 1) * Utils.SLOT_SIZE;
                    int xLeft = x + Utils.CONTAINER_PADDING_WIDTH;
                    for (int i = 0; i < rows; i++) {
                        blankArea.add(new TexturedRect(xLeft, yTop, menuWidth * Utils.SLOT_SIZE, Utils.SLOT_SIZE,
                                Utils.CONTAINER_PADDING_WIDTH, backgroundHeight, textureWidth, textureHeight));
                        yTop -= Utils.SLOT_SIZE;
                    }
                    if (remainder > 0) {
                        int xRight = x + Utils.CONTAINER_PADDING_WIDTH + menuWidth * Utils.SLOT_SIZE;
                        int width = remainder * Utils.SLOT_SIZE;
                        blankArea.add(new TexturedRect(xRight - width, yTop, width, Utils.SLOT_SIZE,
                                Utils.CONTAINER_PADDING_WIDTH, backgroundHeight, textureWidth, textureHeight));
                    }
                }
            }
            if (!leftPageButton.active) {
                leftPageButton.setActive(true);
            }
        } else if (newPage < oldPage) {
            if (page == 1) {
                leftPageButton.setActive(false);
            }
            blankArea.clear();
            if (!rightPageButton.active) {
                rightPageButton.setActive(true);
            }
        }
        int slotsPerPage = menuWidth * menuHeight;
        int oldMin = slotsPerPage * (oldPage - 1);
        int oldMax = Math.min(oldMin + slotsPerPage, totalSlots);
        handler.moveSlotRange(oldMin, oldMax, -2000);
        int newMin = slotsPerPage * (newPage - 1);
        int newMax = Math.min(newMin + slotsPerPage, totalSlots);
        handler.moveSlotRange(newMin, newMax, 2000);
        this.setPageText();
    }

    private void setPageText() {
        currentPageText = new TranslatableText("screen.ninjaphenix_container_lib.page_x_y", page, pages);
    }

    @Override
    protected void drawMouseoverTooltip(MatrixStack stack, int mouseX, int mouseY) {
        super.drawMouseoverTooltip(stack, mouseX, mouseY);
        leftPageButton.renderButtonTooltip(stack, mouseX, mouseY);
        rightPageButton.renderButtonTooltip(stack, mouseX, mouseY);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        int currentPage = page;
        if (currentPage != 1) {
            handler.resetSlotPositions(false, menuWidth, menuHeight);
            super.resize(client, width, height);
            blankArea.clear();
            this.setPage(1, currentPage);
            return;
        }
        super.resize(client, width, height);
    }

    @Override
    protected void drawForeground(MatrixStack stack, int mouseX, int mouseY) {
        textRenderer.draw(stack, title, 8, 6, 0x404040);
        textRenderer.draw(stack, playerInventoryTitle, 8, backgroundHeight - 96 + 2, 0x404040);
        if (currentPageText != null) {
            textRenderer.draw(stack, currentPageText.asOrderedText(), pageTextX - x, backgroundHeight - 94, 0x404040);
        }
    }

    @Override
    protected boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            this.setPage(page, Screen.hasShiftDown() ? pages : page + 1);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            this.setPage(page, Screen.hasShiftDown() ? 1 : page - 1);
            return true;
        }
        return false;
    }

    public List<Rect2i> getExclusionZones() {
        return Collections.emptyList();
    }

    public void addPageButtons() {
        int width = 54;
        int x = this.x + backgroundWidth - 61;
        int originalX = x;
        int y = this.y + backgroundHeight - 96;
        List<ClickableWidget> renderableChildren = new ArrayList<>();
        for (var child : this.children()) {
            if (child instanceof ClickableWidget widget) {
                renderableChildren.add(widget);
            }
        }
        renderableChildren.sort(Comparator.comparingInt(a -> -a.x));
        for (ClickableWidget widget : renderableChildren) {
            if (PageScreen.regionIntersects(widget, x, y, width, 12)) {
                x = widget.x - width - 2;
            }
        }
        page = 1;
        this.setPageText();
        // Honestly this is dumb.
        if (x == originalX && PlatformUtils.isModLoaded("inventoryprofiles")) {
            x -= 14;
        }
        leftPageButton = new PageButton(x, y, 0,
                new TranslatableText("screen.ninjaphenix_container_lib.prev_page"), button -> this.setPage(page, page - 1),
                this::renderButtonTooltip);
        leftPageButton.active = false;
        this.addDrawableChild(leftPageButton);
        rightPageButton = new PageButton(x + 42, y, 1,
                new TranslatableText("screen.ninjaphenix_container_lib.next_page"), button -> this.setPage(page, page + 1),
                this::renderButtonTooltip);
        this.addDrawableChild(rightPageButton);
        pageTextX = (1 + leftPageButton.x + rightPageButton.x - rightPageButton.getWidth() / 2F) / 2F;
    }

    private void renderButtonTooltip(PressableWidget button, MatrixStack stack, int x, int y) {
        this.renderTooltip(stack, button.getMessage(), x, y);
    }
}