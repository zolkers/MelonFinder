package fr.riege.api.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBusImpl implements IEventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventBusImpl.class);

    private final Map<Class<? extends Event>, List<Subscription>> subscriptions;
    private final Map<Object, List<Subscription>> ownerSubscriptions;

    public EventBusImpl() {
        this.subscriptions = new ConcurrentHashMap<>();
        this.ownerSubscriptions = new ConcurrentHashMap<>();
    }

    @Override
    public <T extends Event> void post(@NotNull T event) {
        doPost(event, null);
    }

    @Override
    public <T extends Event> void post(@NotNull T event, @NotNull EventPhase phase) {
        doPost(event, phase);
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void doPost(@NotNull T event, @Nullable EventPhase phase) {
        List<Subscription> subs = subscriptions.get(event.getClass());
        if (subs == null || subs.isEmpty()) return;

        for (Subscription sub : subs) {
            if (!sub.isActive() || sub.getPhase() != null && phase != null && sub.getPhase() != phase) continue;
            if (event.isCancelled()) break;

            try {
                ((EventHandler<T>) sub.getHandler()).handle(event);
            } catch (Exception e) {
                LOGGER.error("Error dispatching event {}", event.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType, @NotNull EventHandler<T> handler) {
        return doSubscribe(eventType, null, EventPriority.NORMAL, null, handler);
    }

    @Override
    public <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType, @Nullable Object owner, @NotNull EventHandler<T> handler) {
        return doSubscribe(eventType, owner, EventPriority.NORMAL, null, handler);
    }

    @Override
    public <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType, int priority, @NotNull EventHandler<T> handler) {
        return doSubscribe(eventType, null, priority, null, handler);
    }

    @Override
    public <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType, @NotNull EventPhase phase, @NotNull EventHandler<T> handler) {
        return doSubscribe(eventType, null, EventPriority.NORMAL, phase, handler);
    }

    @Override
    public <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType, @Nullable Object owner, int priority,
            @NotNull EventHandler<T> handler) {
        return doSubscribe(eventType, owner, priority, null, handler);
    }

    @Override
    public <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType, @Nullable Object owner, @NotNull EventPhase phase,
            @NotNull EventHandler<T> handler) {
        return doSubscribe(eventType, owner, EventPriority.NORMAL, phase, handler);
    }

    @Override
    public <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType, @Nullable Object owner, int priority,
            @Nullable EventPhase phase, @NotNull EventHandler<T> handler) {
        return doSubscribe(eventType, owner, priority, phase, handler);
    }

    private <T extends Event> @NotNull Subscription doSubscribe(
            @NotNull Class<T> eventType, @Nullable Object owner, int priority,
            @Nullable EventPhase phase, @NotNull EventHandler<T> handler) {
        Subscription subscription = new Subscription(eventType, handler, priority, phase);

        subscriptions.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(subscription);
        sortSubscriptions(eventType);

        if (owner != null) {
            ownerSubscriptions.computeIfAbsent(owner, k -> new CopyOnWriteArrayList<>()).add(subscription);
        }

        return subscription;
    }

    @Override
    public void unsubscribe(@NotNull Subscription subscription) {
        subscription.deactivate();
        List<Subscription> subs = subscriptions.get(subscription.getEventType());
        if (subs != null) {
            subs.remove(subscription);
        }
    }

    @Override
    public void unsubscribeAll(@NotNull Object owner) {
        List<Subscription> subs = ownerSubscriptions.remove(owner);
        if (subs == null) return;
        for (Subscription sub : subs) {
            unsubscribe(sub);
        }
    }

    private void sortSubscriptions(@NotNull Class<? extends Event> eventType) {
        List<Subscription> subs = subscriptions.get(eventType);
        if (subs == null || subs.size() <= 1) return;
        subs.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
    }
}
