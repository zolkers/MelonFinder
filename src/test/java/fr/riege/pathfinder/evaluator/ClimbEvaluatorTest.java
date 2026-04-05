package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClimbEvaluatorTest {

    private static final BlockPos FROM  = new BlockPos(0, 64, 0);
    private static final BlockPos TO_UP = new BlockPos(0, 65, 0);
    private static final BlockPos TO_DN = new BlockPos(0, 63, 0);
    private static final BlockPos TO_SIDE = new BlockPos(1, 65, 0);

    private IWorldLayer climbableAt(BlockPos climbable, boolean destinationPassable) {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return false; }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return !destinationPassable && pos.equals(TO_UP); }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
            @Override public boolean isClimbable(@NonNull BlockPos pos) { return pos.equals(climbable); }
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

    @Test
    void climbUp_onClimbableBlock_isPossible() {
        ClimbEvaluator evaluator = new ClimbEvaluator(climbableAt(FROM, true));
        MovementResult result = evaluator.evaluate(FROM, TO_UP);
        assertTrue(result.isPossible());
        assertTrue(result.getCost() > 0.0);
    }

    @Test
    void climbDown_onClimbableBlock_isPossible() {
        ClimbEvaluator evaluator = new ClimbEvaluator(climbableAt(FROM, true));
        MovementResult result = evaluator.evaluate(FROM, TO_DN);
        assertTrue(result.isPossible());
    }

    @Test
    void climbOnNonClimbableBlock_isImpossible() {
        // FROM is not a climbable block (ladder/vine)
        IWorldLayer noLadder = new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return false; }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return false; }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
            @Override public boolean isClimbable(@NonNull BlockPos pos) { return false; }
        };
        ClimbEvaluator evaluator = new ClimbEvaluator(noLadder);
        assertFalse(evaluator.evaluate(FROM, TO_UP).isPossible());
    }

    @Test
    void climbIntoSolidBlock_isImpossible() {
        ClimbEvaluator evaluator = new ClimbEvaluator(climbableAt(FROM, false));
        assertFalse(evaluator.evaluate(FROM, TO_UP).isPossible());
    }

    @Test
    void diagonalMove_isImpossible() {
        ClimbEvaluator evaluator = new ClimbEvaluator(climbableAt(FROM, true));
        assertFalse(evaluator.evaluate(FROM, TO_SIDE).isPossible());
    }
}
