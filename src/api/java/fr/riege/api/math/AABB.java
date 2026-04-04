package fr.riege.api.math;

import fr.riege.api.layer.ICollisionLayer;
import org.jetbrains.annotations.NotNull;

/**
 * An immutable axis-aligned bounding box defined by two corner vectors in
 * world space.
 *
 * <p>An {@code AABB} is specified by its minimum corner ({@link #min()}) and
 * its maximum corner ({@link #max()}), where each component of {@code min} is
 * less than or equal to the corresponding component of {@code max}.  The box
 * is "closed" — boundary contact counts as an intersection.
 *
 * <p>{@code AABB} instances are used throughout the pathfinder for:
 * <ul>
 *   <li>Representing the entity's physical hitbox at a candidate position,
 *       passed to {@link ICollisionLayer#hasCollisionAt(AABB)} to validate
 *       walkability.</li>
 *   <li>Representing individual block collision boxes returned by
 *       {@link ICollisionLayer#getCollisionBoxes(BlockPos)}.</li>
 *   <li>Broadening a ray into a swept capsule volume for line-of-sight
 *       checks.</li>
 * </ul>
 *
 * <p>Because {@code AABB} is a record, equality is determined by exact
 * component-wise equality of both {@link Vec3} corners.
 *
 * @param min the minimum (south-west-bottom) corner of the box; every
 *            component must be {@code <=} the corresponding component of
 *            {@code max}
 * @param max the maximum (north-east-top) corner of the box; every
 *            component must be {@code >=} the corresponding component of
 *            {@code min}
 *
 * @see Vec3
 * @see ICollisionLayer
 */
public record AABB(Vec3 min, Vec3 max) {

    /**
     * Tests whether this box overlaps {@code other}.
     *
     * <p>Overlap is tested axis by axis using an open interval on the
     * separating-axis side: two boxes do <em>not</em> overlap if one's
     * maximum coordinate on any axis is less than or equal to the other's
     * minimum on that same axis.  All other configurations are considered
     * intersecting, including the case where the two boxes share only a face,
     * edge, or corner.
     *
     * @param other the bounding box to test against; must not be {@code null}
     * @return {@code true} if the two boxes occupy any overlapping volume
     *         (including boundary contact); {@code false} if they are
     *         completely disjoint
     */
    public boolean intersects(@NotNull AABB other) {
        boolean noOverlapX = this.max.x() <= other.min.x() || other.max.x() <= this.min.x();
        boolean noOverlapY = this.max.y() <= other.min.y() || other.max.y() <= this.min.y();
        boolean noOverlapZ = this.max.z() <= other.min.z() || other.max.z() <= this.min.z();
        return !(noOverlapX || noOverlapY || noOverlapZ);
    }

    /**
     * Returns a new {@code AABB} that is this box uniformly expanded by
     * {@code amount} in all six directions.
     *
     * <p>Each minimum component is decreased by {@code amount} and each
     * maximum component is increased by {@code amount}, enlarging the box
     * by {@code 2 * amount} along each axis.  The original box is not
     * modified.
     *
     * <p>Passing a negative {@code amount} shrinks the box.  If the shrinkage
     * causes any minimum component to exceed the corresponding maximum
     * component, the resulting box is degenerate (zero or negative volume)
     * and intersection tests will return {@code false}.
     *
     * @param amount the expansion amount in world-space units; may be
     *               negative to shrink the box
     * @return a new {@code AABB} expanded (or shrunk) by {@code amount};
     *         never {@code null}
     */
    @NotNull
    public AABB expand(double amount) {
        Vec3 newMin = min.add(-amount, -amount, -amount);
        Vec3 newMax = max.add(amount, amount, amount);
        return new AABB(newMin, newMax);
    }

    /**
     * Returns a human-readable string representation of this bounding box.
     *
     * <p>The format is {@code AABB{min=<min>, max=<max>}}.
     *
     * @return a non-empty string describing the two corners; never
     *         {@code null}
     */
    @Override
    public @NotNull String toString() {
        return "AABB{min=" + min + ", max=" + max + "}";
    }
}
