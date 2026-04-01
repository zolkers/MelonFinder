package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WalkEvaluatorTest {

    private static final BlockPos FROM = new BlockPos(0, 64, 0);
    private static final BlockPos TO = new BlockPos(1, 64, 0);
    private static final BlockPos HEAD = new BlockPos(1, 66, 0);

    private IWorldLayer walkableWorld(boolean toWalkable, boolean headSolid) {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) {
                return pos.equals(TO) ? toWalkable : false;
            }
            @Override public boolean isSolid(@NonNull BlockPos pos) {
                return pos.equals(HEAD) && headSolid;
            }
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
    void flatWalkableSurface_isPossibleWithPositiveCost() {
        WalkEvaluator evaluator = new WalkEvaluator(walkableWorld(true, false), normalBlock(), standardEntity());
        MovementResult result = evaluator.evaluate(FROM, TO);
        assertTrue(result.isPossible());
        assertTrue(result.getCost() > 0.0);
    }

    @Test
    void notWalkableDestination_isImpossible() {
        WalkEvaluator evaluator = new WalkEvaluator(walkableWorld(false, false), normalBlock(), standardEntity());
        MovementResult result = evaluator.evaluate(FROM, TO);
        assertFalse(result.isPossible());
    }

    @Test
    void noHeadroom_isImpossible() {
        WalkEvaluator evaluator = new WalkEvaluator(walkableWorld(true, true), normalBlock(), standardEntity());
        MovementResult result = evaluator.evaluate(FROM, TO);
        assertFalse(result.isPossible());
    }

    @Test
    void soulSandSpeed_costHigherThanNormal() {
        IBlockPhysicsLayer soulSandBlock = new IBlockPhysicsLayer() {
            @Override public float getSpeedMultiplier(@NonNull BlockPos pos) { return 0.4f; }
            @Override public float getSlipperiness(@NonNull BlockPos pos) { return 0.6f; }
            @Override public boolean isPassable(@NonNull BlockPos pos) { return true; }
            @Override public double getStandingY(@NonNull BlockPos pos) { return pos.y(); }
            @Override public float getDragFactor(@NonNull BlockPos pos) { return 1.0f; }
            @Override public float getBlockDamage(@NonNull BlockPos pos) { return 0.0f; }
        };
        WalkEvaluator normalEval = new WalkEvaluator(walkableWorld(true, false), normalBlock(), standardEntity());
        WalkEvaluator soulSandEval = new WalkEvaluator(walkableWorld(true, false), soulSandBlock, standardEntity());
        double normalCost = normalEval.evaluate(FROM, TO).getCost();
        double soulSandCost = soulSandEval.evaluate(FROM, TO).getCost();
        assertTrue(soulSandCost > normalCost);
    }
}
