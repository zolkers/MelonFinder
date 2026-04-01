package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class ParkourEvaluator implements IMovementEvaluator {

    private static final double BASE_COST = 3.0;
    private static final double GAP_COST_FACTOR = 0.5;
    private static final int REQUIRED_GAP = 2;

    private final IWorldLayer worldLayer;

    public ParkourEvaluator(@NotNull IWorldLayer worldLayer) {
        this.worldLayer = worldLayer;
    }

    @Override
    public @NotNull MovementResult evaluate(@NotNull BlockPos from, @NotNull BlockPos to) {
        int dx = Math.abs(to.getX() - from.getX());
        int dy = to.getY() - from.getY();
        int dz = Math.abs(to.getZ() - from.getZ());
        boolean isHorizontalGap = dy == 0 && ((dx == REQUIRED_GAP && dz == 0) || (dz == REQUIRED_GAP && dx == 0));
        if (!isHorizontalGap) return MovementResult.impossible();
        BlockPos intermediate = midpoint(from, to);
        if (worldLayer.isWalkable(intermediate)) return MovementResult.impossible();
        if (!worldLayer.isWalkable(to)) return MovementResult.impossible();
        return MovementResult.possible(BASE_COST + REQUIRED_GAP * GAP_COST_FACTOR);
    }

    private @NotNull BlockPos midpoint(@NotNull BlockPos from, @NotNull BlockPos to) {
        int midX = (from.getX() + to.getX()) / 2;
        int midZ = (from.getZ() + to.getZ()) / 2;
        return new BlockPos(midX, from.getY(), midZ);
    }
}
