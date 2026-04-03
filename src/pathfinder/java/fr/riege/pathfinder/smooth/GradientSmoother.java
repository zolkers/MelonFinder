package fr.riege.pathfinder.smooth;

import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class GradientSmoother {

    private static final int ITERATIONS = 10;
    private static final double STEP = 0.25;
    private static final double ENTITY_HEIGHT = 1.8;

    private final ICollisionLayer collisionLayer;
    private final double hitboxHalf;

    public GradientSmoother(@NotNull ICollisionLayer collisionLayer, double hitboxHalf) {
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
            Vec3 prev = points.get(i - 1);
            Vec3 p    = points.get(i);
            Vec3 next = points.get(i + 1);
            double midX = (prev.x() + next.x()) * 0.5;
            double midZ = (prev.z() + next.z()) * 0.5;
            double newX = p.x() + STEP * (midX - p.x());
            double newZ = p.z() + STEP * (midZ - p.z());
            Vec3 candidate = new Vec3(newX, p.y(), newZ);
            if (!hasCollision(candidate)) {
                result.set(i, candidate);
            }
        }
        return result;
    }

    private boolean hasCollision(@NotNull Vec3 pos) {
        Vec3 min = new Vec3(pos.x() - hitboxHalf, pos.y(), pos.z() - hitboxHalf);
        Vec3 max = new Vec3(pos.x() + hitboxHalf, pos.y() + ENTITY_HEIGHT, pos.z() + hitboxHalf);
        return collisionLayer.hasCollisionAt(new AABB(min, max));
    }
}
