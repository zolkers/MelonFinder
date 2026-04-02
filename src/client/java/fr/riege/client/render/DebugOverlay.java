package fr.riege.client.render;

import fr.riege.api.event.IEventBus;
import fr.riege.api.path.Path;
import fr.riege.api.path.PathStatus;
import fr.riege.client.event.events.RenderHudEvent;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DebugOverlay {

    private static final int TEXT_X = 4;
    private static final int TEXT_Y = 4;
    private static final int COLOR_FOUND   = 0xFF00FF00;
    private static final int COLOR_IDLE    = 0xFFAAAAAA;
    private static final int COLOR_ERROR   = 0xFFFF4444;
    private static final String PATH_TAG = "Path: ";

    @Nullable
    private Path currentPath;
    @Nullable
    private volatile String statusOverride;

    public void register(@NotNull IEventBus bus) {
        bus.subscribe(RenderHudEvent.class, this, this::onRenderHud);
    }

    public void setPath(@Nullable Path path) {
        this.currentPath = path;
        this.statusOverride = null;
    }

    public void setStatus(@NotNull String text) {
        this.statusOverride = text;
    }

    private void onRenderHud(@NotNull RenderHudEvent event) {
        String line = buildLine();
        if (line == null) return;

        event.getGraphics().drawString(Minecraft.getInstance().font, line, TEXT_X, TEXT_Y, resolveColor(), false);
    }

    @Nullable
    private String buildLine() {
        String override = statusOverride;
        if (override != null && !override.isEmpty()) return PATH_TAG + override;

        Path path = currentPath;
        if (path == null) return null;
        if (path.status() == PathStatus.FOUND) {
            return PATH_TAG + path.segments().size() + " segments";
        }
        return PATH_TAG + path.status().name().toLowerCase();
    }

    private int resolveColor() {
        Path path = currentPath;
        if (path == null) return COLOR_IDLE;
        if (path.status() == PathStatus.FOUND) return COLOR_FOUND;
        if (path.status() == PathStatus.UNREACHABLE) return COLOR_ERROR;
        return COLOR_IDLE;
    }
}
