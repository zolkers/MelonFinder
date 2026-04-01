package fr.riege.pathfinder.smooth;

import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class PathSmoother {

    private static final int LOS_STEPS = 20;
    private static final double ENTITY_HEIGHT = 1.8;

    private final ICollisionLayer collisionLayer;
    private final double hitboxHalf;

    public PathSmoother(@NotNull ICollisionLayer collisionLayer, double hitboxHalf) {
        this.collisionLayer = collisionLayer;
        this.hitboxHalf = hitboxHalf;
    }

    public @NotNull List<BlockPos> smooth(@NotNull List<BlockPos> path) {
        if (path.size() <= 2) {
            return new ArrayList<>(path);
        }
        List<BlockPos> result = new ArrayList<>();
        result.add(path.get(0));
        int anchor = 0;
        int i = 2;
        while (i < path.size()) {
            if (!hasLos(path.get(anchor), path.get(i))) {
                result.add(path.get(i - 1));
                anchor = i - 1;
            }
            i++;
        }
        result.add(path.get(path.size() - 1));
        return result;
    }

    private boolean hasLos(@NotNull BlockPos from, @NotNull BlockPos to) {
        double fromX = from.getX() + 0.5;
        double fromY = from.getY();
        double fromZ = from.getZ() + 0.5;
        double toX = to.getX() + 0.5;
        double toY = to.getY();
        double toZ = to.getZ() + 0.5;

        for (int step = 0; step <= LOS_STEPS; step++) {
            double t = (double) step / LOS_STEPS;
            double cx = fromX + (toX - fromX) * t;
            double cy = fromY + (toY - fromY) * t;
            double cz = fromZ + (toZ - fromZ) * t;
            Vec3 minVec = new Vec3(cx - hitboxHalf, cy, cz - hitboxHalf);
            Vec3 maxVec = new Vec3(cx + hitboxHalf, cy + ENTITY_HEIGHT, cz + hitboxHalf);
            AABB box = new AABB(minVec, maxVec);
            if (collisionLayer.hasCollisionAt(box)) {
                return false;
            }
        }
        return true;
    }
}
