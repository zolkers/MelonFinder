package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoalYTest {

    @Test
    void isReached_sameY_returnsTrue() {
        GoalY goal = new GoalY(0, 64, 0);
        assertTrue(goal.isReached(new BlockPos(100, 64, -200)));
    }

    @Test
    void isReached_differentY_returnsFalse() {
        GoalY goal = new GoalY(0, 64, 0);
        assertFalse(goal.isReached(new BlockPos(0, 65, 0)));
        assertFalse(goal.isReached(new BlockPos(0, 63, 0)));
    }

    @Test
    void heuristicCost_onlyAccountsForY() {
        GoalY goal = new GoalY(0, 10, 0);
        BlockPos from = new BlockPos(100, 20, 100);
        assertEquals(10.0, goal.heuristicCost(from));
    }

    @Test
    void getTargetForHeuristic_usesInitialXZ() {
        GoalY goal = new GoalY(5, 10, 15);
        assertEquals(new BlockPos(5, 10, 15), goal.getTargetForHeuristic());
    }
}
