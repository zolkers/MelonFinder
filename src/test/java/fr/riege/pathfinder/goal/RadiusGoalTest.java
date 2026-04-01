package fr.riege.pathfinder.goal;

import fr.riege.api.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RadiusGoalTest {

    @Test
    void isReached_atCenter_returnsTrue() {
        BlockPos center = new BlockPos(5, 64, 5);
        RadiusGoal goal = new RadiusGoal(center, 3.0);

        assertTrue(goal.isReached(center));
    }

    @Test
    void isReached_withinRadius_returnsTrue() {
        RadiusGoal goal = new RadiusGoal(new BlockPos(0, 0, 0), 5.0);

        assertTrue(goal.isReached(new BlockPos(3, 0, 4))); // distance = 5.0, on boundary
        assertTrue(goal.isReached(new BlockPos(1, 0, 1))); // well inside
    }

    @Test
    void isReached_outsideRadius_returnsFalse() {
        RadiusGoal goal = new RadiusGoal(new BlockPos(0, 0, 0), 4.0);

        assertFalse(goal.isReached(new BlockPos(3, 0, 4))); // distance = 5.0 > 4.0
        assertFalse(goal.isReached(new BlockPos(5, 0, 0)));
    }

    @Test
    void getTargetForHeuristic_returnsCenter() {
        BlockPos center = new BlockPos(10, 70, -5);
        RadiusGoal goal = new RadiusGoal(center, 2.0);

        assertEquals(center, goal.getTargetForHeuristic());
    }

    @Test
    void getCenter_returnsCenter() {
        BlockPos center = new BlockPos(7, 64, 3);
        RadiusGoal goal = new RadiusGoal(center, 1.0);

        assertEquals(center, goal.getCenter());
    }

    @Test
    void getRadius_returnsRadius() {
        RadiusGoal goal = new RadiusGoal(new BlockPos(0, 0, 0), 6.5);

        assertEquals(6.5, goal.getRadius(), 1e-9);
    }

    @Test
    void heuristicCost_fromCenter_isZero() {
        BlockPos center = new BlockPos(5, 64, 5);
        RadiusGoal goal = new RadiusGoal(center, 3.0);

        assertEquals(0.0, goal.heuristicCost(center), 1e-9);
    }

    @Test
    void heuristicCost_fromOrigin_isEuclidean() {
        RadiusGoal goal = new RadiusGoal(new BlockPos(3, 0, 4), 1.0);
        // distance from (0,0,0) to (3,0,4) = 5
        assertEquals(5.0, goal.heuristicCost(new BlockPos(0, 0, 0)), 1e-9);
    }
}
