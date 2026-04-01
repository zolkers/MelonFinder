package fr.riege.api.path;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class Node {

    private final BlockPos pos;
    private final MovementType movementType;
    private final double gCost;
    private final double hCost;

    public Node(@NotNull BlockPos pos, @NotNull MovementType movementType, double gCost, double hCost) {
        this.pos = pos;
        this.movementType = movementType;
        this.gCost = gCost;
        this.hCost = hCost;
    }

    @NotNull
    public BlockPos getPos() {
        return pos;
    }

    @NotNull
    public MovementType getMovementType() {
        return movementType;
    }

    public double getGCost() {
        return gCost;
    }

    public double getHCost() {
        return hCost;
    }

    public double getFCost() {
        return gCost + hCost;
    }
}
