package fr.riege.api.event;

/**
 * The abstract base class for all events dispatched through the MelonFinder
 * event bus.
 *
 * <p>Every concrete event type must extend {@code Event}.  The base class
 * provides two capabilities shared by all events:
 * <ol>
 *   <li><strong>Timestamping</strong> — the creation time is captured at
 *       construction via {@link System#currentTimeMillis()} and exposed via
 *       {@link #getTimestamp()}.  Subscribers may use this to measure
 *       end-to-end latency.</li>
 *   <li><strong>Cancellation</strong> — a subscriber may call {@link #cancel()}
 *       to signal that subsequent subscribers (with lower priority) and the
 *       default action should be skipped.  Subscribers that only observe
 *       events without acting on them should use {@link EventPriority#MONITOR}
 *       and must not cancel.</li>
 * </ol>
 *
 * <p>Subclasses declare additional fields that carry the event-specific
 * payload (e.g. the completed {@link fr.riege.api.path.PathResult}).
 *
 * @see IEventBus
 * @see EventHandler
 * @see EventPriority
 * @see EventPhase
 */
public abstract class Event {

    private final long timestamp;
    private boolean cancelled;

    /**
     * Initialises the event with the current system time and a non-cancelled
     * state.
     *
     * <p>Subclass constructors must call this constructor either explicitly or
     * implicitly (via {@code super()}) before setting their own fields.
     */
    protected Event() {
        this.timestamp = System.currentTimeMillis();
        this.cancelled = false;
    }

    /**
     * Returns the wall-clock time at which this event was constructed, in
     * milliseconds since the Unix epoch.
     *
     * <p>The value is captured once in the constructor and never changes.
     * It is suitable for measuring the delay between event creation and
     * handler execution.
     *
     * @return the creation timestamp in milliseconds; always {@code > 0}
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns whether this event has been cancelled by a subscriber.
     *
     * <p>The event bus stops dispatching to lower-priority subscribers as soon
     * as this method returns {@code true}.  The exact cancellation semantics
     * (whether the triggering action is also skipped) depend on the event type
     * and the bus implementation.
     *
     * @return {@code true} if {@link #cancel()} has been called; {@code false}
     *         otherwise
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Marks this event as cancelled.
     *
     * <p>Once cancelled, the event bus will not deliver this event to
     * subscribers with lower priority than the subscriber that cancelled it.
     * Calling this method on an already-cancelled event has no additional
     * effect.
     *
     * <p>Subscribers registered with {@link EventPriority#MONITOR} must not
     * call this method; they are for observation only.
     */
    public void cancel() {
        this.cancelled = true;
    }
}
