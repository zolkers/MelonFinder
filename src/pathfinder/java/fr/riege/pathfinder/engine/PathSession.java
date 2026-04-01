package fr.riege.pathfinder.engine;

import fr.riege.api.goal.IGoal;
import fr.riege.api.math.BlockPos;
import fr.riege.api.path.PathStatus;
import org.jetbrains.annotations.NotNull;

public final class PathSession {

    private final BlockPos start;
    private final IGoal goal;
    private PathStatus status;

    public PathSession(@NotNull BlockPos start, @NotNull IGoal goal) {
        this.start = start;
        this.goal = goal;
        this.status = PathStatus.CANCELLED;
    }

    public @NotNull BlockPos getStart() { return start; }
    public @NotNull IGoal getGoal() { return goal; }
    public @NotNull PathStatus getStatus() { return status; }
    public void setStatus(@NotNull PathStatus status) { this.status = status; }
}
