package fr.riege.api.layer;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Provides physics properties for individual blocks in the world.
 *
 * <p>This interface is the single point of contact between the pathfinder and
 * block-level movement physics.  Implementations live exclusively in the
 * {@code layer} source set and translate Minecraft block states into
 * engine-neutral numeric values.  The pathfinder never inspects block identity
 * directly; it only reads the values returned by this interface.
 *
 * <h2>Coordinate convention</h2>
 * <p>All {@link BlockPos} arguments refer to the block whose south-west-bottom
 * corner is at that integer coordinate, consistent with Minecraft's block
 * coordinate system.  The implementation is responsible for converting to
 * whatever internal representation the underlying game engine uses.
 *
 * @see IWorldLayer
 * @see ICollisionLayer
 * @see IEntityPhysicsLayer
 */
public interface IBlockPhysicsLayer {

    /**
     * Returns the horizontal speed multiplier applied when an entity walks
     * across the top surface of the block at {@code pos}.
     *
     * <p>A value of {@code 1.0f} represents normal speed.  Values greater than
     * {@code 1.0f} accelerate movement (e.g. soul sand has a value below
     * {@code 1.0f}; honey blocks have an even lower value).  The pathfinder
     * uses this multiplier to inflate the movement cost of traversing the
     * block, making slower surfaces less attractive routes.
     *
     * @param pos the block position to query; must not be {@code null}
     * @return the speed multiplier; always positive; {@code 1.0f} for normal
     *         blocks
     */
    float getSpeedMultiplier(@NotNull BlockPos pos);

    /**
     * Returns the slipperiness coefficient of the top surface of the block at
     * {@code pos}.
     *
     * <p>Slipperiness affects how quickly an entity decelerates after stopping
     * input.  In vanilla Minecraft the default value is {@code 0.6f}; ice
     * surfaces typically return {@code 0.98f}.  The pathfinder uses this value
     * to model momentum, influencing whether a block is safe to stop on.
     *
     * @param pos the block position to query; must not be {@code null}
     * @return the slipperiness coefficient; typically in the range
     *         {@code [0.6, 0.98]}
     */
    float getSlipperiness(@NotNull BlockPos pos);

    /**
     * Returns whether the block at {@code pos} can be walked through without
     * physical obstruction.
     *
     * <p>Passable blocks include air, tall grass, flowers, and similar
     * non-solid blocks.  Solid blocks, fences, and barriers return
     * {@code false}.  This query is distinct from {@link IWorldLayer#isSolid}
     * because some blocks that are logically "solid" still allow passage (e.g.
     * signs).
     *
     * @param pos the block position to query; must not be {@code null}
     * @return {@code true} if an entity can move through this block;
     *         {@code false} if it presents a physical collision surface
     */
    boolean isPassable(@NotNull BlockPos pos);

    /**
     * Returns the exact Y coordinate at which an entity stands when resting on
     * the top surface of the block at {@code pos}.
     *
     * <p>For a full-height solid block at integer Y coordinate {@code n}, this
     * method returns {@code n + 1.0}.  For blocks with non-standard height
     * (slabs, stairs, soul sand) the returned value reflects the actual walking
     * surface.  The pathfinder uses this value to determine the precise
     * vertical position of a node rather than assuming {@code pos.y() + 1}.
     *
     * @param pos the block position to query; must not be {@code null}
     * @return the Y world coordinate of the walkable surface of the block;
     *         {@code >= pos.y()} and {@code <= pos.y() + 1.0}
     */
    double getStandingY(@NotNull BlockPos pos);

    /**
     * Returns the drag factor applied to an entity moving through the block at
     * {@code pos}.
     *
     * <p>Most blocks return {@code 1.0f} (no drag).  Fluid blocks and
     * cobwebs return values below {@code 1.0f}, representing significant
     * resistance.  The pathfinder uses this value to increase the cost of
     * passing through obstructing media.
     *
     * @param pos the block position to query; must not be {@code null}
     * @return the drag factor; {@code 1.0f} for unobstructed blocks; lower
     *         values indicate greater resistance
     */
    float getDragFactor(@NotNull BlockPos pos);

    /**
     * Returns the amount of damage per tick inflicted on an entity standing
     * inside the block at {@code pos}.
     *
     * <p>Blocks such as cacti, magma blocks, campfires, and sweet berry bushes
     * deal contact damage.  A return value of {@code 0.0f} indicates the block
     * is harmless.  The pathfinder uses this value to apply a damage-based
     * cost penalty, making hazardous blocks less attractive routes unless no
     * alternative exists.
     *
     * @param pos the block position to query; must not be {@code null}
     * @return the damage per tick; {@code 0.0f} for non-damaging blocks;
     *         always {@code >= 0.0f}
     */
    float getBlockDamage(@NotNull BlockPos pos);
}
