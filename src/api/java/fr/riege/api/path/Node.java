package fr.riege.api.path;

import fr.riege.api.math.BlockPos;

public record Node(BlockPos pos, MovementType movementType, double gCost, double hCost) {

    public double fCost() {
        return gCost + hCost;
    }
}
