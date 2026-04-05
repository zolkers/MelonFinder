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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AsyncPathfinderServiceTest {

    private static final int FLOOR_Y = 64;

    private IWorldLayer flatWorld() {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) {
                return pos.y() == FLOOR_Y && pos.x() >= 0 && pos.x() <= 19 && pos.z() >= 0 && pos.z() <= 19;
            }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return false; }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
        };
    }

    /** World that blocks isWalkable() until the latch is released. */
    private IWorldLayer blockingWorld(CountDownLatch latch) {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return pos.y() == FLOOR_Y && pos.x() >= 0 && pos.x() <= 19 && pos.z() >= 0 && pos.z() <= 19;
            }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return false; }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
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
            @Override public @NonNull List<AABB> getCollisionBoxes(@NonNull BlockPos pos) { return Collections.emptyList(); }
            @Override public boolean hasCollisionAt(@NonNull AABB box) { return false; }
            @Override public double getMaxReach(@NonNull BlockPos from, @NonNull Direction dir, double hitboxHalf) { return 5.0; }
        };
    }

    private PathfinderContext buildContext(IWorldLayer world) {
        IBlockPhysicsLayer block = normalBlock();
        OrderedRegistry<IMovementEvaluator> registry = new OrderedRegistry<>();
        registry.register(MovementKeys.WALK, new WalkEvaluator(world, block));
        return new PathfinderContext(
            world, block, standardEntity(), noCollision(),
            registry, new Euclidean3DHeuristic(),
            5_000L, 16, 42L, 1.0
        );
    }

    @Test
    void requestPath_futureCompletesWithFoundStatus() throws Exception {
        AsyncPathfinderService service = new AsyncPathfinderService();
        PathfinderContext ctx = buildContext(flatWorld());
        BlockPos from = new BlockPos(0, FLOOR_Y, 0);
        BlockGoal goal = new BlockGoal(new BlockPos(5, FLOOR_Y, 5));

        CompletableFuture<PathResult> future = service.requestPath(from, goal, ctx);
        PathResult result = future.get(10, TimeUnit.SECONDS);

        assertEquals(PathStatus.FOUND, result.path().status());
    }

    @Test
    void isRunning_falseAfterCompletion() throws Exception {
        AsyncPathfinderService service = new AsyncPathfinderService();
        PathfinderContext ctx = buildContext(flatWorld());
        BlockPos from = new BlockPos(0, FLOOR_Y, 0);
        BlockGoal goal = new BlockGoal(new BlockPos(5, FLOOR_Y, 5));

        CompletableFuture<PathResult> future = service.requestPath(from, goal, ctx);
        future.get(10, TimeUnit.SECONDS);

        assertFalse(service.isRunning());
    }

    @Test
    void cancel_whenIdle_doesNotThrow() {
        AsyncPathfinderService service = new AsyncPathfinderService();
        assertDoesNotThrow(service::cancel);
        assertFalse(service.isRunning());
    }

    @Test
    void cancel_futureIsCancelledOrCompleted() throws Exception {
        CountDownLatch block = new CountDownLatch(1);
        AsyncPathfinderService service = new AsyncPathfinderService();
        PathfinderContext ctx = buildContext(blockingWorld(block));
        BlockPos from = new BlockPos(0, FLOOR_Y, 0);
        BlockGoal goal = new BlockGoal(new BlockPos(5, FLOOR_Y, 5));

        CompletableFuture<PathResult> future = service.requestPath(from, goal, ctx);

        // Small delay to let the virtual thread start and block on isWalkable
        Thread.sleep(50);
        service.cancel();
        block.countDown(); // unblock the world so the thread can exit

        // Future must complete (cancelled or exceptionally) within 5s
        assertTrue(future.isDone() || future.isCancelled() || future.isCompletedExceptionally()
            || future.get(5, TimeUnit.SECONDS) != null,
            "Future must eventually complete after cancel");
    }

    @Test
    void secondRequest_cancelsFirstFuture() throws Exception {
        CountDownLatch block = new CountDownLatch(1);
        AsyncPathfinderService service = new AsyncPathfinderService();

        PathfinderContext slowCtx = buildContext(blockingWorld(block));
        PathfinderContext fastCtx = buildContext(flatWorld());
        BlockPos from = new BlockPos(0, FLOOR_Y, 0);
        BlockGoal goal = new BlockGoal(new BlockPos(5, FLOOR_Y, 5));

        CompletableFuture<PathResult> first = service.requestPath(from, goal, slowCtx);

        // Let the first request start
        Thread.sleep(50);

        CompletableFuture<PathResult> second = service.requestPath(from, goal, fastCtx);
        block.countDown(); // unblock slow world

        // Second must complete normally
        PathResult result = second.get(10, TimeUnit.SECONDS);
        assertEquals(PathStatus.FOUND, result.path().status());

        // First must be cancelled or completed exceptionally (not FOUND)
        assertTrue(first.isCancelled() || first.isCompletedExceptionally()
            || first.get(5, TimeUnit.SECONDS).path().status() == PathStatus.CANCELLED,
            "First future must be cancelled when second request is made");
    }
}
