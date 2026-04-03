package fr.riege.pathfinder.smooth;

import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CatmullRomSmoother {

    private static final int    SAMPLES_PER_GAP = 8;
    private static final double ENTITY_HEIGHT   = 1.8;

    private final ICollisionLayer collisionLayer;
    private final double hitboxHalf;

    public CatmullRomSmoother(@NotNull ICollisionLayer collisionLayer, double hitboxHalf) {
        this.collisionLayer = collisionLayer;
        this.hitboxHalf = hitboxHalf;
    }

    public @NotNull List<Vec3> smooth(@NotNull List<Vec3> points) {
        if (points.size() < 2) return new ArrayList<>(points);
        List<Vec3> result = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            sampleGap(points, i, result);
        }
        return result;
    }

    private void sampleGap(@NotNull List<Vec3> pts, int i, @NotNull List<Vec3> out) {
        Vec3 p0 = pts.get(Math.max(0, i - 1));
        Vec3 p1 = pts.get(i);
        Vec3 p2 = pts.get(i + 1);
        Vec3 p3 = pts.get(Math.min(pts.size() - 1, i + 2));
        for (int k = 1; k <= SAMPLES_PER_GAP; k++) {
            double t = (double) k / SAMPLES_PER_GAP;
            double x = catmullRom(p0.x(), p1.x(), p2.x(), p3.x(), t);
            double z = catmullRom(p0.z(), p1.z(), p2.z(), p3.z(), t);
            double y = p1.y() + t * (p2.y() - p1.y());
            Vec3 candidate = new Vec3(x, y, z);
            out.add(hasCollision(candidate) ? nearer(candidate, p1, p2) : candidate);
        }
    }

    private static double catmullRom(double v0, double v1, double v2, double v3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return 0.5 * ((2 * v1)
            + (-v0 + v2) * t
            + (2 * v0 - 5 * v1 + 4 * v2 - v3) * t2
            + (-v0 + 3 * v1 - 3 * v2 + v3) * t3);
    }

    private static @NotNull Vec3 nearer(@NotNull Vec3 candidate, @NotNull Vec3 p1, @NotNull Vec3 p2) {
        return candidate.distanceTo(p1) <= candidate.distanceTo(p2) ? p1 : p2;
    }

    private boolean hasCollision(@NotNull Vec3 pos) {
        Vec3 min = new Vec3(pos.x() - hitboxHalf, pos.y(), pos.z() - hitboxHalf);
        Vec3 max = new Vec3(pos.x() + hitboxHalf, pos.y() + ENTITY_HEIGHT, pos.z() + hitboxHalf);
        return collisionLayer.hasCollisionAt(new AABB(min, max));
    }
}
