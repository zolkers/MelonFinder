package fr.riege.pathfinder.goal;

import fr.riege.api.goal.IGoal;
import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public record RadiusGoal(BlockPos center, double radius) implements IGoal {

    @Override
    public boolean isReached(@NotNull BlockPos current) {
        return current.distanceTo(center) <= radius;
    }

    @Override
    public @NotNull BlockPos getTargetForHeuristic() {
        return center;
    }
}
