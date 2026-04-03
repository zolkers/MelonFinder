package fr.riege.api.event;

/**
 * Represents the dispatch phase of an event within the event bus lifecycle.
 *
 * <p>When an event is posted with an explicit phase, only subscribers that
 * were registered for that same phase (or for no specific phase) receive the
 * notification.  This allows a single event type to carry two semantically
 * distinct moments:
 * <ul>
 *   <li>{@link #PRE} — <em>before</em> the action associated with the event
 *       occurs; cancellation here can prevent the action.</li>
 *   <li>{@link #POST} — <em>after</em> the action has already occurred;
 *       cancellation here has no effect on the triggering action but can
 *       suppress downstream processing.</li>
 * </ul>
 *
 * <p>Not all event types are posted in both phases.  Event types that model
 * completed actions (e.g. {@link fr.riege.api.event.events.PathCompleteEvent})
 * are posted only in the {@code POST} phase.
 *
 * @see IEventBus
 * @see Event
 */
public enum EventPhase {

    /**
     * The pre-action phase, posted before the triggering action takes place.
     *
     * <p>Subscribers registered for this phase may cancel the event to prevent
     * the action from executing.
     */
    PRE,

    /**
     * The post-action phase, posted after the triggering action has completed.
     *
     * <p>Subscribers registered for this phase observe the result of the
     * action.  Cancelling a post-phase event suppresses downstream handlers
     * but cannot undo the action itself.
     */
    POST
}
