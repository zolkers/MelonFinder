package fr.riege.pathfinder.goal;

import fr.riege.api.goal.IGoal;
import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class BlockPosGoal implements IGoal {

    private final BlockPos target;

    public BlockPosGoal(@NotNull BlockPos target) {
        this.target = target;
    }

    @Override
    public boolean isReached(@NotNull BlockPos current) {
        return current.equals(target);
    }

    @Override
    public @NotNull BlockPos getTargetForHeuristic() {
        return target;
    }

    public @NotNull BlockPos getTarget() {
        return target;
    }
}
