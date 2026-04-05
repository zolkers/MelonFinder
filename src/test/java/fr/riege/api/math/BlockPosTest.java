package fr.riege.api.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockPosTest {

    @Test
    void distanceTo_exactMatch() {
        BlockPos a = new BlockPos(1, 2, 3);
        BlockPos b = new BlockPos(1, 2, 3);
        assertEquals(0.0, a.distanceTo(b));
    }

    @Test
    void distanceTo_simpleDistance() {
        BlockPos a = new BlockPos(0, 0, 0);
        BlockPos b = new BlockPos(3, 4, 0);
        assertEquals(5.0, a.distanceTo(b));
    }

    @Test
    void distanceSqTo_simpleDistance() {
        BlockPos a = new BlockPos(0, 0, 0);
        BlockPos b = new BlockPos(3, 4, 0);
        assertEquals(25.0, a.distanceSqTo(b));
    }

    @Test
    void offset_worksCorrectly() {
        BlockPos pos = new BlockPos(10, 20, 30);
        BlockPos offset = pos.offset(5, -10, 15);
        assertEquals(new BlockPos(15, 10, 45), offset);
    }

    @Test
    void asLong_isStable() {
        BlockPos pos = new BlockPos(1234, 56, -789);
        assertEquals(pos.asLong(), new BlockPos(1234, 56, -789).asLong());
    }
}
