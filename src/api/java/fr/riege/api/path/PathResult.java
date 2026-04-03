package fr.riege.api.path;

/**
 * An immutable summary of a completed pathfinding computation, bundling the
 * resulting {@link Path} with performance diagnostics.
 *
 * <p>{@code PathResult} is the value propagated by
 * {@link fr.riege.api.event.events.PathCompleteEvent} after each search
 * finishes, regardless of whether a route was found.  Subscribers can inspect
 * both the path outcome and the search statistics in a single object.
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * bus.subscribe(PathCompleteEvent.class, this, event -> {
 *     PathResult result = event.getResult();
 *     if (result.path().status() == PathStatus.FOUND) {
 *         LOGGER.info("Path found in {}ms, {} nodes explored.",
 *             result.computeMs(), result.nodesExplored());
 *     }
 * });
 * }</pre>
 *
 * @param path          the path produced by the search; its {@link Path#status()}
 *                      indicates the outcome; never {@code null}
 * @param computeMs     the wall-clock time the search consumed, in
 *                      milliseconds; always {@code >= 0}
 * @param nodesExplored the total number of A* nodes that were popped from the
 *                      open set and expanded during the search; always
 *                      {@code >= 0}
 *
 * @see Path
 * @see PathStatus
 * @see fr.riege.api.event.events.PathCompleteEvent
 */
public record PathResult(Path path, long computeMs, int nodesExplored) {}
