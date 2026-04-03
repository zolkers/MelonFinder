package fr.riege.api.layer;

import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Provides collision detection services against the world geometry.
 *
 * <p>This interface abstracts all broad-phase and narrow-phase collision
 * queries needed by the pathfinder and post-processing smoothers.  The
 * implementation lives in the {@code layer} source set and delegates to
 * Minecraft's block collision system.  No caller in {@code pathfinder} or
 * {@code api} may import any Minecraft class; all collision information must
 * flow through this interface.
 *
 * <h2>AABB convention</h2>
 * <p>{@link AABB} arguments use world-space coordinates where the minimum
 * corner ({@link AABB#min()}) is the south-west-bottom corner and the maximum
 * corner ({@link AABB#max()}) is the north-east-top corner.  Both corners are
 * expressed in the same floating-point world space used by {@link fr.riege.api.math.Vec3}.
 *
 * @see IWorldLayer
 * @see IBlockPhysicsLayer
 * @see IEntityPhysicsLayer
 */
public interface ICollisionLayer {

    /**
     * Returns all collision boxes contributed by the block at {@code pos}.
     *
     * <p>Most solid blocks contribute a single unit cube.  Blocks with complex
     * geometry (stairs, fences, walls) may contribute multiple non-overlapping
     * boxes.  Non-solid blocks (air, tall grass) return an empty list.
     *
     * <p>The returned boxes are expressed in world space, not block-local
     * space; each box's coordinates are already offset by the block's world
     * position.
     *
     * @param pos the block position to query; must not be {@code null}
     * @return an unmodifiable list of world-space collision boxes for the
     *         block; never {@code null}; may be empty
     */
    @NotNull List<AABB> getCollisionBoxes(@NotNull BlockPos pos);

    /**
     * Tests whether the given axis-aligned bounding box overlaps any solid
     * geometry in the world.
     *
     * <p>This is the primary query used by the pathfinder and path smoothers
     * to validate candidate positions.  The implementation should expand the
     * query to all blocks whose coordinate ranges intersect {@code box} and
     * test each block's collision boxes for intersection.
     *
     * @param box the bounding box to test; must not be {@code null}
     * @return {@code true} if {@code box} overlaps at least one solid
     *         collision box in the world; {@code false} if the volume is
     *         free of obstruction
     */
    boolean hasCollisionAt(@NotNull AABB box);

    /**
     * Returns the maximum distance an entity with half-width {@code hitboxHalf}
     * can travel from {@code from} in {@code dir} before encountering a solid
     * block face.
     *
     * <p>This is used by the LOS-based path smoother to determine whether a
     * straight-line segment between two waypoints is clear of geometry.  The
     * returned value is the safe travel distance along the ray; it is clamped
     * to the actual obstacle distance so that the caller can detect whether the
     * full intended distance is reachable.
     *
     * @param from       the origin block position of the ray; must not be
     *                   {@code null}
     * @param dir        the cardinal direction to cast the ray; must not be
     *                   {@code null}
     * @param hitboxHalf half the entity's horizontal hitbox width, in blocks;
     *                   used to widen the ray into a swept capsule
     * @return the distance in blocks the entity can travel before hitting
     *         solid geometry; always {@code >= 0.0}
     */
    double getMaxReach(@NotNull BlockPos from, @NotNull Direction dir, double hitboxHalf);
}
