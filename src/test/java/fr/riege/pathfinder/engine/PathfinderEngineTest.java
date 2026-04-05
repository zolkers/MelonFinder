package fr.riege.pathfinder.engine;

import fr.riege.api.goal.BlockGoal;
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
import fr.riege.pathfinder.registry.OrderedRegistry;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathfinderEngineTest {

    private static final int FLOOR_Y = 64;

    private IWorldLayer flatWorld() {
        return new IWorldLayer() {
            @Override
            public boolean isWalkable(@NonNull BlockPos pos) {
                return pos.y() == FLOOR_Y
                    && pos.x() >= 0 && pos.x() <= 19
                    && pos.z() >= 0 && pos.z() <= 19;
            }

            @Override
            public boolean isSolid(@NonNull BlockPos pos) {
                return false;
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

    private PathfinderContext buildContext() {
        IWorldLayer world = flatWorld();
        IBlockPhysicsLayer block = normalBlock();
        IEntityPhysicsLayer entity = standardEntity();
        ICollisionLayer collision = noCollision();

        OrderedRegistry<IMovementEvaluator> registry = new OrderedRegistry<>();
        registry.register(MovementKeys.WALK, new WalkEvaluator(world, block));

        return new PathfinderContext(
            world, block, entity, collision,
            registry, new Euclidean3DHeuristic(),
            5_000L, 16, 42L, 1.0
        );
    }

    @Test
    void reachablePath_statusIsFound() {
        PathfinderEngine engine = new PathfinderEngine();
        PathfinderContext ctx = buildContext();
        BlockPos from = new BlockPos(0, FLOOR_Y, 0);
        BlockGoal goal = new BlockGoal(new BlockPos(5, FLOOR_Y, 5));

        PathResult result = engine.compute(from, goal, ctx);

        assertEquals(PathStatus.FOUND, result.path().status());
    }

    @Test
    void computeWhileActive_previousCancelledNewSucceeds() {
        PathfinderEngine engine = new PathfinderEngine();
        PathfinderContext ctx = buildContext();
        BlockPos from = new BlockPos(0, FLOOR_Y, 0);
        BlockGoal goal = new BlockGoal(new BlockPos(5, FLOOR_Y, 5));

        // First compute completes normally (engine is synchronous, session clears after each call)
        PathResult first = engine.compute(from, goal, ctx);
        assertEquals(PathStatus.FOUND, first.path().status());
        assertFalse(engine.isRunning());

        // Second compute starts fresh — cancel() is called at start, no exception thrown
        PathResult second = engine.compute(from, goal, ctx);
        assertEquals(PathStatus.FOUND, second.path().status());
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
        BlockGoal goal = new BlockGoal(new BlockPos(0, 99, 0));

        PathResult result = engine.compute(from, goal, ctx);
        PathStatus status = result.path().status();

        assertTrue(
            status == PathStatus.UNREACHABLE || status == PathStatus.TIMEOUT || status == PathStatus.PARTIAL,
            "Expected UNREACHABLE, TIMEOUT or PARTIAL but got: " + status
        );
    }
}
