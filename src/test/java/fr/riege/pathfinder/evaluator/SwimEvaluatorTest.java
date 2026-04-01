package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
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
            @Override public double getStandingY(@NonNull BlockPos pos) { return pos.getY(); }
            @Override public float getDragFactor(@NonNull BlockPos pos) { return 1.0f; }
            @Override public float getBlockDamage(@NonNull BlockPos pos) { return 0.0f; }
        };
    }

    @Test
    void inWater_isPossible() {
        SwimEvaluator evaluator = new SwimEvaluator(worldWithFluid(FluidType.WATER), normalBlock());
        MovementResult result = evaluator.evaluate(FROM, TO);
        assertTrue(result.isPossible());
        assertTrue(result.getCost() > 0.0);
    }

    @Test
    void noFluid_isImpossible() {
        SwimEvaluator evaluator = new SwimEvaluator(worldWithFluid(FluidType.NONE), normalBlock());
        assertFalse(evaluator.evaluate(FROM, TO).isPossible());
    }
}
