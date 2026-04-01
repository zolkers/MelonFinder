package fr.riege.pathfinder.smooth;

import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public final class SubBlockSampler {

    private static final double MAX_VARIANCE = 0.1;
    private static final double DIRECTION_BIAS = 0.1;
    private static final double ENTITY_HEIGHT = 1.8;

    private final ICollisionLayer collisionLayer;
    private final double hitboxHalf;
    private final Random random;

    public SubBlockSampler(@NotNull ICollisionLayer collisionLayer, double hitboxHalf, long seed) {
        this.collisionLayer = collisionLayer;
        this.hitboxHalf = hitboxHalf;
        this.random = new Random(seed);
    }

    public @NotNull Vec3 sample(@NotNull BlockPos pos, @NotNull BlockPos nextPos) {
        double cx = pos.x() + 0.5;
        double cy = pos.y();
        double cz = pos.z() + 0.5;

        double biasX = (nextPos.x() - pos.x()) * DIRECTION_BIAS;
        double biasZ = (nextPos.z() - pos.z()) * DIRECTION_BIAS;

        double noiseX = (random.nextDouble() * 2.0 - 1.0) * MAX_VARIANCE;
        double noiseZ = (random.nextDouble() * 2.0 - 1.0) * MAX_VARIANCE;

        double rx = cx + biasX + noiseX;
        double ry = cy;
        double rz = cz + biasZ + noiseZ;

        Vec3 minVec = new Vec3(rx - hitboxHalf, ry, rz - hitboxHalf);
        Vec3 maxVec = new Vec3(rx + hitboxHalf, ry + ENTITY_HEIGHT, rz + hitboxHalf);
        AABB box = new AABB(minVec, maxVec);

        if (collisionLayer.hasCollisionAt(box)) {
            return new Vec3(cx, cy, cz);
        }
        return new Vec3(rx, ry, rz);
    }
}
