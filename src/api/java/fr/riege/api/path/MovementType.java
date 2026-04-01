package fr.riege.api.path;

import fr.riege.api.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

public record MovementType(RegistryKey key) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MovementType(RegistryKey key1))) return false;
        return key.equals(key1);
    }

    @Override
    public @NotNull String toString() {
        return "MovementType{key=" + key + "}";
    }
}
