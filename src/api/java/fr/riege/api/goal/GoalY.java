package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link IGoal} implementation that is satisfied when the search reaches
 * any block at a specific Y coordinate, regardless of its X and Z coordinates.
 *
 * <p>This goal is useful for navigating to a specific vertical level in the
 * world, such as a mining layer or a mountain peak, without having a
 * particular horizontal destination in mind.
 *
 * <h2>Heuristic target</h2>
 * <p>Since there is no specific horizontal target, the heuristic uses a
 * reference {@link BlockPos} with the starting X and Z coordinates and the
 * target Y.  This results in a heuristic that primarily accounts for the
 * vertical distance.
 *
 * @see BlockGoal
 * @see GoalXZ
 * @see IGoal
 */
public final class GoalY implements IGoal {

    private final int targetY;
    private final BlockPos heuristicTarget;

    /**
     * Constructs a {@code GoalY} that is satisfied by any block at the
     * target Y level.
     *
     * @param startX the X coordinate used for the initial heuristic estimate
     * @param targetY the target Y coordinate
     * @param startZ the Z coordinate used for the initial heuristic estimate
     */
    public GoalY(int startX, int targetY, int startZ) {
        this.targetY = targetY;
        this.heuristicTarget = new BlockPos(startX, targetY, startZ);
    }

    /**
     * Returns {@code true} if {@code current} is at the target Y coordinate.
     *
     * @param current the block position currently being evaluated; must not
     *                be {@code null}
     * @return {@code true} if {@code current.y() == targetY};
     *         {@code false} otherwise
     */
    @Override
    public boolean isReached(@NotNull BlockPos current) {
        return current.y() == targetY;
    }

    /**
     * Returns the heuristic reference position, which uses the target Y and
     * the X/Z coordinates provided at construction.
     *
     * @return the heuristic target; never {@code null}
     */
    @Override
    public @NotNull BlockPos getTargetForHeuristic() {
        return heuristicTarget;
    }

    /**
     * Overridden to provide a vertical-only heuristic estimate when the X/Z
     * distance is not relevant.
     *
     * @param from the position from which to estimate; never {@code null}
     * @return the vertical distance to the target Y
     */
    @Override
    public double heuristicCost(@NotNull BlockPos from) {
        return Math.abs(from.y() - targetY);
    }
}
