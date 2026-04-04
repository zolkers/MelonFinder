package fr.riege.api.event.events;

import fr.riege.api.event.Event;
import fr.riege.api.path.PathResult;
import org.jetbrains.annotations.NotNull;

/**
 * Posted by the pathfinder engine when a computation finishes, regardless of
 * whether a route was found.
 *
 * <p>This event is fired on the thread that ran the pathfinding computation,
 * immediately after the engine returns a {@link PathResult}.  Subscribers
 * should keep their handlers short; any heavy work (rendering, persistence)
 * should be handed off to the appropriate thread.
 *
 * <p>{@code PathCompleteEvent} is a post-action event — by the time it is
 * dispatched the search has already finished and its result is immutable.
 * Cancelling this event suppresses downstream subscribers but has no effect
 * on the completed computation.
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * bus.subscribe(PathCompleteEvent.class, this, event -> {
 *     PathResult result = event.getResult();
 *     if (result.path().status() == PathStatus.FOUND) {
 *         client.displayPath(result.path());
 *     }
 * });
 * }</pre>
 *
 * @see PathResult
 * @see fr.riege.api.path.PathStatus
 * @see fr.riege.api.event.IEventBus
 */
public final class PathCompleteEvent extends Event {

    private final PathResult result;

    /**
     * Constructs a {@code PathCompleteEvent} carrying the given result.
     *
     * @param result the outcome of the pathfinding computation; must not be
     *               {@code null}
     */
    public PathCompleteEvent(@NotNull PathResult result) {
        this.result = result;
    }

    /**
     * Returns the result of the pathfinding computation that triggered this
     * event.
     *
     * <p>The result encapsulates the computed {@link fr.riege.api.path.Path},
     * the wall-clock time consumed by the search, and the number of A* nodes
     * that were explored.  Callers should always check
     * {@link fr.riege.api.path.Path#status()} before consuming the segment
     * list.
     *
     * @return the path result; never {@code null}
     */
    public @NotNull PathResult getResult() {
        return result;
    }
}
