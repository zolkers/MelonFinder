package fr.riege.pathfinder.registry;

import fr.riege.api.registry.IRegistry;
import fr.riege.api.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class SimpleRegistry<V> implements IRegistry<V> {

    private final Map<RegistryKey, V> entries = new LinkedHashMap<>();

    @Override
    public void register(@NotNull RegistryKey key, @NotNull V value) {
        if (entries.containsKey(key)) {
            throw new IllegalArgumentException("Key already registered: " + key);
        }
        entries.put(key, value);
    }

    @Override
    public @NotNull Optional<V> get(@NotNull RegistryKey key) {
        return Optional.ofNullable(entries.get(key));
    }

    @Override
    public @NotNull Collection<V> getAll() {
        return Collections.unmodifiableCollection(entries.values());
    }

    @Override
    public boolean contains(@NotNull RegistryKey key) {
        return entries.containsKey(key);
    }
}
