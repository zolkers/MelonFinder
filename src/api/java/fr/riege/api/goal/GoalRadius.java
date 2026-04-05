package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link IGoal} implementation that is satisfied when the search reaches
 * any block within a specified Euclidean distance from a target center.
 *
 * <p>This goal is useful for navigating to the general vicinity of an entity,
 * a block, or a location when the exact standing position is not critical.
 * It is particularly effective for reaching interactable objects that can be
 * used from a distance (e.g., chests, crafting tables, or entities).
 *
 * <h2>Heuristic target</h2>
 * <p>The target center is used as the heuristic reference point.  Since every
 * point that satisfies {@link #isReached(BlockPos)} is at most {@code radius}
 * away from the center, the Euclidean distance to the center remains an
 * admissible (though potentially conservative) heuristic.
 *
 * @see BlockGoal
 * @see GoalXZ
 * @see IGoal
 */
public final class GoalRadius implements IGoal {

    private final BlockPos center;
    private final double radiusSq;

    /**
     * Constructs a {@code GoalRadius} that is satisfied by any block within
     * the given radius of the target center.
     *
     * @param center the target center position; must not be {@code null}
     * @param radius the maximum Euclidean distance from the center that
     *               satisfies the goal; must be non-negative
     */
    public GoalRadius(@NotNull BlockPos center, double radius) {
        this.center = center;
        this.radiusSq = radius * radius;
    }

    /**
     * Returns {@code true} if {@code current} is within the specified radius
     * of the target center.
     *
     * @param current the block position currently being evaluated; must not
     *                be {@code null}
     * @return {@code true} if the distance to {@code center} is less than or
     *         equal to {@code radius}; {@code false} otherwise
     */
    @Override
    public boolean isReached(@NotNull BlockPos current) {
        return current.distanceSqTo(center) <= radiusSq;
    }

    /**
     * Returns the target center position, which is used as the heuristic
     * reference point.
     *
     * @return the target center; never {@code null}
     */
    @Override
    public @NotNull BlockPos getTargetForHeuristic() {
        return center;
    }
}
