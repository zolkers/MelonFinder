package fr.riege.api.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Subscription {

    private final Class<? extends Event> eventType;
    private final EventHandler<?> handler;
    private final int priority;
    private final EventPhase phase;
    private boolean active;

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

    public @NotNull Class<? extends Event> getEventType() {
        return eventType;
    }

    @SuppressWarnings("java:S1452")
    public @NotNull EventHandler<?> getHandler() {
        return handler;
    }

    public int getPriority() {
        return priority;
    }

    public @Nullable EventPhase getPhase() {
        return phase;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }
}
