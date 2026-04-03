package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class WalkEvaluator implements IMovementEvaluator {

    private static final double BASE_COST = 1.0;

    private final IWorldLayer worldLayer;
    private final IBlockPhysicsLayer blockPhysicsLayer;

    public WalkEvaluator(
            @NotNull IWorldLayer worldLayer,
            @NotNull IBlockPhysicsLayer blockPhysicsLayer) {
        this.worldLayer = worldLayer;
        this.blockPhysicsLayer = blockPhysicsLayer;
    }

    @Override
    public @NotNull MovementResult evaluate(@NotNull BlockPos from, @NotNull BlockPos to) {
        if (!worldLayer.isWalkable(to)) return MovementResult.impossible();
        if (!diagonalClear(from, to)) return MovementResult.impossible();
        double cost = computeCost(to);
        return MovementResult.possible(cost);
    }

    private boolean diagonalClear(@NotNull BlockPos from, @NotNull BlockPos to) {
        int dx = to.x() - from.x();
        int dz = to.z() - from.z();
        if (dx == 0 || dz == 0) return true;
        BlockPos cornerA = new BlockPos(from.x() + dx, from.y(), from.z());
        BlockPos cornerB = new BlockPos(from.x(), from.y(), from.z() + dz);
        return worldLayer.isPassable(cornerA) && worldLayer.isPassable(cornerA.offset(0, 1, 0))
            && worldLayer.isPassable(cornerB) && worldLayer.isPassable(cornerB.offset(0, 1, 0));
    }

    private double computeCost(@NotNull BlockPos pos) {
        float speed = blockPhysicsLayer.getSpeedMultiplier(pos);
        float drag = blockPhysicsLayer.getDragFactor(pos);
        float damage = blockPhysicsLayer.getBlockDamage(pos);
        return (BASE_COST / speed) * drag + damage;
    }
}
