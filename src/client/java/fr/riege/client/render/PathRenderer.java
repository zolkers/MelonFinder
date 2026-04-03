package fr.riege.client.render;

import fr.riege.api.event.IEventBus;
import fr.riege.api.path.Node;
import fr.riege.api.path.Path;
import fr.riege.api.path.PathStatus;
import fr.riege.api.path.Segment;
import fr.riege.api.registry.MovementKeys;
import fr.riege.api.registry.RegistryKey;
import fr.riege.client.event.events.RenderWorldEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PathRenderer {

    private static final float NODE_BOX_SIZE  = 0.25f;
    private static final float LINE_WIDTH      = 1.5f;
    private static final float PATH_LINE_WIDTH = 2.0f;

    private static final float[] COLOR_WALK  = {0.0f, 1.0f, 0.0f, 0.7f};
    private static final float[] COLOR_JUMP  = {1.0f, 0.8f, 0.0f, 0.7f};
    private static final float[] COLOR_FALL  = {1.0f, 0.2f, 0.2f, 0.7f};
    private static final float[] COLOR_OTHER = {0.5f, 0.5f, 1.0f, 0.7f};
    private static final float[] COLOR_START = {0.0f, 1.0f, 1.0f, 0.9f}; // cyan
    private static final float[] COLOR_END   = {1.0f, 0.0f, 1.0f, 0.9f}; // magenta
    private static final float[] COLOR_PATH  = {1.0f, 1.0f, 1.0f, 0.6f}; // white

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

        List<Node> allNodes = collectNodes(path);
        if (allNodes.isEmpty()) return;

        renderNodes(allNodes, handle);
        renderPathLine(allNodes, handle);
    }

    private @NotNull List<Node> collectNodes(@NotNull Path path) {
        List<Node> result = new ArrayList<>();
        for (Segment segment : path.segments()) {
            result.addAll(segment.nodes());
        }
        return result;
    }

    private void renderNodes(@NotNull List<Node> nodes, @NotNull RenderHandle handle) {
        int last = nodes.size() - 1;
        for (int i = 0; i <= last; i++) {
            Node node = nodes.get(i);
            float[] color = resolveNodeColor(node, i == 0, i == last);
            double cx = node.pos().x() + 0.5 - handle.cameraX();
            double cy = node.pos().y() + 0.5 - handle.cameraY();
            double cz = node.pos().z() + 0.5 - handle.cameraZ();
            drawBox(handle, cx, cy, cz, color);
        }
    }

    private float @NotNull[] resolveNodeColor(@NotNull Node node, boolean isStart, boolean isEnd) {
        if (isStart) return COLOR_START;
        if (isEnd) return COLOR_END;
        return resolveColor(node);
    }

    private void renderPathLine(@NotNull List<Node> nodes, @NotNull RenderHandle handle) {
        if (nodes.size() < 2) return;
        handle.beginLines(COLOR_PATH[0], COLOR_PATH[1], COLOR_PATH[2], COLOR_PATH[3]);
        for (int i = 0; i < nodes.size() - 1; i++) {
            Node a = nodes.get(i);
            Node b = nodes.get(i + 1);
            double ax = a.pos().x() + 0.5 - handle.cameraX();
            double ay = a.pos().y() + 0.5 - handle.cameraY();
            double az = a.pos().z() + 0.5 - handle.cameraZ();
            double bx = b.pos().x() + 0.5 - handle.cameraX();
            double by = b.pos().y() + 0.5 - handle.cameraY();
            double bz = b.pos().z() + 0.5 - handle.cameraZ();
            handle.emitLine(ax, ay, az, bx, by, bz, PATH_LINE_WIDTH);
        }
        handle.end(false);
    }

    private float @NotNull[] resolveColor(@NotNull Node node) {
        RegistryKey key = node.movementType().key();
        if (MovementKeys.JUMP.equals(key)) return COLOR_JUMP;
        if (MovementKeys.FALL.equals(key)) return COLOR_FALL;
        if (MovementKeys.WALK.equals(key)) return COLOR_WALK;
        return COLOR_OTHER;
    }

    private void drawBox(@NotNull RenderHandle handle, double cx, double cy, double cz, float @NotNull[] color) {
        float half = PathRenderer.NODE_BOX_SIZE / 2.0f;
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
