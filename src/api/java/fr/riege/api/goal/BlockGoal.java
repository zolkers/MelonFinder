package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link IGoal} implementation that is satisfied only when the search
 * reaches a specific block position with an exact three-dimensional match.
 *
 * <p>This is the simplest possible goal: the search terminates as soon as it
 * visits the exact block at {@code (x, y, z)}.  It is suitable when the
 * target surface height is known in advance and the Y coordinate of the
 * destination is fixed.
 *
 * <h2>Limitation on uneven terrain</h2>
 * <p>Because {@code BlockGoal} requires an exact Y match, it will return
 * {@link fr.riege.api.path.PathStatus#UNREACHABLE UNREACHABLE} whenever the
 * walkable surface at the target X/Z is at a different Y than the one
 * specified.  When the precise standing Y is unknown, prefer {@link GoalXZ},
 * which ignores the Y coordinate in its reach predicate.
 *
 * @see GoalXZ
 * @see IGoal
 */
public final class BlockGoal implements IGoal {

    private final BlockPos target;

    /**
     * Constructs a {@code BlockGoal} that targets the given block position.
     *
     * @param target the exact block position that satisfies the goal; must
     *               not be {@code null}
     */
    public BlockGoal(@NotNull BlockPos target) {
        this.target = target;
    }

    /**
     * Returns {@code true} if and only if {@code current} equals the target
     * position on all three axes.
     *
     * @param current the block position currently being evaluated; must not
     *                be {@code null}
     * @return {@code true} if {@code current} is the exact target block;
     *         {@code false} otherwise
     */
    @Override
    public boolean isReached(@NotNull BlockPos current) {
        return current.equals(target);
    }

    /**
     * Returns the target block position, which is used directly as the
     * heuristic reference point.
     *
     * @return the target position; never {@code null}
     */
    @Override
    public @NotNull BlockPos getTargetForHeuristic() {
        return target;
    }
}
