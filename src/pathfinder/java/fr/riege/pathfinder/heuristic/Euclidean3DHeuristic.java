package fr.riege.pathfinder.heuristic;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class Euclidean3DHeuristic implements IHeuristic {

    @Override
    public double estimate(@NotNull BlockPos from, @NotNull BlockPos goal) {
        return from.distanceTo(goal);
    }
}
