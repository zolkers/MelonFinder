package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Direction;
import fr.riege.api.math.FluidType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JumpEvaluatorTest {

    private static final BlockPos FROM = new BlockPos(0, 64, 0);
    private static final BlockPos TO_UP = new BlockPos(1, 65, 0);
    private static final BlockPos TO_SAME = new BlockPos(1, 64, 0);
    private static final BlockPos TO_UP2 = new BlockPos(1, 66, 0);

    private IWorldLayer worldWith(boolean toWalkable, boolean headBlocked) {
        BlockPos headPos = new BlockPos(FROM.x(), FROM.y() + 2, FROM.z());
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return toWalkable; }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return pos.equals(headPos) && headBlocked; }
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

    private ICollisionLayer emptyCollision() {
        return new ICollisionLayer() {
            @Override public @NonNull List<AABB> getCollisionBoxes(@NonNull BlockPos pos) { return List.of(); }
            @Override public boolean hasCollisionAt(@NonNull AABB box) { return false; }
            @Override public double getMaxReach(@NonNull BlockPos from, @NonNull Direction dir, double hitboxHalf) { return 0; }
        };
    }

    @Test
    void validJump_isPossible() {
        JumpEvaluator evaluator = new JumpEvaluator(worldWith(true, false), normalBlock(), standardEntity(), emptyCollision());
        MovementResult result = evaluator.evaluate(FROM, TO_UP);
        assertTrue(result.isPossible());
        assertTrue(result.getCost() > 0.0);
    }

    @Test
    void sameHeightMove_isImpossible() {
        JumpEvaluator evaluator = new JumpEvaluator(worldWith(true, false), normalBlock(), standardEntity(), emptyCollision());
        assertFalse(evaluator.evaluate(FROM, TO_SAME).isPossible());
    }

    @Test
    void tooHighJump_isImpossible() {
        JumpEvaluator evaluator = new JumpEvaluator(worldWith(true, false), normalBlock(), standardEntity(), emptyCollision());
        assertFalse(evaluator.evaluate(FROM, TO_UP2).isPossible());
    }

    @Test
    void noHeadClearance_isImpossible() {
        JumpEvaluator evaluator = new JumpEvaluator(worldWith(true, true), normalBlock(), standardEntity(), emptyCollision());
        assertFalse(evaluator.evaluate(FROM, TO_UP).isPossible());
    }

    @Test
    void destinationNotWalkable_isImpossible() {
        JumpEvaluator evaluator = new JumpEvaluator(worldWith(false, false), normalBlock(), standardEntity(), emptyCollision());
        assertFalse(evaluator.evaluate(FROM, TO_UP).isPossible());
    }
}
