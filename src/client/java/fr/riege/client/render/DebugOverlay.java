package fr.riege.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.riege.api.event.IEventBus;
import fr.riege.api.math.BlockPos;
import fr.riege.api.path.Path;
import fr.riege.api.path.PathStatus;
import fr.riege.client.event.events.RenderHudEvent;
import fr.riege.client.event.events.RenderWorldEvent;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Collections;
import java.util.Map;

public final class DebugOverlay {

    private static final int TEXT_X = 4;
    private static final int TEXT_Y = 4;
    private static final int COLOR_FOUND   = 0xFF00FF00;
    private static final int COLOR_IDLE    = 0xFFAAAAAA;
    private static final int COLOR_ERROR   = 0xFFFF4444;
    private static final int COLOR_COST    = 0xFFFFFF00;
    private static final String PATH_TAG = "Path: ";
    private static final double MAX_COST_DIST_SQ = 48.0 * 48.0;

    @Nullable
    private Path currentPath;
    @Nullable
    private volatile String statusOverride;
    private volatile Map<BlockPos, Double> exploredCosts = Collections.emptyMap();

    @Nullable
    private Matrix4f lastMvp;
    private double lastCamX;
    private double lastCamY;
    private double lastCamZ;

    public void register(@NotNull IEventBus bus) {
        bus.subscribe(RenderHudEvent.class, this, this::onRenderHud);
        bus.subscribe(RenderWorldEvent.class, this, this::onRenderWorld);
    }

    public void setPath(@Nullable Path path) {
        this.currentPath = path;
        this.statusOverride = null;
    }

    public void setStatus(@NotNull String text) {
        this.statusOverride = text;
    }

    public void setExploredCosts(@NotNull Map<BlockPos, Double> costs) {
        this.exploredCosts = costs;
    }

    private void onRenderWorld(@NotNull RenderWorldEvent event) {
        lastMvp = new Matrix4f(event.getProjection()).mul(RenderSystem.getModelViewMatrix());
        lastCamX = event.getCameraX();
        lastCamY = event.getCameraY();
        lastCamZ = event.getCameraZ();
    }

    private void onRenderHud(@NotNull RenderHudEvent event) {
        String line = buildLine();
        if (line != null) {
            event.getGraphics().drawString(Minecraft.getInstance().font, line, TEXT_X, TEXT_Y, resolveColor(), false);
        }
        renderExploredCosts(event);
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

    private void renderExploredCosts(@NotNull RenderHudEvent event) {
        Map<BlockPos, Double> costs = exploredCosts;
        Matrix4f mvp = lastMvp;
        if (costs.isEmpty() || mvp == null) return;

        Minecraft mc = Minecraft.getInstance();
        int guiW = mc.getWindow().getGuiScaledWidth();
        int guiH = mc.getWindow().getGuiScaledHeight();

        for (Map.Entry<BlockPos, Double> entry : costs.entrySet()) {
            int[] screen = projectToScreen(entry.getKey(), mvp, guiW, guiH);
            if (screen == null) continue;
            event.getGraphics().drawString(mc.font, formatCost(entry.getValue()), screen[0], screen[1], COLOR_COST, false);
        }
    }

    @Nullable
    private int[] projectToScreen(@NotNull BlockPos pos, @NotNull Matrix4f mvp, int guiW, int guiH) {
        double dx = pos.x() + 0.5 - lastCamX;
        double dy = pos.y() + 0.5 - lastCamY;
        double dz = pos.z() + 0.5 - lastCamZ;
        if (dx * dx + dy * dy + dz * dz > MAX_COST_DIST_SQ) return null;

        Vector4f clip = mvp.transform(new Vector4f((float) dx, (float) dy, (float) dz, 1f));
        float ndcX = clip.x / clip.w;
        float ndcY = clip.y / clip.w;
        if (clip.w <= 0f || ndcX < -1f || ndcX > 1f || ndcY < -1f || ndcY > 1f) return null;

        return new int[]{(int) ((ndcX + 1f) * 0.5f * guiW), (int) ((1f - ndcY) * 0.5f * guiH)};
    }

    private @NotNull String formatCost(double cost) {
        if (cost >= 99999.0) return "100000";
        return String.valueOf((int) Math.round(cost));
    }
}
