package fr.riege.api.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A handle representing a single registered event handler on an
 * {@link IEventBus}.
 *
 * <p>A {@code Subscription} is returned by every {@code subscribe} overload on
 * {@link IEventBus}.  It carries the registration metadata that the bus needs
 * to deliver and deactivate the subscription:
 * <ul>
 *   <li>the event type it listens for</li>
 *   <li>the handler to invoke</li>
 *   <li>the numeric priority</li>
 *   <li>the optional phase restriction</li>
 * </ul>
 *
 * <p>A subscription starts in an active state.  Passing it to
 * {@link IEventBus#unsubscribe(Subscription)} deactivates it, after which
 * {@link #isActive()} returns {@code false} and the handler is never called
 * again.
 *
 * <p>Instances of this class are not intended to be created directly by
 * application code; they are constructed by the bus implementation.
 *
 * @see IEventBus
 * @see EventHandler
 * @see EventPriority
 * @see EventPhase
 */
public final class Subscription {

    private final Class<? extends Event> eventType;
    private final EventHandler<?> handler;
    private final int priority;
    private final EventPhase phase;
    private boolean active;

    /**
     * Constructs a new, active subscription with the given parameters.
     *
     * <p>This constructor is called by the bus implementation; application
     * code should obtain subscriptions via
     * {@link IEventBus#subscribe(Class, EventHandler) IEventBus.subscribe}.
     *
     * @param eventType the class of events this subscription listens for;
     *                  must not be {@code null}
     * @param handler   the callback to invoke when a matching event is posted;
     *                  must not be {@code null}
     * @param priority  the dispatch priority; higher values are invoked first;
     *                  see {@link EventPriority} for named constants
     * @param phase     the phase restriction; {@code null} means no restriction
     *                  (matches events posted with any or no phase)
     */
    public Subscription(
            @NotNull Class<? extends Event> eventType,
            @NotNull EventHandler<?> handler,
            int priority,
            @Nullable EventPhase phase) {
        this.eventType = eventType;
        this.handler = handler;
        this.priority = priority;
        this.phase = phase;
        this.active = true;
    }

    /**
     * Returns the class of events that this subscription listens for.
     *
     * @return the event type; never {@code null}
     */
    public @NotNull Class<? extends Event> getEventType() {
        return eventType;
    }

    /**
     * Returns the handler that will be invoked when a matching event is posted.
     *
     * <p>The returned type uses a wildcard because the handler's type parameter
     * is erased at the subscription level.  The bus is responsible for casting
     * safely at dispatch time.
     *
     * @return the event handler; never {@code null}
     */
    @SuppressWarnings("java:S1452")
    public @NotNull EventHandler<?> getHandler() {
        return handler;
    }

    /**
     * Returns the numeric dispatch priority of this subscription.
     *
     * <p>Higher values cause this subscription to be invoked before
     * lower-priority subscriptions of the same event type.  See
     * {@link EventPriority} for named constants.
     *
     * @return the priority; any integer; typically one of the
     *         {@link EventPriority} constants
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the phase restriction for this subscription, or {@code null} if
     * the subscription has no phase restriction.
     *
     * <p>When the phase is non-{@code null}, the bus only delivers events
     * posted with the matching {@link EventPhase}.  When {@code null}, the bus
     * delivers events posted with any phase (or with no phase at all).
     *
     * @return the phase filter; may be {@code null}
     */
    public @Nullable EventPhase getPhase() {
        return phase;
    }

    /**
     * Returns whether this subscription is still active.
     *
     * <p>A subscription becomes inactive after
     * {@link IEventBus#unsubscribe(Subscription)} is called with this token,
     * or after {@link #deactivate()} is called directly by the bus
     * implementation.  Inactive subscriptions are never invoked.
     *
     * @return {@code true} if the handler may still be called for future
     *         events; {@code false} if this subscription has been removed
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Marks this subscription as inactive.
     *
     * <p>This method is called by the bus implementation when the subscription
     * is removed.  Application code should not call this method directly;
     * use {@link IEventBus#unsubscribe(Subscription)} instead.
     *
     * <p>Calling this method on an already-inactive subscription has no
     * additional effect.
     */
    public void deactivate() {
        this.active = false;
    }
}
