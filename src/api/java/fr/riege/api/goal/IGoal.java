package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the termination condition for an A* pathfinding search.
 *
 * <p>An {@code IGoal} answers two questions the search engine asks:
 * <ol>
 *   <li><strong>Am I there yet?</strong> — {@link #isReached(BlockPos)} returns
 *       {@code true} when the search has found an acceptable destination block.
 *       Implementations may match an exact position ({@link BlockGoal}), a
 *       horizontal coordinate pair ({@link GoalXZ}), a region, or any other
 *       spatial predicate.</li>
 *   <li><strong>How far am I from the goal?</strong> — {@link #getTargetForHeuristic()}
 *       provides a reference {@link BlockPos} that the heuristic function uses
 *       to estimate the remaining cost.  This does not have to be the exact
 *       destination; it should be a representative point close enough that the
 *       Euclidean distance to it is an admissible lower bound on the true
 *       remaining cost.</li>
 * </ol>
 *
 * <h2>Admissibility</h2>
 * <p>For the A* search to find an optimal path, the heuristic must be
 * <em>admissible</em>: it must never overestimate the true cost.  The default
 * implementation of {@link #heuristicCost(BlockPos)} uses straight-line
 * Euclidean distance, which satisfies this condition provided movement costs
 * are always {@code >= 1.0} per block.
 *
 * @see BlockGoal
 * @see GoalXZ
 * @see fr.riege.api.event.events.PathCompleteEvent
 */
public interface IGoal {

    /**
     * Returns whether {@code current} is an acceptable destination for this
     * goal.
     *
     * <p>The search terminates as soon as the node with the lowest f-cost
     * satisfies this predicate.  Implementations must be fast and side-effect
     * free, as this method is called once per node popped from the open set.
     *
     * @param current the block position currently being evaluated; never
     *                {@code null}
     * @return {@code true} if this position satisfies the goal condition;
     *         {@code false} if the search should continue
     */
    boolean isReached(@NotNull BlockPos current);

    /**
     * Returns a representative target position used by the heuristic to
     * estimate the remaining travel cost from any candidate node.
     *
     * <p>The returned position does not have to be the exact goal block — it
     * must simply be close enough that the Euclidean distance to it is an
     * admissible heuristic.  For a point goal this is the goal block itself.
     * For an XZ goal this is the typed target with the best-guess Y coordinate.
     *
     * @return the heuristic reference position; never {@code null}
     */
    @NotNull BlockPos getTargetForHeuristic();

    /**
     * Returns the heuristic cost estimate from {@code from} to this goal.
     *
     * <p>The default implementation returns the Euclidean distance from
     * {@code from} to the position returned by {@link #getTargetForHeuristic()}.
     * Custom goal implementations may override this method to supply a tighter
     * or domain-specific heuristic, provided the result remains admissible.
     *
     * @param from the position from which to estimate the remaining cost;
     *             never {@code null}
     * @return the estimated remaining cost in blocks; always {@code >= 0.0}
     */
    default double heuristicCost(@NotNull BlockPos from) {
        return from.distanceTo(getTargetForHeuristic());
    }
}
