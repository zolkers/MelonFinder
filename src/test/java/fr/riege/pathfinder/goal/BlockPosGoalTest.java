package fr.riege.pathfinder.goal;

import fr.riege.api.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockPosGoalTest {

    @Test
    void isReached_exactPosition_returnsTrue() {
        BlockPos target = new BlockPos(3, 64, 7);
        BlockPosGoal goal = new BlockPosGoal(target);

        assertTrue(goal.isReached(new BlockPos(3, 64, 7)));
    }

    @Test
    void isReached_differentPosition_returnsFalse() {
        BlockPosGoal goal = new BlockPosGoal(new BlockPos(3, 64, 7));

        assertFalse(goal.isReached(new BlockPos(4, 64, 7)));
        assertFalse(goal.isReached(new BlockPos(3, 65, 7)));
        assertFalse(goal.isReached(new BlockPos(3, 64, 8)));
    }

    @Test
    void getTargetForHeuristic_returnsTarget() {
        BlockPos target = new BlockPos(10, 70, -5);
        BlockPosGoal goal = new BlockPosGoal(target);

        assertEquals(target, goal.getTargetForHeuristic());
    }

    @Test
    void getTarget_returnsTarget() {
        BlockPos target = new BlockPos(1, 2, 3);
        BlockPosGoal goal = new BlockPosGoal(target);

        assertEquals(target, goal.target());
    }

    @Test
    void heuristicCost_fromTarget_isZero() {
        BlockPos target = new BlockPos(5, 64, 5);
        BlockPosGoal goal = new BlockPosGoal(target);

        assertEquals(0.0, goal.heuristicCost(target), 1e-9);
    }

    @Test
    void heuristicCost_fromOrigin_isEuclidean() {
        BlockPosGoal goal = new BlockPosGoal(new BlockPos(3, 0, 4));
        // distance from (0,0,0) to (3,0,4) = 5
        assertEquals(5.0, goal.heuristicCost(new BlockPos(0, 0, 0)), 1e-9);
    }
}
