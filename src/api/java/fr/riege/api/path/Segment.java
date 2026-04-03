package fr.riege.api.path;

import fr.riege.api.math.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An immutable section of a {@link Path} that connects two sub-block positions
 * with a list of A* nodes.
 *
 * <p>A segment is the unit of geometry consumed by the path renderer and the
 * future path executor.  Each segment has a precise floating-point start and
 * end position in world space (the result of sub-block sampling and smoothing)
 * and an ordered list of {@link Node} objects corresponding to the A* nodes
 * whose block coordinates fall within this section.
 *
 * <h2>Dense segments</h2>
 * <p>After the Catmull-Rom smoothing stage the path assembler creates one
 * segment per consecutive {@link Vec3} pair in the dense output list.  A
 * typical path with twelve A* waypoints and eight Catmull-Rom sub-samples per
 * gap therefore produces approximately 88 segments.  The {@link #start()} and
 * {@link #end()} vectors reflect the exact smoothed world-space positions, not
 * the centres of the underlying block nodes.
 *
 * <h2>Defensive copy</h2>
 * <p>The compact constructor wraps the supplied node list with
 * {@link List#copyOf} so that external modifications to the original list do
 * not affect the segment after construction.
 *
 * @param nodes  the ordered list of A* nodes associated with this segment;
 *               copied on construction; never {@code null}
 * @param start  the sub-block world-space start position of this segment;
 *               never {@code null}
 * @param end    the sub-block world-space end position of this segment;
 *               never {@code null}
 * @param length the Euclidean distance between {@code start} and {@code end},
 *               in blocks; always {@code >= 0.0}
 *
 * @see Path
 * @see Node
 * @see Vec3
 */
public record Segment(List<Node> nodes, Vec3 start, Vec3 end, double length) {

    /**
     * Constructs a {@code Segment} with a defensive copy of the node list.
     *
     * <p>The supplied {@code nodes} list is copied via {@link List#copyOf};
     * the resulting internal list is unmodifiable.  All other parameters are
     * stored as-is.
     *
     * @param nodes  the A* nodes that make up this segment; must not be
     *               {@code null}; the list is copied, so later modifications
     *               to the original have no effect
     * @param start  the world-space starting position of this segment; must
     *               not be {@code null}
     * @param end    the world-space ending position of this segment; must not
     *               be {@code null}
     * @param length the Euclidean distance from {@code start} to {@code end},
     *               in blocks; must be {@code >= 0.0}
     */
    public Segment(@NotNull List<Node> nodes, @NotNull Vec3 start, @NotNull Vec3 end, double length) {
        this.nodes = List.copyOf(nodes);
        this.start = start;
        this.end = end;
        this.length = length;
    }
}
