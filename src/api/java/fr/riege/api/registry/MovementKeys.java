package fr.riege.api.registry;

/**
 * Declares the canonical {@link RegistryKey} constants for all movement types
 * and heuristics built into MelonFinder.
 *
 * <p>Every movement evaluator registered by the {@code layer} module uses one
 * of these keys.  Client code that colours path segments by movement type or
 * selects a specific evaluator by key should compare against these constants
 * rather than constructing ad-hoc key strings.
 *
 * <p>This class is a non-instantiable constant holder; all members are
 * {@code public static final}.
 *
 * @see RegistryKey
 * @see IRegistry
 */
public final class MovementKeys {

    /**
     * Key for the standard walking movement: horizontal traversal of passable
     * blocks at ground level without jumping.
     */
    public static final RegistryKey WALK    = RegistryKey.of("fr.riege:walk");

    /**
     * Key for the jumping movement: a one-block step-up using an upward
     * velocity impulse, used when the next horizontal block is one block
     * higher than the current position.
     */
    public static final RegistryKey JUMP    = RegistryKey.of("fr.riege:jump");

    /**
     * Key for the falling movement: a multi-block drop with no upward
     * velocity, initiated when the ground is two or more blocks below the
     * current position.
     */
    public static final RegistryKey FALL    = RegistryKey.of("fr.riege:fall");

    /**
     * Key for the swimming movement: traversal through fluid blocks (water)
     * using the entity's swim speed.
     */
    public static final RegistryKey SWIM    = RegistryKey.of("fr.riege:swim");

    /**
     * Key for the climbing movement: upward or downward traversal on ladder
     * or vine blocks without consuming jump velocity.
     */
    public static final RegistryKey CLIMB   = RegistryKey.of("fr.riege:climb");

    /**
     * Key for the sprinting movement: horizontal traversal at increased speed,
     * allowed only on open flat ground meeting the sprint activation conditions.
     */
    public static final RegistryKey SPRINT  = RegistryKey.of("fr.riege:sprint");

    /**
     * Key for the sneaking movement: slow horizontal traversal at reduced
     * speed, used when the path must cross narrow ledges where falling is
     * otherwise possible.
     */
    public static final RegistryKey SNEAK   = RegistryKey.of("fr.riege:sneak");

    /**
     * Key for the parkour movement: a longer horizontal jump that crosses
     * gaps of two or more blocks at the cost of a jump impulse.
     */
    public static final RegistryKey PARKOUR = RegistryKey.of("fr.riege:parkour");

    /**
     * Key for the three-dimensional Euclidean heuristic used by the A* search
     * to estimate the remaining cost to the goal.
     */
    public static final RegistryKey HEURISTIC_EUCLIDEAN_3D =
            RegistryKey.of("fr.riege:heuristic_euclidean_3d");

    private MovementKeys() {
    }
}
