package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoalRadiusTest {

    @Test
    void isReached_insideRadius() {
        GoalRadius goal = new GoalRadius(new BlockPos(0, 0, 0), 5.0);
        assertTrue(goal.isReached(new BlockPos(3, 4, 0)));
        assertTrue(goal.isReached(new BlockPos(0, 5, 0)));
        assertTrue(goal.isReached(new BlockPos(1, 1, 1)));
    }

    @Test
    void isReached_outsideRadius() {
        GoalRadius goal = new GoalRadius(new BlockPos(0, 0, 0), 5.0);
        assertFalse(goal.isReached(new BlockPos(5, 5, 5)));
        assertFalse(goal.isReached(new BlockPos(6, 0, 0)));
    }

    @Test
    void isReached_exactRadius() {
        GoalRadius goal = new GoalRadius(new BlockPos(0, 0, 0), 3.0);
        assertTrue(goal.isReached(new BlockPos(3, 0, 0)));
    }

    @Test
    void getTargetForHeuristic_returnsCenter() {
        BlockPos center = new BlockPos(10, 20, 30);
        GoalRadius goal = new GoalRadius(center, 5.0);
        assertEquals(center, goal.getTargetForHeuristic());
    }
}
