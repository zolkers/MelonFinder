package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class WalkEvaluator implements IMovementEvaluator {

    private static final double BASE_COST = 1.0;

    private final IWorldLayer worldLayer;
    private final IBlockPhysicsLayer blockPhysicsLayer;
    private final IEntityPhysicsLayer entityPhysicsLayer;

    public WalkEvaluator(
            @NotNull IWorldLayer worldLayer,
            @NotNull IBlockPhysicsLayer blockPhysicsLayer,
            @NotNull IEntityPhysicsLayer entityPhysicsLayer) {
        this.worldLayer = worldLayer;
        this.blockPhysicsLayer = blockPhysicsLayer;
        this.entityPhysicsLayer = entityPhysicsLayer;
    }

    @Override
    public @NotNull MovementResult evaluate(@NotNull BlockPos from, @NotNull BlockPos to) {
        if (!worldLayer.isWalkable(to)) return MovementResult.impossible();
        if (!fitsAtDestination(to)) return MovementResult.impossible();
        double cost = computeCost(to);
        return MovementResult.possible(cost);
    }

    private boolean fitsAtDestination(@NotNull BlockPos pos) {
        int headY = (int) Math.ceil(pos.getY() + entityPhysicsLayer.getHitboxHeight());
        BlockPos head = new BlockPos(pos.getX(), headY, pos.getZ());
        return !worldLayer.isSolid(head);
    }

    private double computeCost(@NotNull BlockPos pos) {
        float speed = blockPhysicsLayer.getSpeedMultiplier(pos);
        float drag = blockPhysicsLayer.getDragFactor(pos);
        float damage = blockPhysicsLayer.getBlockDamage(pos);
        return (BASE_COST / speed) * drag + damage;
    }
}
