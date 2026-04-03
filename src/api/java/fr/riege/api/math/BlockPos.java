package fr.riege.api.math;

import org.jetbrains.annotations.NotNull;

/**
 * An immutable integer block position in three-dimensional world space.
 *
 * <p>A {@code BlockPos} identifies a single block in the Minecraft world.  Each
 * coordinate names the south-west-bottom corner of the 1×1×1 block volume at
 * that integer grid point.  This is the primary unit of currency for A* search:
 * the start position, goal position, neighbour expansion, and explored-node map
 * all use {@code BlockPos}.
 *
 * <h2>Coordinate system</h2>
 * <ul>
 *   <li>{@link #x()} runs west (negative) to east (positive).</li>
 *   <li>{@link #y()} runs downward (negative) to upward (positive).</li>
 *   <li>{@link #z()} runs north (negative) to south (positive).</li>
 * </ul>
 *
 * <h2>Packing</h2>
 * <p>For use in hash maps and open-set implementations that need a single
 * long key, use {@link #asLong()}.  The encoding supports coordinates in the
 * range {@code [-2^25, 2^25)} on X and Z and {@code [-2^11, 2^11)} on Y, which
 * comfortably covers the Minecraft world boundary.
 *
 * @param x the X coordinate of this block position
 * @param y the Y coordinate of this block position
 * @param z the Z coordinate of this block position
 *
 * @see Vec3
 */
public record BlockPos(int x, int y, int z) {

    /**
     * Returns the Euclidean distance between the centre of this block and the
     * centre of {@code other}.
     *
     * <p>Block centres are at half-integer offsets ({@code x + 0.5},
     * {@code y + 0.5}, {@code z + 0.5}), but this method computes distance
     * between the integer coordinates directly (treating each position as a
     * dimensionless point).  This is consistent with the heuristic distance
     * used by the A* search.
     *
     * @param other the target position; must not be {@code null}
     * @return the straight-line distance between the two block positions;
     *         always {@code >= 0.0}
     */
    public double distanceTo(@NotNull BlockPos other) {
        double dx = (double) this.x - other.x;
        double dy = (double) this.y - other.y;
        double dz = (double) this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Returns a new {@code BlockPos} displaced from this position by the given
     * integer offsets.
     *
     * <p>This is the primary method for generating neighbour positions during
     * A* node expansion.  For example, to obtain the block directly above this
     * position: {@code pos.offset(0, 1, 0)}.
     *
     * @param dx the displacement along the X axis in blocks
     * @param dy the displacement along the Y axis in blocks
     * @param dz the displacement along the Z axis in blocks
     * @return a new {@code BlockPos} at {@code (x+dx, y+dy, z+dz)}; never
     *         {@code null}
     */
    @NotNull
    public BlockPos offset(int dx, int dy, int dz) {
        return new BlockPos(x + dx, y + dy, z + dz);
    }

    /**
     * Compares this position to {@code obj} for exact coordinate equality.
     *
     * <p>Two {@code BlockPos} instances are equal if and only if all three
     * integer coordinates are identical.
     *
     * @param obj the object to compare; may be {@code null}
     * @return {@code true} if {@code obj} is a {@code BlockPos} with the same
     *         X, Y, and Z coordinates; {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BlockPos(int x1, int y1, int z1))) {
            return false;
        }
        return x == x1 && y == y1 && z == z1;
    }

    /**
     * Packs this block position into a single {@code long} value.
     *
     * <p>The encoding reserves 26 bits for X, 12 bits for Y, and 26 bits for
     * Z, laid out as {@code [X:26][Y:12][Z:26]}.  The result is suitable as a
     * hash-map key wherever a single primitive key is preferred over a full
     * object comparison.
     *
     * <p>Coordinates outside the supported ranges produce undefined (wrapped)
     * values.
     *
     * @return a {@code long} uniquely identifying this position within the
     *         supported coordinate range
     */
    public long asLong() {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) y & 0xFFFL) << 26 | ((long) z & 0x3FFFFFFL);
    }

    /**
     * Returns a human-readable string representation of this block position.
     *
     * <p>The format is {@code BlockPos{x=<x>, y=<y>, z=<z>}}.
     *
     * @return a non-empty string describing the three coordinates; never
     *         {@code null}
     */
    @Override
    public @NotNull String toString() {
        return "BlockPos{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
