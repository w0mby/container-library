package ninjaphenix.container_library.api.client.function;

// To prevent anonymous class creation as ScreenSizeRetiever was planned to have a getWidth(slots) and getHeight(slots)
@SuppressWarnings("ClassCanBeRecord")
public final class ScreenSize {
    private final int width, height;

    private ScreenSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static ScreenSize of(int width, int height) {
        return new ScreenSize(width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}