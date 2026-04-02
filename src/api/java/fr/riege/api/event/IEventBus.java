package fr.riege.api.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IEventBus {

    <T extends Event> void post(@NotNull T event);

    <T extends Event> void post(@NotNull T event, @NotNull EventPhase phase);

    void unsubscribe(@NotNull Subscription subscription);

    void unsubscribeAll(@NotNull Object owner);

    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            @NotNull EventHandler<T> handler);

    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            @Nullable Object owner,
            @NotNull EventHandler<T> handler);

    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            int priority,
            @NotNull EventHandler<T> handler);

    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            @NotNull EventPhase phase,
            @NotNull EventHandler<T> handler);

    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            @Nullable Object owner,
            int priority,
            @NotNull EventHandler<T> handler);

    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            @Nullable Object owner,
            @NotNull EventPhase phase,
            @NotNull EventHandler<T> handler);

    <T extends Event> @NotNull Subscription subscribe(
            @NotNull Class<T> eventType,
            @Nullable Object owner,
            int priority,
            @Nullable EventPhase phase,
            @NotNull EventHandler<T> handler);
}
