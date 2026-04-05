package fr.riege.api.path;

/**
 * Describes the outcome of a single pathfinding computation.
 *
 * <p>The status is always available on the {@link Path} returned by the
 * pathfinder engine, regardless of whether a route was found.  Callers must
 * inspect the status before processing the path's segment list: only a path
 * with status {@link #FOUND} carries valid segments.
 *
 * @see Path#status()
 * @see PathResult
 */
public enum PathStatus {

    /**
     * A complete route from the start position to the goal was found and the
     * {@link Path} contains at least one {@link Segment}.
     *
     * <p>This is the only status for which {@link Path#segments()} is
     * non-empty and {@link Path#totalCost()} is meaningful.
     */
    FOUND,

    /**
     * The goal position is not reachable from the start given the current
     * world geometry and movement rules.
     *
     * <p>The A* search exhausted the entire reachable node graph without
     * satisfying the goal predicate.  The returned path has an empty segment
     * list and a total cost of {@code 0.0}.
     */
    UNREACHABLE,

    /**
     * The computation was aborted because it exceeded the maximum allowed
     * wall-clock time configured in the pathfinder context.
     *
     * <p>Partial results are discarded; the returned path has an empty segment
     * list.  Callers may retry with a simpler goal, a shorter maximum distance,
     * or a longer time budget.
     */
    TIMEOUT,

    /**
     * The computation was explicitly cancelled by the caller before it
     * completed.
     *
     * <p>The returned path has an empty segment list.  Cancellation can occur
     * because the player issued a new navigation command or because a higher-
     * level controller interrupted the current request.
     */
    CANCELLED,

    /**
     * A complete route to the goal could not be found, but the search reached
     * a node closer to the goal than the start position.
     *
     * <p>The returned path leads to the best explored node (lowest heuristic
     * cost to goal) that is at least five blocks from the start.  Callers
     * should follow this partial path and then recompute from the new position.
     */
    PARTIAL
}
