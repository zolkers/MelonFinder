package fr.riege.pathfinder.smooth;

import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class GradientDescentSmoother {

    private static final int    ITERATIONS   = 10;
    private static final double ALPHA        = 0.25;
    private static final double BETA         = 0.15;
    private static final double PROBE_RADIUS = 0.6;
    private static final double ENTITY_HEIGHT = 1.8;

    private static final double INV_SQRT2 = 1.0 / Math.sqrt(2.0);

    private static final double[][] PROBE_DIRS = {
        { 1,         0        },
        {-1,         0        },
        { 0,         1        },
        { 0,        -1        },
        { INV_SQRT2, INV_SQRT2},
        { INV_SQRT2,-INV_SQRT2},
        {-INV_SQRT2, INV_SQRT2},
        {-INV_SQRT2,-INV_SQRT2}
    };

    private final ICollisionLayer collisionLayer;
    private final double hitboxHalf;

    public GradientDescentSmoother(@NotNull ICollisionLayer collisionLayer, double hitboxHalf) {
        this.collisionLayer = collisionLayer;
        this.hitboxHalf = hitboxHalf;
    }

    public @NotNull List<Vec3> smooth(@NotNull List<Vec3> points) {
        if (points.size() <= 2) return new ArrayList<>(points);
        List<Vec3> current = new ArrayList<>(points);
        for (int iter = 0; iter < ITERATIONS; iter++) {
            current = iterate(current);
        }
        return current;
    }

    private @NotNull List<Vec3> iterate(@NotNull List<Vec3> points) {
        List<Vec3> result = new ArrayList<>(points);
        for (int i = 1; i < points.size() - 1; i++) {
            Vec3 candidate = computeNewPos(points, i);
            if (!hasCollision(candidate)) {
                result.set(i, candidate);
            }
        }
        return result;
    }

    private @NotNull Vec3 computeNewPos(@NotNull List<Vec3> points, int i) {
        Vec3 prev = points.get(i - 1);
        Vec3 p    = points.get(i);
        Vec3 next = points.get(i + 1);
        double midX = (prev.x() + next.x()) * 0.5;
        double midZ = (prev.z() + next.z()) * 0.5;
        double elasticX = ALPHA * (midX - p.x());
        double elasticZ = ALPHA * (midZ - p.z());
        double[] repulsion = computeRepulsion(p);
        return new Vec3(p.x() + elasticX + repulsion[0], p.y(), p.z() + elasticZ + repulsion[1]);
    }

    private double @NotNull[] computeRepulsion(@NotNull Vec3 p) {
        double rx = 0;
        double rz = 0;
        for (double[] dir : PROBE_DIRS) {
            Vec3 probe = new Vec3(p.x() + dir[0] * PROBE_RADIUS, p.y(), p.z() + dir[1] * PROBE_RADIUS);
            if (hasCollision(probe)) {
                rx -= BETA * dir[0];
                rz -= BETA * dir[1];
            }
        }
        return new double[]{rx, rz};
    }

    private boolean hasCollision(@NotNull Vec3 pos) {
        Vec3 min = new Vec3(pos.x() - hitboxHalf, pos.y(), pos.z() - hitboxHalf);
        Vec3 max = new Vec3(pos.x() + hitboxHalf, pos.y() + ENTITY_HEIGHT, pos.z() + hitboxHalf);
        return collisionLayer.hasCollisionAt(new AABB(min, max));
    }
}
