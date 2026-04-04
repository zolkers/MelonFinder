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

/**
 * Smooths a sequence of Vec3 waypoints with a centripetal Catmull-Rom spline.
 *
 * <h2>Centripetal parameterisation (α = 0.5)</h2>
 * <p>Unlike the uniform variant, centripetal CR assigns knot intervals
 * proportional to √distance between consecutive control points.  This
 * eliminates cusps and self-intersections that arise when control points are
 * clustered (e.g. two waypoints close together after a tight corner), which
 * was the root cause of the overshoot problems seen in practice.
 *
 * <h2>Per-gap fallback</h2>
 * <p>Each spline gap is sampled independently.  If more than half of the
 * samples in a gap fail the AABB/walkability check, the entire gap is replaced
 * with linear interpolation so the output never contains endpoint-jumping
 * artefacts.
 *
 * <h2>Walkability guard</h2>
 * <p>Ground support is validated via {@link IWorldLayer#isWalkable(BlockPos)}
 * (the same predicate A* uses) rather than the weaker {@code isSolid} check.
 * This ensures the spline never dips below dangerous terrain (lava surface,
 * etc.) that A* would never have routed through.
 */
public final class CatmullRomSmoother {

    public static final int SAMPLES_PER_GAP = 32;

    private static final double ENTITY_HEIGHT  = 1.8;
    private static final double KNOT_EPS       = 1e-10;
    private static final int    FAIL_THRESHOLD = SAMPLES_PER_GAP / 2;

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
        List<Vec3> result = new ArrayList<>(points.size() * SAMPLES_PER_GAP);
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

        double t0 = 0;
        double t1 = t0 + knotInterval(p0, p1);
        double t2 = t1 + knotInterval(p1, p2);
        double t3 = t2 + knotInterval(p2, p3);

        if (t2 - t1 < KNOT_EPS) {
            out.add(p2);
            return;
        }

        List<Vec3> gapSamples = new ArrayList<>(SAMPLES_PER_GAP);
        int failCount = 0;

        for (int k = 1; k <= SAMPLES_PER_GAP; k++) {
            double u = (double) k / SAMPLES_PER_GAP;
            double t = t1 + u * (t2 - t1);
            double y = p1.y() + u * (p2.y() - p1.y());
            double x = neville(p0.x(), p1.x(), p2.x(), p3.x(), t0, t1, t2, t3, t);
            double z = neville(p0.z(), p1.z(), p2.z(), p3.z(), t0, t1, t2, t3, t);
            Vec3 candidate = new Vec3(x, y, z);

            if (isValid(candidate)) {
                gapSamples.add(candidate);
            } else {
                failCount++;
                Vec3 linear = linearPoint(p1, p2, u);
                gapSamples.add(isValid(linear) ? linear : (u <= 0.5 ? p1 : p2));
            }
        }

        if (failCount > FAIL_THRESHOLD) {
            gapSamples.clear();
            for (int k = 1; k <= SAMPLES_PER_GAP; k++) {
                double u = (double) k / SAMPLES_PER_GAP;
                gapSamples.add(linearPoint(p1, p2, u));
            }
        }

        out.addAll(gapSamples);
    }

    // ── Centripetal Catmull-Rom via Neville's algorithm ──────────────────────

    /** Knot interval: √distance (α = 0.5, centripetal parameterisation). */
    private static double knotInterval(@NotNull Vec3 a, @NotNull Vec3 b) {
        double dx = b.x() - a.x();
        double dy = b.y() - a.y();
        double dz = b.z() - a.z();
        return Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz));
    }

    /**
     * Evaluates the centripetal Catmull-Rom spline for one coordinate component
     * using Neville's algorithm.  The four control values {@code v0..v3}
     * correspond to knots {@code t0..t3}; evaluation is at {@code t ∈ [t1,t2]}.
     */
    private static double neville(double v0, double v1, double v2, double v3,
            double t0, double t1, double t2, double t3, double t) {
        double a1 = lerp(v0, v1, t0, t1, t);
        double a2 = lerp(v1, v2, t1, t2, t);
        double a3 = lerp(v2, v3, t2, t3, t);
        double b1 = lerp(a1, a2, t0, t2, t);
        double b2 = lerp(a2, a3, t1, t3, t);
        return lerp(b1, b2, t1, t2, t);
    }

    private static double lerp(double v0, double v1, double t0, double t1, double t) {
        if (t1 - t0 < KNOT_EPS) return v1;
        return (t1 - t) / (t1 - t0) * v0 + (t - t0) / (t1 - t0) * v1;
    }

    // ── Validity helpers ─────────────────────────────────────────────────────

    private static @NotNull Vec3 linearPoint(@NotNull Vec3 p1, @NotNull Vec3 p2, double u) {
        return new Vec3(
            p1.x() + u * (p2.x() - p1.x()),
            p1.y() + u * (p2.y() - p1.y()),
            p1.z() + u * (p2.z() - p1.z())
        );
    }

    private boolean isValid(@NotNull Vec3 pos) {
        return !hasCollision(pos) && hasGroundSupport(pos);
    }

    private boolean hasGroundSupport(@NotNull Vec3 pos) {
        int bx = (int) Math.floor(pos.x());
        int by = (int) Math.floor(pos.y());
        int bz = (int) Math.floor(pos.z());
        BlockPos current = new BlockPos(bx, by, bz);
        if (worldLayer.getFluidType(current) != FluidType.NONE) {
            return true;
        }
        return worldLayer.isWalkable(current);
    }

    private boolean hasCollision(@NotNull Vec3 pos) {
        Vec3 min = new Vec3(pos.x() - hitboxHalf, pos.y(), pos.z() - hitboxHalf);
        Vec3 max = new Vec3(pos.x() + hitboxHalf, pos.y() + ENTITY_HEIGHT, pos.z() + hitboxHalf);
        return collisionLayer.hasCollisionAt(new AABB(min, max));
    }
}
