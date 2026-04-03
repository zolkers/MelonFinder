package fr.riege.api.registry;

import org.jetbrains.annotations.NotNull;

/**
 * An immutable, namespaced identifier used to uniquely name entries in a
 * {@link IRegistry}.
 *
 * <p>A {@code RegistryKey} consists of two parts separated by a colon in its
 * string representation:
 * <ul>
 *   <li><strong>namespace</strong> — identifies the mod or subsystem that owns
 *       the entry (e.g. {@code "fr.riege"}).</li>
 *   <li><strong>path</strong> — identifies the specific entry within that
 *       namespace (e.g. {@code "walk"}, {@code "jump"}).</li>
 * </ul>
 *
 * <p>Keys are created via the static factory {@link #of(String)}, which
 * parses the {@code "namespace:path"} notation.  Equality is determined by
 * exact string comparison of both components, so two keys are equal if and
 * only if they have the same namespace and the same path.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * RegistryKey key = RegistryKey.of("fr.riege:walk");
 * registry.register(key, walkEvaluator);
 * }</pre>
 *
 * @see IRegistry
 * @see MovementKeys
 */
public final class RegistryKey {

    private final String namespace;
    private final String path;

    private RegistryKey(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    /**
     * Parses a {@code "namespace:path"} string and returns the corresponding
     * {@code RegistryKey}.
     *
     * <p>The string must contain exactly one colon character that separates
     * a non-empty namespace from a path.  The namespace appears before the
     * first colon; everything after the first colon is the path (the path
     * itself may contain additional colons, though by convention it does not).
     *
     * @param key the key string in {@code "namespace:path"} format; must not
     *            be {@code null}; must contain at least one {@code ':'} character
     * @return the parsed {@code RegistryKey}; never {@code null}
     * @throws IllegalArgumentException if {@code key} does not contain a colon
     */
    @NotNull
    public static RegistryKey of(@NotNull String key) {
        int colonIndex = key.indexOf(':');
        if (colonIndex < 0) {
            throw new IllegalArgumentException("Invalid registry key (missing ':'): " + key);
        }
        String namespace = key.substring(0, colonIndex);
        String path = key.substring(colonIndex + 1);
        return new RegistryKey(namespace, path);
    }

    /**
     * Returns the namespace component of this key.
     *
     * <p>The namespace is the portion of the key string before the first
     * colon.  It identifies the module or mod that registered this key.
     *
     * @return the namespace; never {@code null}; never empty
     */
    @NotNull
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the path component of this key.
     *
     * <p>The path is the portion of the key string after the first colon.
     * It identifies the specific entry within the namespace.
     *
     * @return the path; never {@code null}; may be empty if the key string
     *         ended with a colon (though this is not a recommended practice)
     */
    @NotNull
    public String getPath() {
        return path;
    }

    /**
     * Compares this key to {@code obj} for equality.
     *
     * <p>Two {@code RegistryKey} instances are equal if and only if both their
     * namespace and path components are equal.
     *
     * @param obj the object to compare; may be {@code null}
     * @return {@code true} if {@code obj} is a {@code RegistryKey} with
     *         identical namespace and path; {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RegistryKey other)) {
            return false;
        }
        return namespace.equals(other.namespace) && path.equals(other.path);
    }

    /**
     * Returns a hash code consistent with the {@link #equals(Object)}
     * contract.
     *
     * @return a hash code derived from both the namespace and the path
     */
    @Override
    public int hashCode() {
        int result = namespace.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    /**
     * Returns the canonical string representation of this key.
     *
     * <p>The format is {@code "namespace:path"}, which is the same format
     * accepted by {@link #of(String)}.
     *
     * @return a non-empty string in {@code "namespace:path"} format; never
     *         {@code null}
     */
    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
