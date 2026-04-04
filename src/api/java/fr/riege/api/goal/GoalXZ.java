package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link IGoal} implementation that is satisfied when the search reaches
 * any block at the target X and Z coordinates, regardless of its Y coordinate.
 *
 * <p>This goal is the preferred choice for player navigation commands where
 * the target coordinates are typed by the user and the exact standing height
 * at the destination is not known in advance.  Because Minecraft terrain is
 * uneven, the walkable surface at a given X/Z may be at a different Y than
 * the one the user typed; {@link BlockGoal}'s exact three-dimensional match
 * would then produce {@link fr.riege.api.path.PathStatus#UNREACHABLE UNREACHABLE}
 * even though a valid path exists.
 *
 * <h2>Heuristic target</h2>
 * <p>Even though the Y coordinate is ignored for reach detection, a Y value
 * must still be provided at construction time so that the heuristic can
 * compute a meaningful Euclidean distance estimate.  The supplied Y should be
 * the best available approximation of the actual destination height (e.g. the
 * value the user typed).  An inaccurate Y will not compromise correctness —
 * the A* search remains complete — but may produce a less efficient search if
 * the estimate is far from reality.
 *
 * @see BlockGoal
 * @see IGoal
 */
public final class GoalXZ implements IGoal {

    private final int x;
    private final int z;
    private final BlockPos heuristicTarget;

    /**
     * Constructs a {@code GoalXZ} that is satisfied by any block at
     * ({@code x}, *, {@code z}).
     *
     * @param x the target X block coordinate
     * @param y the Y coordinate used only for the heuristic distance estimate;
     *          does not affect the reach predicate
     * @param z the target Z block coordinate
     */
    public GoalXZ(int x, int y, int z) {
        this.x = x;
        this.z = z;
        this.heuristicTarget = new BlockPos(x, y, z);
    }

    /**
     * Returns {@code true} if {@code current} is at the target X and Z
     * coordinates, ignoring Y.
     *
     * @param current the block position currently being evaluated; must not
     *                be {@code null}
     * @return {@code true} if {@code current.x() == x} and
     *         {@code current.z() == z}; {@code false} otherwise
     */
    @Override
    public boolean isReached(@NotNull BlockPos current) {
        return current.x() == x && current.z() == z;
    }

    /**
     * Returns the heuristic reference position, constructed from the target
     * X and Z coordinates and the Y value supplied at construction time.
     *
     * @return the heuristic target; never {@code null}
     */
    @Override
    public @NotNull BlockPos getTargetForHeuristic() {
        return heuristicTarget;
    }
}
