package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SwimEvaluatorTest {

    private static final BlockPos FROM = new BlockPos(0, 64, 0);
    private static final BlockPos TO = new BlockPos(1, 64, 0);

    private IWorldLayer worldWithFluid(FluidType fluidType) {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return true; }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return false; }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return fluidType; }
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
    void inWater_isPossible() {
        SwimEvaluator evaluator = new SwimEvaluator(worldWithFluid(FluidType.WATER), normalBlock(), standardEntity());
        MovementResult result = evaluator.evaluate(FROM, TO);
        assertTrue(result.isPossible());
        assertTrue(result.getCost() > 0.0);
    }

    @Test
    void noFluid_isImpossible() {
        SwimEvaluator evaluator = new SwimEvaluator(worldWithFluid(FluidType.NONE), normalBlock(), standardEntity());
        assertFalse(evaluator.evaluate(FROM, TO).isPossible());
    }

    @Test
    void swimSpeedReducesCost() {
        IEntityPhysicsLayer fastSwimmer = new IEntityPhysicsLayer() {
            @Override public double getHitboxWidth() { return 0.6; }
            @Override public double getHitboxHeight() { return 1.8; }
            @Override public double getStepHeight() { return 0.6; }
            @Override public double getJumpVelocity() { return 0.42; }
            @Override public float evaluateFallDamage(int blocks) { return 0; }
            @Override public double getSwimSpeed() { return 1.0; }
            @Override public double getSprintMultiplier() { return 1.3; }
            @Override public double getSneakSpeedMultiplier() { return 0.3; }
        };
        SwimEvaluator slow = new SwimEvaluator(worldWithFluid(FluidType.WATER), normalBlock(), standardEntity());
        SwimEvaluator fast = new SwimEvaluator(worldWithFluid(FluidType.WATER), normalBlock(), fastSwimmer);
        assertTrue(fast.evaluate(FROM, TO).getCost() < slow.evaluate(FROM, TO).getCost());
    }
}
