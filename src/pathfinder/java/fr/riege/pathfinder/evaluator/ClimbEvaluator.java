package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class ClimbEvaluator implements IMovementEvaluator {

    private static final double BASE_COST = 2.5;

    private final IBlockPhysicsLayer blockPhysicsLayer;

    public ClimbEvaluator(@NotNull IBlockPhysicsLayer blockPhysicsLayer) {
        this.blockPhysicsLayer = blockPhysicsLayer;
    }

    @Override
    public @NotNull MovementResult evaluate(@NotNull BlockPos from, @NotNull BlockPos to) {
        int dx = to.x() - from.x();
        int dy = to.y() - from.y();
        int dz = to.z() - from.z();
        boolean isVertical = dx == 0 && dz == 0 && (dy == 1 || dy == -1);
        if (!isVertical) return MovementResult.impossible();
        if (blockPhysicsLayer.isPassable(to)) return MovementResult.impossible();
        return MovementResult.possible(BASE_COST);
    }
}
