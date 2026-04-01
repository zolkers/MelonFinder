package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParkourEvaluatorTest {

    private static final BlockPos FROM = new BlockPos(0, 64, 0);
    private static final BlockPos TO_GAP2 = new BlockPos(2, 64, 0);
    private static final BlockPos TO_GAP1 = new BlockPos(1, 64, 0);
    private static final BlockPos TO_DIAGONAL = new BlockPos(2, 64, 2);

    private IWorldLayer walkableWorld() {
        return new IWorldLayer() {
            @Override public boolean isWalkable(BlockPos pos) { return true; }
            @Override public boolean isSolid(BlockPos pos) { return false; }
            @Override public FluidType getFluidType(BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(BlockPos pos) { return 15; }
        };
    }

    private IWorldLayer nonWalkableWorld() {
        return new IWorldLayer() {
            @Override public boolean isWalkable(BlockPos pos) { return false; }
            @Override public boolean isSolid(BlockPos pos) { return false; }
            @Override public FluidType getFluidType(BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(BlockPos pos) { return 15; }
        };
    }

    @Test
    void gap2Move_isPossible() {
        ParkourEvaluator evaluator = new ParkourEvaluator(walkableWorld());
        MovementResult result = evaluator.evaluate(FROM, TO_GAP2);
        assertTrue(result.isPossible());
        assertTrue(result.getCost() > 0.0);
    }

    @Test
    void gap1Move_isImpossible() {
        ParkourEvaluator evaluator = new ParkourEvaluator(walkableWorld());
        assertFalse(evaluator.evaluate(FROM, TO_GAP1).isPossible());
    }

    @Test
    void diagonalGap_isImpossible() {
        ParkourEvaluator evaluator = new ParkourEvaluator(walkableWorld());
        assertFalse(evaluator.evaluate(FROM, TO_DIAGONAL).isPossible());
    }

    @Test
    void nonWalkableDestination_isImpossible() {
        ParkourEvaluator evaluator = new ParkourEvaluator(nonWalkableWorld());
        assertFalse(evaluator.evaluate(FROM, TO_GAP2).isPossible());
    }
}
