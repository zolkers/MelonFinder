package fr.riege.api.registry;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * A keyed container that maps {@link RegistryKey} identifiers to values of
 * type {@code V}.
 *
 * <p>The registry is the preferred alternative to switch expressions or
 * if-chains for extensible concerns such as movement evaluators and heuristics.
 * New behaviour can be contributed by calling {@link #register} without
 * modifying any existing code, which keeps the system open for extension and
 * closed for modification.
 *
 * <h2>Thread safety</h2>
 * <p>Implementations are not required to be thread-safe.  The pathfinder
 * registers all evaluators during initialisation, before any pathfinding
 * threads are started, and performs only read operations ({@link #get},
 * {@link #getAll}) during the search.  Callers must not register entries
 * concurrently with reads.
 *
 * @param <V> the type of value stored in this registry
 *
 * @see RegistryKey
 */
public interface IRegistry<V> {

    /**
     * Associates {@code value} with {@code key} in this registry.
     *
     * <p>If an entry already exists for {@code key}, the implementation may
     * either replace it or throw an exception depending on policy.  Callers
     * should treat duplicate registration as a programming error.
     *
     * @param key   the registry key to associate; must not be {@code null}
     * @param value the value to store; must not be {@code null}
     */
    void register(@NotNull RegistryKey key, @NotNull V value);

    /**
     * Returns the value associated with {@code key}, if present.
     *
     * @param key the registry key to look up; must not be {@code null}
     * @return an {@link Optional} containing the registered value, or
     *         {@link Optional#empty()} if no value has been registered under
     *         {@code key}; never {@code null}
     */
    @NotNull
    Optional<V> get(@NotNull RegistryKey key);

    /**
     * Returns an unmodifiable view of all values currently registered.
     *
     * <p>The iteration order of the returned collection is determined by the
     * implementation.  For order-sensitive uses (e.g. movement evaluators that
     * must be tried in priority order), use an implementation that preserves
     * insertion order, such as the built-in {@code OrderedRegistry}.
     *
     * @return a collection containing every registered value; never
     *         {@code null}; may be empty if no values have been registered
     */
    @NotNull
    Collection<V> getAll();

    /**
     * Returns whether a value has been registered under {@code key}.
     *
     * @param key the registry key to test; must not be {@code null}
     * @return {@code true} if a value is registered under {@code key};
     *         {@code false} otherwise
     */
    boolean contains(@NotNull RegistryKey key);
}
