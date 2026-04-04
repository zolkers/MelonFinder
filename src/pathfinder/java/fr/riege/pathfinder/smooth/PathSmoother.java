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

public final class PathSmoother {

    private static final int LOS_STEPS = 20;
    private static final double ENTITY_HEIGHT = 1.8;

    private final ICollisionLayer collisionLayer;
    private final IWorldLayer worldLayer;
    private final double hitboxHalf;

    public PathSmoother(@NotNull ICollisionLayer collisionLayer,
                        @NotNull IWorldLayer worldLayer,
                        double hitboxHalf) {
        this.collisionLayer = collisionLayer;
        this.worldLayer = worldLayer;
        this.hitboxHalf = hitboxHalf;
    }

    public @NotNull List<BlockPos> smooth(@NotNull List<BlockPos> path) {
        if (path.size() <= 2) {
            return new ArrayList<>(path);
        }
        List<BlockPos> result = new ArrayList<>();
        result.add(path.getFirst());
        int anchor = 0;
        int i = 2;
        while (i < path.size()) {
            if (!hasLos(path.get(anchor), path.get(i))) {
                result.add(path.get(i - 1));
                anchor = i - 1;
                i = anchor + 1;
            }
            i++;
        }
        result.add(path.getLast());
        return result;
    }

    private boolean hasLos(@NotNull BlockPos from, @NotNull BlockPos to) {
        double fromX = from.x() + 0.5;
        double fromY = from.y();
        double fromZ = from.z() + 0.5;
        double toX = to.x() + 0.5;
        double toY = to.y();
        double toZ = to.z() + 0.5;

        for (int step = 0; step <= LOS_STEPS; step++) {
            double t = (double) step / LOS_STEPS;
            double cx = fromX + (toX - fromX) * t;
            double cy = fromY + (toY - fromY) * t;
            double cz = fromZ + (toZ - fromZ) * t;

            Vec3 minVec = new Vec3(cx - hitboxHalf, cy, cz - hitboxHalf);
            Vec3 maxVec = new Vec3(cx + hitboxHalf, cy + ENTITY_HEIGHT, cz + hitboxHalf);
            if (collisionLayer.hasCollisionAt(new AABB(minVec, maxVec))) {
                return false;
            }
            if (!hasGroundSupport(cx, cy, cz)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasGroundSupport(double cx, double cy, double cz) {
        int bx = (int) Math.floor(cx);
        int by = (int) Math.floor(cy);
        int bz = (int) Math.floor(cz);
        BlockPos current = new BlockPos(bx, by, bz);
        if (worldLayer.getFluidType(current) != FluidType.NONE) {
            return true;
        }
        BlockPos below = new BlockPos(bx, by - 1, bz);
        return worldLayer.isSolid(below);
    }
}
