package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SneakEvaluatorTest {

    private static final BlockPos FROM = new BlockPos(0, 64, 0);
    private static final BlockPos TO = new BlockPos(1, 64, 0);

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

    @Test
    void walkableSurface_isPossibleAndMoreExpensiveThanWalk() {
        SneakEvaluator sneakEval = new SneakEvaluator(walkableWorld(), normalBlock(), standardEntity());
        WalkEvaluator walkEval = new WalkEvaluator(walkableWorld(), normalBlock());
        MovementResult sneakResult = sneakEval.evaluate(FROM, TO);
        MovementResult walkResult = walkEval.evaluate(FROM, TO);
        assertTrue(sneakResult.isPossible());
        assertTrue(sneakResult.getCost() > walkResult.getCost());
    }

    @Test
    void nonWalkable_isImpossible() {
        SneakEvaluator evaluator = new SneakEvaluator(nonWalkableWorld(), normalBlock(), standardEntity());
        assertFalse(evaluator.evaluate(FROM, TO).isPossible());
    }
}
