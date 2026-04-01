package fr.riege.pathfinder.heuristic;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface IHeuristic {
    double estimate(@NotNull BlockPos from, @NotNull BlockPos goal);
}
