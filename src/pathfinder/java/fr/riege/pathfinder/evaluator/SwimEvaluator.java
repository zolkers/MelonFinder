package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import org.jetbrains.annotations.NotNull;

public final class SwimEvaluator implements IMovementEvaluator {

    private static final double BASE_COST = 2.0;

    private final IWorldLayer worldLayer;
    private final IBlockPhysicsLayer blockPhysicsLayer;
    private final IEntityPhysicsLayer entityPhysicsLayer;

    public SwimEvaluator(
            @NotNull IWorldLayer worldLayer,
            @NotNull IBlockPhysicsLayer blockPhysicsLayer,
            @NotNull IEntityPhysicsLayer entityPhysicsLayer) {
        this.worldLayer = worldLayer;
        this.blockPhysicsLayer = blockPhysicsLayer;
        this.entityPhysicsLayer = entityPhysicsLayer;
    }

    @Override
    public @NotNull MovementResult evaluate(@NotNull BlockPos from, @NotNull BlockPos to) {
        if (!worldLayer.isPassable(to)) return MovementResult.impossible();
        if (worldLayer.getFluidType(to) == FluidType.NONE) return MovementResult.impossible();
        float drag = blockPhysicsLayer.getDragFactor(to);
        double swimSpeed = entityPhysicsLayer.getSwimSpeed();
        return MovementResult.possible((BASE_COST / swimSpeed) * drag);
    }
}
