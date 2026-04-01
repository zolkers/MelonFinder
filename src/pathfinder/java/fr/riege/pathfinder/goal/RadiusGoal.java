package fr.riege.pathfinder.goal;

import fr.riege.api.goal.IGoal;
import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class RadiusGoal implements IGoal {

    private final BlockPos center;
    private final double radius;

    public RadiusGoal(@NotNull BlockPos center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    @Override
    public boolean isReached(@NotNull BlockPos current) {
        return current.distanceTo(center) <= radius;
    }

    @Override
    public @NotNull BlockPos getTargetForHeuristic() {
        return center;
    }

    public @NotNull BlockPos getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }
}
