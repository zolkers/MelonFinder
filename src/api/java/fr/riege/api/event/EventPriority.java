package fr.riege.api.event;

/**
 * Declares the standard integer priority levels for event subscribers.
 *
 * <p>When an event is posted, the bus delivers it to all matching subscribers
 * in <em>descending</em> priority order: a subscriber with priority
 * {@link #HIGHEST} is called before one with {@link #NORMAL}, which is called
 * before one with {@link #LOWEST}.  Within the same priority level, the
 * delivery order is unspecified.
 *
 * <p>These constants are conventions, not enforcement boundaries.  Any integer
 * may be passed as the priority argument to
 * {@link IEventBus#subscribe(Class, int, EventHandler) subscribe}; the named
 * constants simply provide readable defaults for the most common cases.
 *
 * <h2>Intended usage</h2>
 * <table border="1" summary="Priority guidelines">
 *   <tr><th>Constant</th><th>Value</th><th>Typical use</th></tr>
 *   <tr><td>{@link #MONITOR}</td><td>1000</td><td>Read-only observation; must never cancel</td></tr>
 *   <tr><td>{@link #HIGHEST}</td><td>100</td><td>Critical intercept or override</td></tr>
 *   <tr><td>{@link #HIGH}</td><td>50</td><td>Plugin logic that must run early</td></tr>
 *   <tr><td>{@link #NORMAL}</td><td>0</td><td>Default for most subscribers</td></tr>
 *   <tr><td>{@link #LOW}</td><td>-50</td><td>Fallback logic</td></tr>
 *   <tr><td>{@link #LOWEST}</td><td>-100</td><td>Last-resort processing</td></tr>
 * </table>
 *
 * <p>This class is a non-instantiable constant holder.
 *
 * @see IEventBus
 * @see Event
 */
public final class EventPriority {

    /**
     * The lowest standard priority level ({@code -100}).
     *
     * <p>Subscribers at this level are called after all higher-priority
     * subscribers have had a chance to act or cancel the event.
     */
    public static final int LOWEST  = -100;

    /**
     * A below-normal priority level ({@code -50}).
     *
     * <p>Suitable for fallback or secondary processing that should occur after
     * the primary handlers have run.
     */
    public static final int LOW     = -50;

    /**
     * The default priority level ({@code 0}).
     *
     * <p>Used by {@link IEventBus#subscribe(Class, EventHandler)} when no
     * explicit priority is specified.  Most application code should use this
     * level.
     */
    public static final int NORMAL  = 0;

    /**
     * An above-normal priority level ({@code 50}).
     *
     * <p>Use when the subscriber must intercept the event before normal-priority
     * handlers, for example to validate or transform the event payload.
     */
    public static final int HIGH    = 50;

    /**
     * The highest standard priority level ({@code 100}).
     *
     * <p>Subscribers at this level are called before all other subscribers
     * except {@link #MONITOR}.  Prefer {@link #HIGH} unless pre-emption of
     * all other handlers is strictly necessary.
     */
    public static final int HIGHEST = 100;

    /**
     * A special observation-only priority level ({@code 1000}).
     *
     * <p>Subscribers at this level are called last, after all other subscribers
     * including those at {@link #LOWEST}.  They must not cancel the event; they
     * exist solely to record the final state of the event for logging,
     * statistics, or debugging.
     */
    public static final int MONITOR = 1000;

    private EventPriority() {}
}
