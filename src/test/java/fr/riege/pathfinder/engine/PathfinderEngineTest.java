package fr.riege.pathfinder.engine;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Direction;
import fr.riege.api.math.FluidType;
import fr.riege.api.path.PathResult;
import fr.riege.api.path.PathStatus;
import fr.riege.api.registry.MovementKeys;
import fr.riege.pathfinder.evaluator.IMovementEvaluator;
import fr.riege.pathfinder.evaluator.WalkEvaluator;
import fr.riege.pathfinder.heuristic.Euclidean3DHeuristic;
import fr.riege.pathfinder.registry.SimpleRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathfinderEngineTest {

    private static final int FLOOR_Y = 64;

    private IWorldLayer flatWorld() {
        return new IWorldLayer() {
            @Override
            public boolean isWalkable(BlockPos pos) {
                return pos.getY() == FLOOR_Y
                    && pos.getX() >= 0 && pos.getX() <= 19
                    && pos.getZ() >= 0 && pos.getZ() <= 19;
            }

            @Override
            public boolean isSolid(BlockPos pos) {
                return false;
            }

            @Override
            public FluidType getFluidType(BlockPos pos) {
                return FluidType.NONE;
            }

            @Override
            public int getLightLevel(BlockPos pos) {
                return 15;
            }
        };
    }

    private IBlockPhysicsLayer normalBlock() {
        return new IBlockPhysicsLayer() {
            @Override public float getSpeedMultiplier(BlockPos pos) { return 1.0f; }
            @Override public float getSlipperiness(BlockPos pos) { return 0.6f; }
            @Override public boolean isPassable(BlockPos pos) { return true; }
            @Override public double getStandingY(BlockPos pos) { return pos.getY(); }
            @Override public float getDragFactor(BlockPos pos) { return 1.0f; }
            @Override public float getBlockDamage(BlockPos pos) { return 0.0f; }
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
            public List<AABB> getCollisionBoxes(BlockPos pos) { return Collections.emptyList(); }
            @Override
            public boolean hasCollisionAt(AABB box) { return false; }
            @Override
            public double getMaxReach(BlockPos from, Direction dir, double hitboxHalf) { return 5.0; }
        };
    }

    private PathfinderContext buildContext() {
        IWorldLayer world = flatWorld();
        IBlockPhysicsLayer block = normalBlock();
        IEntityPhysicsLayer entity = standardEntity();
        ICollisionLayer collision = noCollision();

        SimpleRegistry<IMovementEvaluator> registry = new SimpleRegistry<>();
        registry.register(MovementKeys.WALK, new WalkEvaluator(world, block, entity));

        return new PathfinderContext(
            world, block, entity, collision,
            registry, new Euclidean3DHeuristic(),
            10000, 16
        );
    }

    @Test
    void reachablePath_statusIsFound() {
        PathfinderEngine engine = new PathfinderEngine();
        PathfinderContext ctx = buildContext();
        BlockPos from = new BlockPos(0, FLOOR_Y, 0);
        BlockPos to = new BlockPos(5, FLOOR_Y, 5);

        PathResult result = engine.compute(from, to, ctx);

        assertEquals(PathStatus.FOUND, result.getPath().getStatus());
    }

    @Test
    void computeWhileActive_previousCancelledNewSucceeds() {
        PathfinderEngine engine = new PathfinderEngine();
        PathfinderContext ctx = buildContext();
        BlockPos from = new BlockPos(0, FLOOR_Y, 0);
        BlockPos to = new BlockPos(5, FLOOR_Y, 5);

        // First compute completes normally (engine is synchronous, session clears after each call)
        PathResult first = engine.compute(from, to, ctx);
        assertEquals(PathStatus.FOUND, first.getPath().getStatus());
        assertFalse(engine.isRunning());

        // Second compute starts fresh — cancel() is called at start, no exception thrown
        PathResult second = engine.compute(from, to, ctx);
        assertEquals(PathStatus.FOUND, second.getPath().getStatus());
        assertFalse(engine.isRunning());
    }

    @Test
    void cancelWhenIdle_isRunningReturnsFalse() {
        PathfinderEngine engine = new PathfinderEngine();

        engine.cancel();

        assertFalse(engine.isRunning());
    }

    @Test
    void unreachableDestination_statusIsUnreachableOrTimeout() {
        PathfinderEngine engine = new PathfinderEngine();
        PathfinderContext ctx = buildContext();
        // Y=99 is outside the walkable area — no path can reach it
        BlockPos from = new BlockPos(0, FLOOR_Y, 0);
        BlockPos to = new BlockPos(0, 99, 0);

        PathResult result = engine.compute(from, to, ctx);
        PathStatus status = result.getPath().getStatus();

        assertTrue(
            status == PathStatus.UNREACHABLE || status == PathStatus.TIMEOUT,
            "Expected UNREACHABLE or TIMEOUT but got: " + status
        );
    }
}
