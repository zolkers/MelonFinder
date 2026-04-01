package fr.riege.pathfinder.heuristic;

import fr.riege.api.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Euclidean3DHeuristicTest {
    @Test void estimate_knownDistance() {
        assertEquals(5.0, new Euclidean3DHeuristic().estimate(new BlockPos(0, 0, 0), new BlockPos(3, 4, 0)), 0.0001);
    }
    @Test void estimate_samePos_returnsZero() {
        assertEquals(0.0, new Euclidean3DHeuristic().estimate(new BlockPos(5, 5, 5), new BlockPos(5, 5, 5)), 0.0001);
    }
}
