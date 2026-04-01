package fr.riege.pathfinder.math;

import fr.riege.api.math.BlockPos;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BlockPosTest {
    @Test void distanceTo_returnsEuclidean() {
        assertEquals(5.0, new BlockPos(0,0,0).distanceTo(new BlockPos(3,4,0)), 0.0001);
    }
    @Test void equals_sameCoords_returnsTrue() {
        assertEquals(new BlockPos(1,2,3), new BlockPos(1,2,3));
    }
    @Test void offset_returnsNewPos() {
        assertEquals(new BlockPos(1,0,0), new BlockPos(0,0,0).offset(1,0,0));
    }
}
