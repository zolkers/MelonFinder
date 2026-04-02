package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class BlockGoal implements IGoal {

    private final BlockPos target;

    public BlockGoal(@NotNull BlockPos target) {
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
