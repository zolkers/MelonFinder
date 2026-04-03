package fr.riege.api.path;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An immutable description of the route computed by the pathfinder, including
 * its status and the ordered sequence of geometric segments.
 *
 * <p>A {@code Path} is the primary output of a pathfinding computation.  It
 * carries the {@link PathStatus} indicating whether the search succeeded, and
 * — when successful — the ordered list of {@link Segment} objects that together
 * describe the full smoothed route from the start position to the goal.
 *
 * <h2>Validity</h2>
 * <p>Callers must always check {@link #status()} before consuming
 * {@link #segments()}.  A path whose status is anything other than
 * {@link PathStatus#FOUND} has an empty segment list; iterating over the
 * segments of a failed path will simply produce no results, but the caller
 * should branch on status rather than relying on an empty list as a sentinel.
 *
 * <h2>Defensive copy</h2>
 * <p>The compact constructor wraps the supplied segment list with
 * {@link List#copyOf}, so the returned list is always unmodifiable and
 * independent of the collection passed at construction time.
 *
 * @param segments  the ordered sequence of smoothed path segments; copied on
 *                  construction; empty for non-{@link PathStatus#FOUND} paths;
 *                  never {@code null}
 * @param totalCost the sum of {@link Segment#length()} over all segments, in
 *                  blocks; {@code 0.0} for non-found paths; always
 *                  {@code >= 0.0}
 * @param status    the outcome of the pathfinding computation; never
 *                  {@code null}
 *
 * @see PathStatus
 * @see Segment
 * @see PathResult
 */
public record Path(List<Segment> segments, double totalCost, PathStatus status) {

    /**
     * Constructs a {@code Path} with a defensive copy of the segment list.
     *
     * <p>The supplied {@code segments} list is copied via {@link List#copyOf};
     * the resulting internal list is unmodifiable and independent of the
     * original collection.
     *
     * @param segments  the segments comprising the route; must not be
     *                  {@code null}; the list is copied on construction
     * @param totalCost the total movement cost (sum of segment lengths), in
     *                  blocks; must be {@code >= 0.0}
     * @param status    the outcome of the search; must not be {@code null}
     */
    public Path(@NotNull List<Segment> segments, double totalCost, @NotNull PathStatus status) {
        this.segments = List.copyOf(segments);
        this.totalCost = totalCost;
        this.status = status;
    }
}
