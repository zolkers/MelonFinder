package fr.riege.pathfinder.goal;

import fr.riege.api.goal.IGoal;
import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public record BlockPosGoal(BlockPos target) implements IGoal {

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

}
