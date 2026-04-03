package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoalXZTest {

    @Test
    void isReached_exactMatch() {
        GoalXZ goal = new GoalXZ(5, 64, 10);
        assertTrue(goal.isReached(new BlockPos(5, 64, 10)));
    }

    @Test
    void isReached_anyY_returnsTrue() {
        GoalXZ goal = new GoalXZ(5, 64, 10);
        assertTrue(goal.isReached(new BlockPos(5, 70, 10)));
        assertTrue(goal.isReached(new BlockPos(5, 58, 10)));
    }

    @Test
    void isReached_wrongX_returnsFalse() {
        GoalXZ goal = new GoalXZ(5, 64, 10);
        assertFalse(goal.isReached(new BlockPos(6, 64, 10)));
    }

    @Test
    void isReached_wrongZ_returnsFalse() {
        GoalXZ goal = new GoalXZ(5, 64, 10);
        assertFalse(goal.isReached(new BlockPos(5, 64, 11)));
    }

    @Test
    void heuristicTarget_preservesSpecifiedY() {
        GoalXZ goal = new GoalXZ(5, 64, 10);
        assertEquals(new BlockPos(5, 64, 10), goal.getTargetForHeuristic());
    }
}
