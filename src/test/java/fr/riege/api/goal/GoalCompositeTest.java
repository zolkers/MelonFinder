package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GoalCompositeTest {

    @Test
    void or_isReached_ifAnyGoalIsReached_returnsTrue() {
        BlockPos pos1 = new BlockPos(1, 1, 1);
        BlockPos pos2 = new BlockPos(2, 2, 2);
        GoalComposite goal = new GoalComposite(GoalComposite.Logic.OR, new BlockGoal(pos1), new BlockGoal(pos2));

        assertTrue(goal.isReached(pos1));
        assertTrue(goal.isReached(pos2));
    }

    @Test
    void or_isReached_ifNoGoalIsReached_returnsFalse() {
        BlockPos pos1 = new BlockPos(1, 1, 1);
        BlockPos pos2 = new BlockPos(2, 2, 2);
        GoalComposite goal = new GoalComposite(GoalComposite.Logic.OR, new BlockGoal(pos1), new BlockGoal(pos2));

        assertFalse(goal.isReached(new BlockPos(3, 3, 3)));
    }

    @Test
    void or_heuristicCost_returnsMinimum() {
        BlockPos target1 = new BlockPos(10, 0, 0); // dist 10 from (0,0,0)
        BlockPos target2 = new BlockPos(0, 0, 20); // dist 20 from (0,0,0)
        GoalComposite goal = new GoalComposite(GoalComposite.Logic.OR, new BlockGoal(target1), new BlockGoal(target2));

        BlockPos from = new BlockPos(0, 0, 0);
        assertEquals(10.0, goal.heuristicCost(from));
    }

    @Test
    void and_isReached_ifAllGoalsAreReached_returnsTrue() {
        BlockPos pos = new BlockPos(10, 64, 10);
        GoalComposite goal = new GoalComposite(GoalComposite.Logic.AND,
            new GoalXZ(10, 64, 10),
            new GoalY(10, 64, 10)
        );

        assertTrue(goal.isReached(pos));
    }

    @Test
    void and_isReached_ifOneGoalIsNotReached_returnsFalse() {
        BlockPos pos = new BlockPos(10, 65, 10);
        GoalComposite goal = new GoalComposite(GoalComposite.Logic.AND,
            new GoalXZ(10, 64, 10),
            new GoalY(0, 64, 0)
        );

        assertFalse(goal.isReached(pos));
    }

    @Test
    void and_heuristicCost_returnsMaximum() {
        BlockPos start = new BlockPos(0, 0, 0);
        GoalComposite goal = new GoalComposite(GoalComposite.Logic.AND,
            new BlockGoal(new BlockPos(10, 0, 0)), // dist 10
            new BlockGoal(new BlockPos(0, 0, 20))  // dist 20
        );

        assertEquals(20.0, goal.heuristicCost(start));
    }

    @Test
    void constructor_emptyList_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new GoalComposite(GoalComposite.Logic.OR, List.of()));
    }
}
