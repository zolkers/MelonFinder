package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WalkEvaluatorTest {

    private static final BlockPos FROM = new BlockPos(0, 64, 0);
    private static final BlockPos TO   = new BlockPos(1, 64, 0);

    private IWorldLayer world(Set<BlockPos> walkable, Set<BlockPos> solid) {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return walkable.contains(pos); }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return solid.contains(pos); }
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

    @Test
    void flatWalkableSurface_isPossibleWithPositiveCost() {
        WalkEvaluator evaluator = new WalkEvaluator(world(Set.of(TO), Set.of()), normalBlock());
        MovementResult result = evaluator.evaluate(FROM, TO);
        assertTrue(result.isPossible());
        assertTrue(result.getCost() > 0.0);
    }

    @Test
    void notWalkableDestination_isImpossible() {
        WalkEvaluator evaluator = new WalkEvaluator(world(Set.of(), Set.of()), normalBlock());
        assertFalse(evaluator.evaluate(FROM, TO).isPossible());
    }

    @Test
    void diagonalBlockedCorner_isImpossible() {
        BlockPos diagTo = new BlockPos(1, 64, 1);
        BlockPos blockedCorner = new BlockPos(1, 64, 0);
        WalkEvaluator evaluator = new WalkEvaluator(
            world(Set.of(diagTo), Set.of(blockedCorner)), normalBlock());
        assertFalse(evaluator.evaluate(FROM, diagTo).isPossible());
    }

    @Test
    void diagonalClearCorners_isPossible() {
        BlockPos diagTo = new BlockPos(1, 64, 1);
        WalkEvaluator evaluator = new WalkEvaluator(world(Set.of(diagTo), Set.of()), normalBlock());
        assertTrue(evaluator.evaluate(FROM, diagTo).isPossible());
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
        IWorldLayer flatWorld = world(Set.of(TO), Set.of());
        double normalCost = new WalkEvaluator(flatWorld, normalBlock()).evaluate(FROM, TO).getCost();
        double soulSandCost = new WalkEvaluator(flatWorld, soulSandBlock).evaluate(FROM, TO).getCost();
        assertTrue(soulSandCost > normalCost);
    }
}
