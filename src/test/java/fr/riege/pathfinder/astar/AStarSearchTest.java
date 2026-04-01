package fr.riege.pathfinder.astar;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Direction;
import fr.riege.api.math.FluidType;
import fr.riege.api.path.PathStatus;
import fr.riege.api.registry.MovementKeys;
import fr.riege.pathfinder.evaluator.FallEvaluator;
import fr.riege.pathfinder.evaluator.IMovementEvaluator;
import fr.riege.pathfinder.evaluator.JumpEvaluator;
import fr.riege.pathfinder.evaluator.WalkEvaluator;
import fr.riege.pathfinder.goal.BlockPosGoal;
import fr.riege.pathfinder.registry.OrderedRegistry;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AStarSearchTest {

    private static final int FLOOR_Y = 64;

    private IWorldLayer flatWorld(Set<BlockPos> walls) {
        return new IWorldLayer() {
            @Override
            public boolean isWalkable(@NonNull BlockPos pos) {
                if (walls.contains(pos)) return false;
                // Bounded 20x20 grid at floor Y
                return pos.y() == FLOOR_Y
                    && pos.x() >= -2 && pos.x() <= 15
                    && pos.z() >= -2 && pos.z() <= 15;
            }

            @Override
            public boolean isSolid(@NonNull BlockPos pos) {
                return walls.contains(pos);
            }

            @Override
            public @NonNull FluidType getFluidType(@NonNull BlockPos pos) {
                return FluidType.NONE;
            }

            @Override
            public int getLightLevel(@NonNull BlockPos pos) {
                return 15;
            }
        };
    }

    private IBlockPhysicsLayer normalBlock() {
        return new IBlockPhysicsLayer() {
            @Override public float getSpeedMultiplier(@NonNull BlockPos pos) { return 1.0f; }
            @Override public float getSlipperiness(@NonNull BlockPos pos) { return 0.6f; }
            @Override public boolean isPassable(@NonNull BlockPos pos) { return true; }
            @Override public double getStandingY(@NonNull BlockPos pos) { return pos.y(); }
            @Override public float getDragFactor(@NonNull BlockPos pos) { return 1.0f; }
            @Override public float getBlockDamage(@NonNull BlockPos pos) { return 0.0f; }
        };
    }

    private IEntityPhysicsLayer standardEntity() {
        return new IEntityPhysicsLayer() {
            @Override public double getHitboxWidth() { return 0.6; }
            @Override public double getHitboxHeight() { return 1.8; }
            @Override public double getStepHeight() { return 0.6; }
            @Override public double getJumpVelocity() { return 0.42; }
            @Override public float evaluateFallDamage(int blocks) { return Math.max(0, blocks - 3); }
            @Override public double getSwimSpeed() { return 0.2; }
            @Override public double getSprintMultiplier() { return 1.3; }
            @Override public double getSneakSpeedMultiplier() { return 0.3; }
        };
    }

    private ICollisionLayer noCollision() {
        return new ICollisionLayer() {
            @Override
            public @NonNull List<AABB> getCollisionBoxes(@NonNull BlockPos pos) { return Collections.emptyList(); }
            @Override
            public boolean hasCollisionAt(@NonNull AABB box) { return false; }
            @Override
            public double getMaxReach(@NonNull BlockPos from, @NonNull Direction dir, double hitboxHalf) { return 5.0; }
        };
    }

    private AStarSearch buildSearch(Set<BlockPos> walls) {
        IWorldLayer world = flatWorld(walls);
        IBlockPhysicsLayer block = normalBlock();
        IEntityPhysicsLayer entity = standardEntity();
        ICollisionLayer collision = noCollision();

        OrderedRegistry<IMovementEvaluator> registry = new OrderedRegistry<>();
        registry.register(MovementKeys.WALK, new WalkEvaluator(world, block, entity));
        registry.register(MovementKeys.JUMP, new JumpEvaluator(world, block, entity, collision));
        registry.register(MovementKeys.FALL, new FallEvaluator(world, entity));

        NodeGraph graph = new NodeGraph(registry);
        return new AStarSearch(graph, 10000);
    }

    @Test
    void straightPath_found() {
        AStarSearch search = buildSearch(Collections.emptySet());
        BlockPos start = new BlockPos(0, FLOOR_Y, 0);
        BlockPos goalPos = new BlockPos(4, FLOOR_Y, 0);
        List<BlockPos> path = search.search(start, new BlockPosGoal(goalPos));

        assertEquals(PathStatus.FOUND, search.getLastStatus());
        assertFalse(path.isEmpty());
        assertEquals(goalPos, path.get(path.size() - 1));
    }

    @Test
    void startEqualsGoal_returnsOneNode() {
        AStarSearch search = buildSearch(Collections.emptySet());
        BlockPos start = new BlockPos(2, FLOOR_Y, 2);
        List<BlockPos> path = search.search(start, new BlockPosGoal(start));

        assertEquals(PathStatus.FOUND, search.getLastStatus());
        assertEquals(1, path.size());
        assertEquals(start, path.get(0));
    }

    @Test
    void unreachableGoal_returnsUnreachable() {
        // Goal is surrounded by walls — no walkable neighbors exist leading to it
        BlockPos goalPos = new BlockPos(5, FLOOR_Y, 5);
        Set<BlockPos> walls = Set.of(
            new BlockPos(4, FLOOR_Y, 5),
            new BlockPos(6, FLOOR_Y, 5),
            new BlockPos(5, FLOOR_Y, 4),
            new BlockPos(5, FLOOR_Y, 6),
            new BlockPos(4, FLOOR_Y, 4),
            new BlockPos(6, FLOOR_Y, 4),
            new BlockPos(4, FLOOR_Y, 6),
            new BlockPos(6, FLOOR_Y, 6)
        );
        AStarSearch search = buildSearch(walls);
        BlockPos start = new BlockPos(0, FLOOR_Y, 0);
        List<BlockPos> path = search.search(start, new BlockPosGoal(goalPos));

        assertEquals(PathStatus.UNREACHABLE, search.getLastStatus());
        assertTrue(path.isEmpty());
    }
}
