package fr.riege.api.layer;

/**
 * Provides physics constants for the entity being controlled by the pathfinder.
 *
 * <p>All values returned by this interface describe the <em>current</em>
 * physical state of the entity: its dimensions, movement capabilities, and
 * movement modifiers.  The pathfinder queries this interface when constructing
 * collision volumes and evaluating movement costs; it never hard-codes any
 * numeric constant that could differ between entity types or game versions.
 *
 * <p>Implementations live in the {@code layer} source set and read values
 * from the live Minecraft entity object.
 *
 * @see IBlockPhysicsLayer
 * @see ICollisionLayer
 * @see IWorldLayer
 */
public interface IEntityPhysicsLayer {

    /**
     * Returns the total width of the entity's axis-aligned hitbox, in blocks.
     *
     * <p>The hitbox is symmetric; the half-width used for collision tests is
     * {@code getHitboxWidth() / 2.0}.  For a standard player this is
     * {@code 0.6} blocks.
     *
     * @return the hitbox width in blocks; always positive
     */
    double getHitboxWidth();

    /**
     * Returns the total height of the entity's axis-aligned hitbox, in blocks.
     *
     * <p>This value is used to build the vertical extent of collision volumes.
     * For a standard player this is {@code 1.8} blocks.
     *
     * @return the hitbox height in blocks; always positive
     */
    double getHitboxHeight();

    /**
     * Returns the maximum step height the entity can climb without jumping, in
     * blocks.
     *
     * <p>Blocks at a height difference up to and including this value can be
     * traversed as a walk rather than a jump.  For a standard player this is
     * {@code 0.6} blocks, allowing it to step up a full slab in a single tick.
     *
     * @return the step height in blocks; always positive; typically
     *         {@code 0.6}
     */
    double getStepHeight();

    /**
     * Returns the initial upward velocity imparted when the entity jumps, in
     * blocks per tick.
     *
     * <p>The pathfinder uses this value together with gravitational
     * deceleration to compute the maximum reachable height of a jump arc.
     * For a standard player on a non-slippery surface this is approximately
     * {@code 0.42} blocks per tick.
     *
     * @return the jump velocity in blocks per tick; always positive
     */
    double getJumpVelocity();

    /**
     * Evaluates the damage the entity would take after falling {@code blocks}
     * blocks, in half-hearts.
     *
     * <p>Vanilla fall-damage starts accumulating after falling more than
     * 3 blocks and scales linearly.  The pathfinder uses this method to
     * determine whether a given fall is lethal or too costly, and marks the
     * corresponding movement as impossible or penalised accordingly.
     *
     * @param blocks the number of blocks fallen; must be non-negative
     * @return the fall damage in half-hearts; {@code 0.0f} if the fall is
     *         within safe limits; always {@code >= 0.0f}
     */
    float evaluateFallDamage(int blocks);

    /**
     * Returns the entity's horizontal movement speed while swimming, in blocks
     * per tick.
     *
     * <p>This value is used by the swim evaluator to compute the cost of
     * traversing fluid blocks.  It is typically lower than the on-land walk
     * speed.
     *
     * @return the swim speed in blocks per tick; always positive
     */
    double getSwimSpeed();

    /**
     * Returns the sprint speed multiplier applied on top of the base walk
     * speed.
     *
     * <p>A value of {@code 1.3} means sprinting moves the entity at 130% of
     * its normal walking speed.  The pathfinder uses this value to reduce the
     * cost of sprint-eligible movement segments.
     *
     * @return the sprint speed multiplier; always {@code >= 1.0}
     */
    double getSprintMultiplier();

    /**
     * Returns the speed multiplier applied when the entity is sneaking.
     *
     * <p>A value of {@code 0.3} means sneaking moves the entity at 30% of its
     * normal walking speed.  The pathfinder uses this to increase the movement
     * cost of sneak segments, making them less preferred unless required for
     * navigating narrow ledges.
     *
     * @return the sneak speed multiplier; in the range {@code (0.0, 1.0]}
     */
    double getSneakSpeedMultiplier();
}
