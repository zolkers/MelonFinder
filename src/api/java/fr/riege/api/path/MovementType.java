package fr.riege.api.path;

import fr.riege.api.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

/**
 * An immutable token that identifies the kind of movement used to traverse a
 * path segment.
 *
 * <p>Movement types are identified by their {@link RegistryKey}, not by class
 * identity.  This allows movement types to be compared across module
 * boundaries without the caller needing to depend on concrete evaluator
 * classes in the {@code pathfinder} module.  All built-in keys are declared
 * as constants in {@link fr.riege.api.registry.MovementKeys}.
 *
 * <h2>Equality</h2>
 * <p>Two {@code MovementType} instances are considered equal if and only if
 * their underlying {@link RegistryKey} instances are equal.  The overridden
 * {@link #equals(Object)} method enforces this contract explicitly rather than
 * relying on the default record equality, ensuring correct behaviour when
 * comparing instances created in different contexts.
 *
 * @param key the registry key that uniquely names this movement type;
 *            must not be {@code null}
 *
 * @see fr.riege.api.registry.MovementKeys
 * @see RegistryKey
 * @see Node
 */
public record MovementType(RegistryKey key) {

    /**
     * Compares this movement type to {@code o} for equality.
     *
     * <p>Two {@code MovementType} instances are equal if and only if their
     * {@link #key()} values are equal under {@link RegistryKey#equals(Object)}.
     *
     * @param o the object to compare; may be {@code null}
     * @return {@code true} if {@code o} is a {@code MovementType} carrying
     *         the same registry key; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MovementType(RegistryKey key1))) return false;
        return key.equals(key1);
    }

    /**
     * Returns a human-readable string representation of this movement type.
     *
     * <p>The format is {@code MovementType{key=<key>}}.
     *
     * @return a non-empty string identifying the movement type; never
     *         {@code null}
     */
    @Override
    public @NotNull String toString() {
        return "MovementType{key=" + key + "}";
    }
}
