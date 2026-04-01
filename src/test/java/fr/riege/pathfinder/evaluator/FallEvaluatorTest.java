package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FallEvaluatorTest {

    private static final BlockPos FROM = new BlockPos(0, 70, 0);
    private static final BlockPos TO_DOWN = new BlockPos(0, 64, 0);
    private static final BlockPos TO_SAME = new BlockPos(0, 70, 0);

    private IWorldLayer walkableWorld() {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return true; }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return false; }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
        };
    }

    private IWorldLayer nonWalkableWorld() {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return false; }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return false; }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
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

    private IEntityPhysicsLayer lethalFallEntity() {
        return new IEntityPhysicsLayer() {
            @Override public double getHitboxWidth() { return 0.6; }
            @Override public double getHitboxHeight() { return 1.8; }
            @Override public double getStepHeight() { return 0.6; }
            @Override public double getJumpVelocity() { return 0.42; }
            @Override public float evaluateFallDamage(int blocks) { return 20.0f; }
            @Override public double getSwimSpeed() { return 0.2; }
            @Override public double getSprintMultiplier() { return 1.3; }
            @Override public double getSneakSpeedMultiplier() { return 0.3; }
        };
    }

    @Test
    void validFall_isPossible() {
        FallEvaluator evaluator = new FallEvaluator(walkableWorld(), standardEntity());
        MovementResult result = evaluator.evaluate(FROM, TO_DOWN);
        assertTrue(result.isPossible());
        assertTrue(result.getCost() > 0.0);
    }

    @Test
    void sameHeight_isImpossible() {
        FallEvaluator evaluator = new FallEvaluator(walkableWorld(), standardEntity());
        assertFalse(evaluator.evaluate(FROM, TO_SAME).isPossible());
    }

    @Test
    void destinationNotWalkable_isImpossible() {
        FallEvaluator evaluator = new FallEvaluator(nonWalkableWorld(), standardEntity());
        assertFalse(evaluator.evaluate(FROM, TO_DOWN).isPossible());
    }

    @Test
    void lethalFall_isImpossible() {
        FallEvaluator evaluator = new FallEvaluator(walkableWorld(), lethalFallEntity());
        assertFalse(evaluator.evaluate(FROM, TO_DOWN).isPossible());
    }
}
