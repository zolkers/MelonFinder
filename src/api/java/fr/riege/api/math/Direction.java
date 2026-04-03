package fr.riege.api.math;

/**
 * Represents one of the six axis-aligned cardinal directions in three-dimensional
 * block space.
 *
 * <p>Each constant carries its unit integer offset along each world axis, allowing
 * callers to translate a {@link BlockPos} by one block in the given direction
 * without any arithmetic outside this enum.
 *
 * <h2>Coordinate system</h2>
 * <p>The offsets follow the standard Minecraft world coordinate system:
 * <ul>
 *   <li>The <strong>X</strong> axis runs west (negative) to east (positive).</li>
 *   <li>The <strong>Y</strong> axis runs downward (negative) to upward (positive).</li>
 *   <li>The <strong>Z</strong> axis runs north (negative) to south (positive).</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * BlockPos neighbor = pos.offset(
 *     Direction.NORTH.getOffsetX(),
 *     Direction.NORTH.getOffsetY(),
 *     Direction.NORTH.getOffsetZ()
 * );
 * }</pre>
 */
public enum Direction {

    /**
     * The negative-Z direction (decreasing block Z coordinate).
     */
    NORTH(0, 0, -1),

    /**
     * The positive-Z direction (increasing block Z coordinate).
     */
    SOUTH(0, 0, 1),

    /**
     * The positive-X direction (increasing block X coordinate).
     */
    EAST(1, 0, 0),

    /**
     * The negative-X direction (decreasing block X coordinate).
     */
    WEST(-1, 0, 0),

    /**
     * The positive-Y direction (increasing block Y coordinate; upward).
     */
    UP(0, 1, 0),

    /**
     * The negative-Y direction (decreasing block Y coordinate; downward).
     */
    DOWN(0, -1, 0);

    private final int offsetX;
    private final int offsetY;
    private final int offsetZ;

    /**
     * Constructs a direction constant with the given unit block offsets.
     *
     * @param offsetX the displacement along the X axis; {@code -1}, {@code 0},
     *                or {@code 1}
     * @param offsetY the displacement along the Y axis; {@code -1}, {@code 0},
     *                or {@code 1}
     * @param offsetZ the displacement along the Z axis; {@code -1}, {@code 0},
     *                or {@code 1}
     */
    Direction(int offsetX, int offsetY, int offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    /**
     * Returns the displacement of this direction along the X axis.
     *
     * @return {@code -1} for {@link #WEST}, {@code 1} for {@link #EAST},
     *         or {@code 0} for all other directions
     */
    public int getOffsetX() {
        return offsetX;
    }

    /**
     * Returns the displacement of this direction along the Y axis.
     *
     * @return {@code 1} for {@link #UP}, {@code -1} for {@link #DOWN},
     *         or {@code 0} for all horizontal directions
     */
    public int getOffsetY() {
        return offsetY;
    }

    /**
     * Returns the displacement of this direction along the Z axis.
     *
     * @return {@code -1} for {@link #NORTH}, {@code 1} for {@link #SOUTH},
     *         or {@code 0} for all other directions
     */
    public int getOffsetZ() {
        return offsetZ;
    }
}
