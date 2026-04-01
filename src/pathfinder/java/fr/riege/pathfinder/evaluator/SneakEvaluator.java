package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class SneakEvaluator implements IMovementEvaluator {

    private final WalkEvaluator walkEvaluator;
    private final IEntityPhysicsLayer entityPhysicsLayer;

    public SneakEvaluator(
            @NotNull IWorldLayer worldLayer,
            @NotNull IBlockPhysicsLayer blockPhysicsLayer,
            @NotNull IEntityPhysicsLayer entityPhysicsLayer) {
        this.walkEvaluator = new WalkEvaluator(worldLayer, blockPhysicsLayer, entityPhysicsLayer);
        this.entityPhysicsLayer = entityPhysicsLayer;
    }

    @Override
    public @NotNull MovementResult evaluate(@NotNull BlockPos from, @NotNull BlockPos to) {
        MovementResult walkResult = walkEvaluator.evaluate(from, to);
        if (!walkResult.isPossible()) return MovementResult.impossible();
        double sneakCost = walkResult.getCost() * (1.0 / entityPhysicsLayer.getSneakSpeedMultiplier());
        return MovementResult.possible(sneakCost);
    }
}
