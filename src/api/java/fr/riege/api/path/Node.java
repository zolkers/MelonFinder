package fr.riege.api.path;

import fr.riege.api.math.BlockPos;

/**
 * An immutable snapshot of a single A* search node as it was at the moment
 * the path was finalised.
 *
 * <p>Each {@code Node} records the block position the search visited, the type
 * of movement used to arrive there, and the two cost components computed by
 * the A* algorithm at that position.  Nodes are assembled into a {@link Path}
 * by grouping them into {@link Segment} objects after the search completes.
 *
 * <h2>Cost semantics</h2>
 * <p>The A* algorithm assigns two costs to every explored position:
 * <ul>
 *   <li><strong>g-cost</strong> — the actual accumulated movement cost from
 *       the start node to this node along the path taken so far.</li>
 *   <li><strong>h-cost</strong> — the heuristic estimate of the remaining
 *       cost from this node to the goal (typically the Euclidean distance to
 *       the target block).</li>
 * </ul>
 * The f-cost is the sum of both and represents the algorithm's estimate of
 * the total path cost through this node.
 *
 * @param pos          the integer block coordinate of this node; the player's
 *                     feet are at this Y coordinate; never {@code null}
 * @param movementType the kind of movement used to reach this node from its
 *                     predecessor; never {@code null}
 * @param gCost        the accumulated movement cost from the start to this
 *                     node; always {@code >= 0.0}
 * @param hCost        the heuristic cost estimate from this node to the goal;
 *                     always {@code >= 0.0}
 *
 * @see Path
 * @see Segment
 * @see MovementType
 */
public record Node(BlockPos pos, MovementType movementType, double gCost, double hCost) {

    /**
     * Returns the f-cost of this node, defined as the sum of the g-cost and
     * the h-cost.
     *
     * <p>The f-cost is the primary ordering criterion for the A* open set: the
     * node with the lowest f-cost is expanded next.  A lower f-cost means the
     * algorithm considers this node part of a more promising partial path.
     *
     * @return {@code gCost + hCost}; always {@code >= 0.0}
     */
    public double fCost() {
        return gCost + hCost;
    }
}
