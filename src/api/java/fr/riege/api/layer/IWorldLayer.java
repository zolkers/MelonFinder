package fr.riege.api.layer;

import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import org.jetbrains.annotations.NotNull;

/**
 * Provides high-level queries about the logical state of the game world.
 *
 * <p>This interface offers block-level predicates that the pathfinder needs in
 * order to determine which positions are navigable.  Unlike
 * {@link IBlockPhysicsLayer}, which returns continuous numeric values, this
 * interface answers boolean and categorical questions: can the entity stand
 * here? is there a block here? what fluid is present?
 *
 * <p>Implementations live exclusively in the {@code layer} source set and
 * translate Minecraft block states into these engine-neutral answers.  No
 * class in {@code pathfinder} or {@code api} may query Minecraft directly.
 *
 * <h2>Walkability vs. solidity</h2>
 * <p>{@link #isWalkable(BlockPos)} and {@link #isSolid(BlockPos)} are related
 * but not inverse to each other.  A block can be solid without being a
 * valid standing surface (e.g. a block inside a wall).  A block can also be
 * non-solid but still not walkable (e.g. a block above a large void where the
 * entity would fall immediately).  Both predicates are needed for complete
 * navigation.
 *
 * @see IBlockPhysicsLayer
 * @see ICollisionLayer
 * @see IEntityPhysicsLayer
 */
public interface IWorldLayer {

    /**
     * Returns whether an entity can walk on (stand on top of) the block at
     * {@code pos}.
     *
     * <p>A walkable position requires:
     * <ol>
     *   <li>A solid or semi-solid block at {@code pos} that provides a
     *       standing surface.</li>
     *   <li>At least two blocks of vertical clearance above {@code pos} for
     *       the entity to occupy.</li>
     * </ol>
     *
     * <p>Positions in mid-air, inside solid blocks, or with insufficient
     * overhead clearance return {@code false}.
     *
     * @param pos the block position to evaluate; must not be {@code null}
     * @return {@code true} if an entity can stand at this position;
     *         {@code false} otherwise
     */
    boolean isWalkable(@NotNull BlockPos pos);

    /**
     * Returns whether the block at {@code pos} is solid, meaning it presents
     * a physical face that prevents entities from passing through.
     *
     * <p>Solid blocks include full cubes, stairs, slabs, and similar blocks
     * whose collision shape occupies at least part of the block volume.  Air,
     * tall grass, signs, and banners are not solid.
     *
     * @param pos the block position to query; must not be {@code null}
     * @return {@code true} if the block is solid; {@code false} if the block
     *         is passable or has no collision shape
     */
    boolean isSolid(@NotNull BlockPos pos);

    /**
     * Returns the type of fluid occupying the block at {@code pos}.
     *
     * <p>If the block is not a fluid, or the fluid level is zero, this method
     * returns {@link FluidType#NONE}.  The pathfinder uses this value to
     * select the appropriate movement evaluator (swim, avoid-lava, etc.) for
     * blocks that contain fluid.
     *
     * @param pos the block position to query; must not be {@code null}
     * @return the fluid type present at the position; never {@code null};
     *         {@link FluidType#NONE} if no fluid is present
     */
    @NotNull FluidType getFluidType(@NotNull BlockPos pos);

    /**
     * Returns the internal light level at the block at {@code pos}.
     *
     * <p>Light levels in vanilla Minecraft range from 0 (complete darkness)
     * to 15 (maximum brightness).  The combined light level (the greater of
     * sky light and block light) is used by the pathfinder for optional
     * lighting-aware cost adjustments, such as preferring well-lit routes at
     * night to avoid mob spawns.
     *
     * @param pos the block position to query; must not be {@code null}
     * @return the light level at the position; in the range {@code [0, 15]}
     */
    int getLightLevel(@NotNull BlockPos pos);

    /**
     * Returns whether an entity can move through the block at {@code pos}
     * without physical obstruction.
     *
     * <p>The default implementation is the logical negation of
     * {@link #isSolid(BlockPos)}: a block is passable if and only if it is
     * not solid.  Implementations may override this method for blocks that
     * present no collision shape but are still logically impassable (e.g.
     * fluid blocks that the entity cannot swim through).
     *
     * @param pos the block position to query; must not be {@code null}
     * @return {@code true} if the block allows free passage; {@code false}
     *         if it blocks movement
     */
    default boolean isPassable(@NotNull BlockPos pos) {
        return !isSolid(pos);
    }
}
