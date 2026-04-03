package fr.riege.api.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A publish-subscribe event bus that dispatches {@link Event} instances to
 * registered {@link EventHandler} callbacks.
 *
 * <p>The bus is the central coordination mechanism for decoupled communication
 * between MelonFinder modules.  Producers post events without knowing which
 * consumers exist; consumers subscribe to specific event types without knowing
 * which producers will fire them.
 *
 * <h2>Subscription model</h2>
 * <p>Each call to a {@code subscribe} overload returns a {@link Subscription}
 * token.  Calling {@link #unsubscribe(Subscription)} with this token removes
 * the handler from the bus.  Alternatively, all subscriptions belonging to a
 * given owner object can be removed at once via
 * {@link #unsubscribeAll(Object)}, which is the preferred cleanup mechanism
 * for component-level lifecycle management.
 *
 * <h2>Dispatch order</h2>
 * <p>When an event is posted, all matching handlers are invoked in descending
 * {@link EventPriority} order.  Within the same priority level, delivery order
 * is unspecified.  If a handler calls {@link Event#cancel()}, the bus stops
 * delivering to subsequent (lower-priority) handlers.
 *
 * <h2>Phase filtering</h2>
 * <p>Subscriptions may optionally specify an {@link EventPhase}.  When an
 * event is posted with an explicit phase, only subscriptions with a matching
 * phase (or no phase restriction) are invoked.
 *
 * @see Event
 * @see EventHandler
 * @see Subscription
 * @see EventPriority
 * @see EventPhase
 */
public interface IEventBus {

    /**
     * Posts {@code event} to all matching subscribers using the default
     * (unspecified) phase.
     *
     * <p>This is the most common posting method.  All subscribers registered
     * without a phase restriction, and those registered for both phases, will
     * receive this event.
     *
     * @param event the event to dispatch; must not be {@code null}
     * @param <T>   the concrete event type
     */
    <T extends Event> void post(@NotNull T event);

    /**
     * Posts {@code event} to subscribers matching the specified {@code phase}.
     *
     * <p>Only handlers registered for {@code phase} (or for no specific phase)
     * will be invoked.  Use this overload to post the same event type at
     * distinct lifecycle points.
     *
     * @param event the event to dispatch; must not be {@code null}
     * @param phase the dispatch phase; must not be {@code null}
     * @param <T>   the concrete event type
     */
    <T extends Event> void post(@NotNull T event, @NotNull EventPhase phase);

    /**
     * Removes the handler associated with {@code subscription} from the bus.
     *
     * <p>After this call, the subscription's handler will no longer be invoked
     * for any future event posts.  If the subscription has already been
     * deactivated, this method has no effect.
     *
     * @param subscription the subscription token returned by a prior call to
     *                     a {@code subscribe} overload; must not be
     *                     {@code null}
     */
    void unsubscribe(@NotNull Subscription subscription);

    /**
     * Removes all subscriptions whose owner is {@code owner}.
     *
     * <p>This is the preferred way to clean up all handlers registered by a
     * component when that component is destroyed.  If no subscriptions exist
     * for {@code owner}, this method has no effect.
     *
     * @param owner the owner object passed to a {@code subscribe} overload;
     *              must not be {@code null}
     */
    void unsubscribeAll(@NotNull Object owner);

    /**
     * Subscribes {@code handler} to events of type {@code eventType} at
     * {@link EventPriority#NORMAL} priority with no phase restriction and no
     * owner.
     *
     * @param eventType the class token of the event type to subscribe to;
     *                  must not be {@code null}
     * @param handler   the callback to invoke when a matching event is posted;
     *                  must not be {@code null}
     * @param <T>       the concrete event type
     * @return a {@link Subscription} token that can be passed to
     *         {@link #unsubscribe(Subscription)} to remove this handler;
     *         never {@code null}
     */
    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            @NotNull EventHandler<T> handler);

    /**
     * Subscribes {@code handler} to events of type {@code eventType} at
     * {@link EventPriority#NORMAL} priority with no phase restriction, tagged
     * with {@code owner} for bulk removal.
     *
     * @param eventType the class token of the event type to subscribe to;
     *                  must not be {@code null}
     * @param owner     an arbitrary object used as the owner key for
     *                  {@link #unsubscribeAll(Object)}; may be {@code null}
     * @param handler   the callback to invoke when a matching event is posted;
     *                  must not be {@code null}
     * @param <T>       the concrete event type
     * @return the subscription token; never {@code null}
     */
    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            @Nullable Object owner,
            @NotNull EventHandler<T> handler);

    /**
     * Subscribes {@code handler} to events of type {@code eventType} at the
     * given {@code priority} with no phase restriction and no owner.
     *
     * @param eventType the class token of the event type to subscribe to;
     *                  must not be {@code null}
     * @param priority  the dispatch priority; higher values are called first;
     *                  use constants from {@link EventPriority}
     * @param handler   the callback to invoke when a matching event is posted;
     *                  must not be {@code null}
     * @param <T>       the concrete event type
     * @return the subscription token; never {@code null}
     */
    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            int priority,
            @NotNull EventHandler<T> handler);

    /**
     * Subscribes {@code handler} to events of type {@code eventType} posted in
     * the specified {@code phase} at {@link EventPriority#NORMAL} priority with
     * no owner.
     *
     * @param eventType the class token of the event type to subscribe to;
     *                  must not be {@code null}
     * @param phase     the event phase to filter on; must not be {@code null}
     * @param handler   the callback to invoke when a matching event is posted;
     *                  must not be {@code null}
     * @param <T>       the concrete event type
     * @return the subscription token; never {@code null}
     */
    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            @NotNull EventPhase phase,
            @NotNull EventHandler<T> handler);

    /**
     * Subscribes {@code handler} to events of type {@code eventType} at the
     * given {@code priority} with no phase restriction, tagged with
     * {@code owner} for bulk removal.
     *
     * @param eventType the class token of the event type to subscribe to;
     *                  must not be {@code null}
     * @param owner     the owner key; may be {@code null}
     * @param priority  the dispatch priority; higher values are called first
     * @param handler   the callback to invoke when a matching event is posted;
     *                  must not be {@code null}
     * @param <T>       the concrete event type
     * @return the subscription token; never {@code null}
     */
    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            @Nullable Object owner,
            int priority,
            @NotNull EventHandler<T> handler);

    /**
     * Subscribes {@code handler} to events of type {@code eventType} posted in
     * the specified {@code phase} at {@link EventPriority#NORMAL} priority,
     * tagged with {@code owner} for bulk removal.
     *
     * @param eventType the class token of the event type to subscribe to;
     *                  must not be {@code null}
     * @param owner     the owner key; may be {@code null}
     * @param phase     the event phase to filter on; must not be {@code null}
     * @param handler   the callback to invoke when a matching event is posted;
     *                  must not be {@code null}
     * @param <T>       the concrete event type
     * @return the subscription token; never {@code null}
     */
    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            @Nullable Object owner,
            @NotNull EventPhase phase,
            @NotNull EventHandler<T> handler);

    /**
     * The most general subscription overload: subscribes {@code handler} to
     * events of type {@code eventType} with full control over owner, priority,
     * and phase.
     *
     * <p>A {@code null} {@code phase} means the subscription has no phase
     * restriction and will match events posted with any phase (including the
     * no-phase {@link #post(Event)} overload).
     *
     * @param eventType the class token of the event type to subscribe to;
     *                  must not be {@code null}
     * @param owner     the owner key used for {@link #unsubscribeAll(Object)};
     *                  may be {@code null}
     * @param priority  the dispatch priority; higher values are called first;
     *                  use constants from {@link EventPriority}
     * @param phase     the event phase to filter on; {@code null} means no
     *                  phase restriction
     * @param handler   the callback to invoke when a matching event is posted;
     *                  must not be {@code null}
     * @param <T>       the concrete event type
     * @return the subscription token; never {@code null}
     */
    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            @Nullable Object owner,
            int priority,
            @Nullable EventPhase phase,
            @NotNull EventHandler<T> handler);
}
