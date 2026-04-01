package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public interface IGoal {

    boolean isReached(@NotNull BlockPos current);

    @NotNull BlockPos getTargetForHeuristic();

    default double heuristicCost(@NotNull BlockPos from) {
        return from.distanceTo(getTargetForHeuristic());
    }
}
