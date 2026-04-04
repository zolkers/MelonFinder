package fr.riege.pathfinder.smooth;

import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import fr.riege.api.math.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CatmullRomSmoother {

    public static final int    SAMPLES_PER_GAP = 32;
    private static final double ENTITY_HEIGHT   = 1.8;

    private final ICollisionLayer collisionLayer;
    private final IWorldLayer worldLayer;
    private final double hitboxHalf;

    public CatmullRomSmoother(@NotNull ICollisionLayer collisionLayer,
                               @NotNull IWorldLayer worldLayer,
                               double hitboxHalf) {
        this.collisionLayer = collisionLayer;
        this.worldLayer = worldLayer;
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
            out.add(isValid(candidate) ? candidate : linearFallback(p1, p2, t));
        }
    }

    private @NotNull Vec3 linearFallback(@NotNull Vec3 p1, @NotNull Vec3 p2, double t) {
        Vec3 linear = new Vec3(
            p1.x() + t * (p2.x() - p1.x()),
            p1.y() + t * (p2.y() - p1.y()),
            p1.z() + t * (p2.z() - p1.z())
        );
        if (isValid(linear)) return linear;
        return t <= 0.5 ? p1 : p2;
    }

    private static double catmullRom(double v0, double v1, double v2, double v3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return 0.5 * ((2 * v1)
            + (-v0 + v2) * t
            + (2 * v0 - 5 * v1 + 4 * v2 - v3) * t2
            + (-v0 + 3 * v1 - 3 * v2 + v3) * t3);
    }

    private boolean isValid(@NotNull Vec3 pos) {
        if (hasCollision(pos)) return false;
        return hasGroundSupport(pos);
    }

    private boolean hasGroundSupport(@NotNull Vec3 pos) {
        int bx = (int) Math.floor(pos.x());
        int by = (int) Math.floor(pos.y());
        int bz = (int) Math.floor(pos.z());
        BlockPos current = new BlockPos(bx, by, bz);
        if (worldLayer.getFluidType(current) != FluidType.NONE) return true;
        return worldLayer.isSolid(new BlockPos(bx, by - 1, bz));
    }

    private boolean hasCollision(@NotNull Vec3 pos) {
        Vec3 min = new Vec3(pos.x() - hitboxHalf, pos.y(), pos.z() - hitboxHalf);
        Vec3 max = new Vec3(pos.x() + hitboxHalf, pos.y() + ENTITY_HEIGHT, pos.z() + hitboxHalf);
        return collisionLayer.hasCollisionAt(new AABB(min, max));
    }
}
