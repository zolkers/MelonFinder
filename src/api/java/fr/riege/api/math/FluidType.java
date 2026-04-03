package fr.riege.api.math;

/**
 * Enumerates the categories of fluid that can occupy a block position.
 *
 * <p>The pathfinder queries {@link fr.riege.api.layer.IWorldLayer#getFluidType(BlockPos)}
 * to determine which movement evaluator should handle a fluid-containing block.
 * Using an enum rather than Minecraft fluid classes keeps the {@code api} and
 * {@code pathfinder} modules completely free of game-engine imports.
 *
 * <h2>Extension note</h2>
 * <p>This enum is intentionally minimal.  If future Minecraft versions introduce
 * additional fluid types (e.g. mud or powder snow behaving as fluids), new
 * constants should be added here along with corresponding evaluators in the
 * {@code pathfinder} module.
 *
 * @see fr.riege.api.layer.IWorldLayer#getFluidType(BlockPos)
 */
public enum FluidType {

    /**
     * No fluid is present at the queried position.
     *
     * <p>This is the value returned for all dry blocks: air, solid blocks,
     * and any block whose fluid level is zero.
     */
    NONE,

    /**
     * The position contains water (or a water-logged block).
     *
     * <p>Entities with a swim speed can navigate through water blocks.
     * The pathfinder selects the swim evaluator for nodes whose fluid type is
     * {@code WATER}.
     */
    WATER,

    /**
     * The position contains lava.
     *
     * <p>Lava is treated as impassable for non-fireproof entities.  The
     * pathfinder marks lava positions as unreachable unless the entity has
     * fire-immunity, in which case a configurable high-cost penalty applies.
     */
    LAVA
}
