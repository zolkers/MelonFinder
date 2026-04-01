package fr.riege.api.registry;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface IRegistry<V> {

    void register(@NotNull RegistryKey key, @NotNull V value);

    @NotNull
    Optional<V> get(@NotNull RegistryKey key);

    @NotNull
    Collection<V> getAll();

    boolean contains(@NotNull RegistryKey key);
}
