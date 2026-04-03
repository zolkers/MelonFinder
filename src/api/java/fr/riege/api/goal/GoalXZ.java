package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class GoalXZ implements IGoal {

    private final int x;
    private final int z;
    private final BlockPos heuristicTarget;

    public GoalXZ(int x, int y, int z) {
        this.x = x;
        this.z = z;
        this.heuristicTarget = new BlockPos(x, y, z);
    }

    @Override
    public boolean isReached(@NotNull BlockPos current) {
        return current.x() == x && current.z() == z;
    }

    @Override
    public @NotNull BlockPos getTargetForHeuristic() {
        return heuristicTarget;
    }
}
