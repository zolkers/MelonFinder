package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClimbEvaluatorTest {

    private static final BlockPos FROM = new BlockPos(0, 64, 0);
    private static final BlockPos TO_UP = new BlockPos(0, 65, 0);
    private static final BlockPos TO_SIDE = new BlockPos(1, 65, 0);

    private IBlockPhysicsLayer nonPassableBlock() {
        return new IBlockPhysicsLayer() {
            @Override public float getSpeedMultiplier(BlockPos pos) { return 1.0f; }
            @Override public float getSlipperiness(BlockPos pos) { return 0.6f; }
            @Override public boolean isPassable(BlockPos pos) { return false; }
            @Override public double getStandingY(BlockPos pos) { return pos.getY(); }
            @Override public float getDragFactor(BlockPos pos) { return 1.0f; }
            @Override public float getBlockDamage(BlockPos pos) { return 0.0f; }
        };
    }

    private IBlockPhysicsLayer passableBlock() {
        return new IBlockPhysicsLayer() {
            @Override public float getSpeedMultiplier(BlockPos pos) { return 1.0f; }
            @Override public float getSlipperiness(BlockPos pos) { return 0.6f; }
            @Override public boolean isPassable(BlockPos pos) { return true; }
            @Override public double getStandingY(BlockPos pos) { return pos.getY(); }
            @Override public float getDragFactor(BlockPos pos) { return 1.0f; }
            @Override public float getBlockDamage(BlockPos pos) { return 0.0f; }
        };
    }

    @Test
    void verticalMove_nonPassableBlock_isPossible() {
        ClimbEvaluator evaluator = new ClimbEvaluator(nonPassableBlock());
        MovementResult result = evaluator.evaluate(FROM, TO_UP);
        assertTrue(result.isPossible());
        assertTrue(result.getCost() > 0.0);
    }

    @Test
    void diagonalMove_isImpossible() {
        ClimbEvaluator evaluator = new ClimbEvaluator(nonPassableBlock());
        assertFalse(evaluator.evaluate(FROM, TO_SIDE).isPossible());
    }

    @Test
    void verticalMove_passableBlock_isImpossible() {
        ClimbEvaluator evaluator = new ClimbEvaluator(passableBlock());
        assertFalse(evaluator.evaluate(FROM, TO_UP).isPossible());
    }
}
