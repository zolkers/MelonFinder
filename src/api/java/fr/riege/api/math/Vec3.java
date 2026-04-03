package fr.riege.api.math;

import org.jetbrains.annotations.NotNull;

/**
 * An immutable three-dimensional vector with double-precision floating-point
 * components.
 *
 * <p>{@code Vec3} is used throughout the path-smoothing pipeline to represent
 * sub-block positions in world space.  Unlike {@link BlockPos}, which stores
 * integer block coordinates, {@code Vec3} can describe any point within a
 * block (e.g. the result of sub-block sampling or Catmull-Rom interpolation).
 *
 * <p>Instances are created either directly via the canonical record constructor
 * or through helper methods.  Because {@code Vec3} is a record, its
 * {@link #equals(Object)} and {@link #hashCode()} methods compare all three
 * components for exact equality using {@link Double#compare}.
 *
 * <h2>Coordinate convention</h2>
 * <p>Coordinates follow the Minecraft world-space convention:
 * <ul>
 *   <li>{@link #x()} runs west (negative) to east (positive).</li>
 *   <li>{@link #y()} runs downward (negative) to upward (positive).</li>
 *   <li>{@link #z()} runs north (negative) to south (positive).</li>
 * </ul>
 *
 * @param x the X component of this vector
 * @param y the Y component of this vector
 * @param z the Z component of this vector
 *
 * @see BlockPos
 */
public record Vec3(double x, double y, double z) {

    /**
     * Returns the Euclidean distance between this vector and {@code other}.
     *
     * <p>The distance is computed as
     * {@code sqrt((x - other.x)² + (y - other.y)² + (z - other.z)²)}.
     * Both vectors are treated as points in three-dimensional space.
     *
     * @param other the target vector; must not be {@code null}
     * @return the straight-line distance between the two points; always
     *         {@code >= 0.0}
     */
    public double distanceTo(@NotNull Vec3 other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Returns a new {@code Vec3} whose components are this vector's components
     * translated by the given deltas.
     *
     * <p>This method does not modify the receiver; it returns a newly allocated
     * record instance.
     *
     * @param dx the amount to add to the X component
     * @param dy the amount to add to the Y component
     * @param dz the amount to add to the Z component
     * @return a new {@code Vec3} equal to {@code (x+dx, y+dy, z+dz)}; never
     *         {@code null}
     */
    @NotNull
    public Vec3 add(double dx, double dy, double dz) {
        return new Vec3(x + dx, y + dy, z + dz);
    }

    /**
     * Compares this vector to {@code obj} for exact component-wise equality.
     *
     * <p>Two {@code Vec3} instances are equal if and only if all three
     * corresponding components compare equal under {@link Double#compare},
     * which distinguishes {@code +0.0} from {@code -0.0} and treats
     * {@code NaN} as equal to itself (unlike {@code ==}).
     *
     * @param obj the object to compare; may be {@code null}
     * @return {@code true} if {@code obj} is a {@code Vec3} with identical
     *         X, Y, and Z components; {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vec3(double x1, double y1, double z1))) {
            return false;
        }
        return Double.compare(x, x1) == 0
                && Double.compare(y, y1) == 0
                && Double.compare(z, z1) == 0;
    }

    /**
     * Returns a hash code consistent with the component-wise
     * {@link #equals(Object)} contract.
     *
     * @return a hash code derived from all three double components
     */
    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        return result;
    }

    /**
     * Returns a human-readable string representation of this vector.
     *
     * <p>The format is {@code Vec3{x=<x>, y=<y>, z=<z>}}.
     *
     * @return a non-empty string describing this vector; never {@code null}
     */
    @Override
    public @NotNull String toString() {
        return "Vec3{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
