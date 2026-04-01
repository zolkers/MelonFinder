package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class JumpEvaluator implements IMovementEvaluator {

    private static final double BASE_COST = 1.5;
    private static final int MAX_JUMP_HEIGHT = 1;

    private final IWorldLayer worldLayer;
    private final IBlockPhysicsLayer blockPhysicsLayer;
    private final IEntityPhysicsLayer entityPhysicsLayer;
    private final ICollisionLayer collisionLayer;

    public JumpEvaluator(
            @NotNull IWorldLayer worldLayer,
            @NotNull IBlockPhysicsLayer blockPhysicsLayer,
            @NotNull IEntityPhysicsLayer entityPhysicsLayer,
            @NotNull ICollisionLayer collisionLayer) {
        this.worldLayer = worldLayer;
        this.blockPhysicsLayer = blockPhysicsLayer;
        this.entityPhysicsLayer = entityPhysicsLayer;
        this.collisionLayer = collisionLayer;
    }

    @Override
    public @NotNull MovementResult evaluate(@NotNull BlockPos from, @NotNull BlockPos to) {
        int heightDiff = to.getY() - from.getY();
        if (heightDiff != MAX_JUMP_HEIGHT) return MovementResult.impossible();
        if (!worldLayer.isWalkable(to)) return MovementResult.impossible();
        if (!headClearance(from)) return MovementResult.impossible();
        double cost = BASE_COST / blockPhysicsLayer.getSpeedMultiplier(to);
        return MovementResult.possible(cost);
    }

    private boolean headClearance(@NotNull BlockPos from) {
        BlockPos head = new BlockPos(from.getX(), from.getY() + 2, from.getZ());
        return !worldLayer.isSolid(head);
    }
}
