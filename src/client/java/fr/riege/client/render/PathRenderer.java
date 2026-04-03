package fr.riege.client.render;

import fr.riege.api.event.IEventBus;
import fr.riege.api.math.Vec3;
import fr.riege.api.path.Path;
import fr.riege.api.path.PathStatus;
import fr.riege.api.path.Segment;
import fr.riege.client.event.events.RenderWorldEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PathRenderer {

    private static final float  NODE_BOX_SIZE  = 0.25f;
    private static final float  LINE_WIDTH      = 1.5f;
    private static final float  PATH_LINE_WIDTH = 2.0f;
    private static final double RENDER_Y_OFFSET = 0.9; // waist height above feet

    private static final float[] COLOR_START = {0.0f, 1.0f, 1.0f, 0.9f}; // cyan
    private static final float[] COLOR_END   = {1.0f, 0.0f, 1.0f, 0.9f}; // magenta
    private static final float[] COLOR_PATH  = {1.0f, 1.0f, 1.0f, 0.6f}; // white
    private static final float[] COLOR_NODE  = {0.0f, 1.0f, 0.0f, 0.7f}; // green

    private final MelonFinderMeshRenderer meshRenderer;

    @Nullable
    private volatile Path currentPath;

    public PathRenderer(@NotNull MelonFinderMeshRenderer meshRenderer) {
        this.meshRenderer = meshRenderer;
    }

    public void register(@NotNull IEventBus bus) {
        bus.subscribe(RenderWorldEvent.class, this, this::onRenderWorld);
    }

    public void setPath(@Nullable Path path) {
        this.currentPath = path;
    }

    private void onRenderWorld(@NotNull RenderWorldEvent event) {
        meshRenderer.endFrame();
        Path path = currentPath;
        if (path == null || path.status() != PathStatus.FOUND || path.segments().isEmpty()) return;

        MinecraftRenderHandle handle = new MinecraftRenderHandle(
            event.getProjection(), meshRenderer,
            event.getCameraX(), event.getCameraY(), event.getCameraZ()
        );

        renderNodes(path.segments(), handle);
        renderPathLine(path.segments(), handle);
    }

    private void renderNodes(@NotNull List<Segment> segments, @NotNull RenderHandle handle) {
        int last = segments.size() - 1;
        for (int i = 0; i <= last; i++) {
            Vec3 pos   = segments.get(i).start();
            float[] color = (i == 0) ? COLOR_START : COLOR_NODE;
            drawBox(handle, pos.x() - handle.cameraX(), pos.y() + RENDER_Y_OFFSET - handle.cameraY(), pos.z() - handle.cameraZ(), color);
        }
        Vec3 end = segments.getLast().end();
        drawBox(handle, end.x() - handle.cameraX(), end.y() + 0.9 - handle.cameraY(), end.z() - handle.cameraZ(), COLOR_END);
    }

    private void renderPathLine(@NotNull List<Segment> segments, @NotNull RenderHandle handle) {
        handle.beginLines(COLOR_PATH[0], COLOR_PATH[1], COLOR_PATH[2], COLOR_PATH[3]);
        for (Segment seg : segments) {
            Vec3 a = seg.start();
            Vec3 b = seg.end();
            double ax = a.x() - handle.cameraX();
            double ay = a.y() + RENDER_Y_OFFSET - handle.cameraY();
            double az = a.z() - handle.cameraZ();
            double bx = b.x() - handle.cameraX();
            double by = b.y() + RENDER_Y_OFFSET - handle.cameraY();
            double bz = b.z() - handle.cameraZ();
            handle.emitLine(ax, ay, az, bx, by, bz, PATH_LINE_WIDTH);
        }
        handle.end(false);
    }

    private void drawBox(@NotNull RenderHandle handle, double cx, double cy, double cz, float @NotNull[] color) {
        float half = NODE_BOX_SIZE / 2.0f;
        double x1 = cx - half;
        double y1 = cy - half;
        double z1 = cz - half;
        double x2 = cx + half;
        double y2 = cy + half;
        double z2 = cz + half;

        handle.beginLines(color[0], color[1], color[2], color[3]);
        // Bottom face
        handle.emitLine(x1, y1, z1, x2, y1, z1, LINE_WIDTH);
        handle.emitLine(x2, y1, z1, x2, y1, z2, LINE_WIDTH);
        handle.emitLine(x2, y1, z2, x1, y1, z2, LINE_WIDTH);
        handle.emitLine(x1, y1, z2, x1, y1, z1, LINE_WIDTH);
        // Top face
        handle.emitLine(x1, y2, z1, x2, y2, z1, LINE_WIDTH);
        handle.emitLine(x2, y2, z1, x2, y2, z2, LINE_WIDTH);
        handle.emitLine(x2, y2, z2, x1, y2, z2, LINE_WIDTH);
        handle.emitLine(x1, y2, z2, x1, y2, z1, LINE_WIDTH);
        // Vertical edges
        handle.emitLine(x1, y1, z1, x1, y2, z1, LINE_WIDTH);
        handle.emitLine(x2, y1, z1, x2, y2, z1, LINE_WIDTH);
        handle.emitLine(x2, y1, z2, x2, y2, z2, LINE_WIDTH);
        handle.emitLine(x1, y1, z2, x1, y2, z2, LINE_WIDTH);
        handle.end(false);
    }
}
