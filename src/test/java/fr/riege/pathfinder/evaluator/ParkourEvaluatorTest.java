package fr.riege.pathfinder.evaluator;

import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParkourEvaluatorTest {

    private static final BlockPos FROM = new BlockPos(0, 64, 0);
    private static final BlockPos TO_GAP2 = new BlockPos(2, 64, 0);
    private static final BlockPos TO_GAP1 = new BlockPos(1, 64, 0);
    private static final BlockPos TO_DIAGONAL = new BlockPos(2, 64, 2);
    // intermediate for FROM→TO_GAP2 is (1,64,0)
    private static final BlockPos INTERMEDIATE = new BlockPos(1, 64, 0);

    private IWorldLayer gapWorld() {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) {
                return !pos.equals(INTERMEDIATE);
            }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return false; }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
        };
    }

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

    @Test
    void gap2Move_withVoidIntermediate_isPossible() {
        ParkourEvaluator evaluator = new ParkourEvaluator(gapWorld());
        MovementResult result = evaluator.evaluate(FROM, TO_GAP2);
        assertTrue(result.isPossible());
        assertTrue(result.getCost() > 0.0);
    }

    @Test
    void gap2Move_noGap_isImpossible() {
        // intermediate is walkable — no actual void to jump over
        ParkourEvaluator evaluator = new ParkourEvaluator(walkableWorld());
        assertFalse(evaluator.evaluate(FROM, TO_GAP2).isPossible());
    }

    @Test
    void gap1Move_isImpossible() {
        ParkourEvaluator evaluator = new ParkourEvaluator(gapWorld());
        assertFalse(evaluator.evaluate(FROM, TO_GAP1).isPossible());
    }

    @Test
    void diagonalGap_isImpossible() {
        ParkourEvaluator evaluator = new ParkourEvaluator(gapWorld());
        assertFalse(evaluator.evaluate(FROM, TO_DIAGONAL).isPossible());
    }

    @Test
    void nonWalkableDestination_isImpossible() {
        ParkourEvaluator evaluator = new ParkourEvaluator(nonWalkableWorld());
        assertFalse(evaluator.evaluate(FROM, TO_GAP2).isPossible());
    }

    @Test
    void blockedHeadAboveGap_isImpossible() {
        BlockPos ceiling = new BlockPos(INTERMEDIATE.x(), INTERMEDIATE.y() + 1, INTERMEDIATE.z());
        IWorldLayer ceilingWorld = new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return !pos.equals(INTERMEDIATE); }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return pos.equals(ceiling); }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
        };
        ParkourEvaluator evaluator = new ParkourEvaluator(ceilingWorld);
        assertFalse(evaluator.evaluate(FROM, TO_GAP2).isPossible());
    }
}
