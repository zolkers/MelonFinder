package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class FallEvaluator implements IMovementEvaluator {

    private static final double BASE_COST = 0.8;
    private static final float MAX_SURVIVABLE_DAMAGE = 20.0f;

    private final IWorldLayer worldLayer;
    private final IEntityPhysicsLayer entityPhysicsLayer;

    public FallEvaluator(
            @NotNull IWorldLayer worldLayer,
            @NotNull IEntityPhysicsLayer entityPhysicsLayer) {
        this.worldLayer = worldLayer;
        this.entityPhysicsLayer = entityPhysicsLayer;
    }

    @Override
    public @NotNull MovementResult evaluate(@NotNull BlockPos from, @NotNull BlockPos to) {
        if (to.getY() >= from.getY()) return MovementResult.impossible();
        if (!worldLayer.isWalkable(to)) return MovementResult.impossible();
        int drop = from.getY() - to.getY();
        float damage = entityPhysicsLayer.evaluateFallDamage(drop);
        if (damage >= MAX_SURVIVABLE_DAMAGE) return MovementResult.impossible();
        return MovementResult.possible(BASE_COST + damage * 2.0);
    }
}
