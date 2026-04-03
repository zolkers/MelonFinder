package fr.riege.client.render;

import fr.riege.api.event.IEventBus;
import fr.riege.api.math.BlockPos;
import fr.riege.client.event.events.RenderWorldEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public final class ExplorationRenderer {

    private static final float EDGE_R = 0.55f;
    private static final float EDGE_G = 0.55f;
    private static final float EDGE_B = 0.55f;
    private static final float EDGE_A = 0.55f;
    private static final float EDGE_WIDTH = 0.5f;
    private static final double MAX_EDGE_DIST_SQ = 10.0 * 10.0;

    private final MelonFinderMeshRenderer meshRenderer;
    private volatile Map<BlockPos, BlockPos> parentMap = Collections.emptyMap();

    public ExplorationRenderer(@NotNull MelonFinderMeshRenderer meshRenderer) {
        this.meshRenderer = meshRenderer;
    }

    public void register(@NotNull IEventBus bus) {
        bus.subscribe(RenderWorldEvent.class, this, this::onRenderWorld);
    }

    public void setParentMap(@NotNull Map<BlockPos, BlockPos> parents) {
        this.parentMap = parents;
    }

    private void onRenderWorld(@NotNull RenderWorldEvent event) {
        Map<BlockPos, BlockPos> parents = parentMap;
        if (parents.isEmpty()) return;

        double camX = event.getCameraX();
        double camY = event.getCameraY();
        double camZ = event.getCameraZ();

        MinecraftRenderHandle handle = new MinecraftRenderHandle(
            event.getProjection(), meshRenderer, camX, camY, camZ);
        handle.beginLines(EDGE_R, EDGE_G, EDGE_B, EDGE_A);

        for (Map.Entry<BlockPos, BlockPos> entry : parents.entrySet()) {
            BlockPos child = entry.getKey();
            double toX = child.x() + 0.5 - camX;
            double toY = child.y() + 0.5 - camY;
            double toZ = child.z() + 0.5 - camZ;
            if (toX * toX + toY * toY + toZ * toZ > MAX_EDGE_DIST_SQ) continue;

            BlockPos parent = entry.getValue();
            double fromX = parent.x() + 0.5 - camX;
            double fromY = parent.y() + 0.5 - camY;
            double fromZ = parent.z() + 0.5 - camZ;
            handle.emitLine(fromX, fromY, fromZ, toX, toY, toZ, EDGE_WIDTH);
        }

        handle.end(true);
    }
}
