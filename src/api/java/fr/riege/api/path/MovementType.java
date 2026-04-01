package fr.riege.api.path;

import fr.riege.api.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

public final class MovementType {

    private final RegistryKey key;

    public MovementType(@NotNull RegistryKey key) {
        this.key = key;
    }

    @NotNull
    public RegistryKey getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MovementType other)) return false;
        return key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "MovementType{key=" + key + "}";
    }
}
