package fr.riege.api.event;

/**
 * A functional interface representing a callback that handles events of type
 * {@code T}.
 *
 * <p>{@code EventHandler} is the type accepted by all {@code subscribe} overloads
 * on {@link IEventBus}.  Because it is annotated with {@link FunctionalInterface},
 * handlers can be expressed as lambda expressions or method references, which is
 * the idiomatic usage:
 *
 * <pre>{@code
 * bus.subscribe(PathCompleteEvent.class, this,
 *     event -> displayPath(event.getResult().path()));
 * }</pre>
 *
 * <p>Implementations must not retain a strong reference to the event object beyond
 * the duration of the {@link #handle(Event)} invocation unless the event type
 * is explicitly documented as safe to retain.
 *
 * @param <T> the concrete {@link Event} type this handler accepts; must extend
 *            {@code Event}
 *
 * @see IEventBus
 * @see Subscription
 */
@FunctionalInterface
public interface EventHandler<T extends Event> {

    /**
     * Invoked by the event bus when an event of type {@code T} is posted.
     *
     * <p>The handler must complete promptly; long-running work should be
     * dispatched to a background thread.  Throwing an unchecked exception
     * from this method may prevent subsequent handlers from being notified,
     * depending on the bus implementation.
     *
     * @param event the event that was posted; never {@code null}; the same
     *              instance is passed to all subscribers for this event
     */
    void handle(T event);
}
