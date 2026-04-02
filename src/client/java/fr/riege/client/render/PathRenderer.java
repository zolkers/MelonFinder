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

public final class PathRenderer {

    private static final float NODE_BOX_SIZE = 0.25f;
    private static final float LINE_WIDTH = 1.5f;

    private static final float[] COLOR_WALK   = {0.0f, 1.0f, 0.0f, 0.7f};
    private static final float[] COLOR_JUMP   = {1.0f, 0.8f, 0.0f, 0.7f};
    private static final float[] COLOR_FALL   = {1.0f, 0.2f, 0.2f, 0.7f};
    private static final float[] COLOR_OTHER  = {0.5f, 0.5f, 1.0f, 0.7f};

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
        Path path = currentPath;
        if (path == null || path.status() != PathStatus.FOUND || path.segments().isEmpty()) return;

        meshRenderer.endFrame();

        MinecraftRenderHandle handle = new MinecraftRenderHandle(
            event.getProjection(), meshRenderer,
            event.getCameraX(), event.getCameraY(), event.getCameraZ()
        );

        for (Segment segment : path.segments()) {
            renderSegment(segment, handle);
        }
    }

    private void renderSegment(@NotNull Segment segment, @NotNull RenderHandle handle) {
        for (Node node : segment.nodes()) {
            float[] color = resolveColor(node);
            double cx = node.pos().x() + 0.5 - handle.cameraX();
            double cy = node.pos().y() + 0.5 - handle.cameraY();
            double cz = node.pos().z() + 0.5 - handle.cameraZ();
            drawBox(handle, cx, cy, cz, color);
        }
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
