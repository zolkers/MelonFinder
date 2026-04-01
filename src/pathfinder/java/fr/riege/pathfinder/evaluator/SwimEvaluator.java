package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import org.jetbrains.annotations.NotNull;

public final class SwimEvaluator implements IMovementEvaluator {

    private static final double BASE_COST = 2.0;

    private final IWorldLayer worldLayer;
    private final IBlockPhysicsLayer blockPhysicsLayer;

    public SwimEvaluator(
            @NotNull IWorldLayer worldLayer,
            @NotNull IBlockPhysicsLayer blockPhysicsLayer) {
        this.worldLayer = worldLayer;
        this.blockPhysicsLayer = blockPhysicsLayer;
    }

    @Override
    public @NotNull MovementResult evaluate(@NotNull BlockPos from, @NotNull BlockPos to) {
        if (worldLayer.getFluidType(to) == FluidType.NONE) return MovementResult.impossible();
        float drag = blockPhysicsLayer.getDragFactor(to);
        return MovementResult.possible(BASE_COST * drag);
    }
}
