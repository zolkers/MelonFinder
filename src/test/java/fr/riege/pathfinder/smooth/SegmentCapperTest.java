package fr.riege.pathfinder.smooth;

import fr.riege.api.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SegmentCapperTest {

    @Test
    void longPath_allSegmentsWithinLimit() {
        SegmentCapper capper = new SegmentCapper(4);
        List<BlockPos> path = new ArrayList<>();
        path.add(new BlockPos(0, 64, 0));
        path.add(new BlockPos(10, 64, 0));

        List<BlockPos> result = capper.cap(path);

        for (int i = 0; i < result.size() - 1; i++) {
            double dist = result.get(i).distanceTo(result.get(i + 1));
            assertTrue(dist <= 4.1, "Segment " + i + " has length " + dist + " > 4.1");
        }
    }

    @Test
    void singleNode_returnedUnchanged() {
        SegmentCapper capper = new SegmentCapper(4);
        List<BlockPos> path = new ArrayList<>();
        path.add(new BlockPos(0, 64, 0));
        List<BlockPos> result = capper.cap(path);
        assertEquals(1, result.size());
        assertEquals(new BlockPos(0, 64, 0), result.getFirst());
    }

    @Test
    void twoNodesWithinLimit_returnedUnchanged() {
        SegmentCapper capper = new SegmentCapper(4);
        List<BlockPos> path = new ArrayList<>();
        path.add(new BlockPos(0, 64, 0));
        path.add(new BlockPos(3, 64, 0));
        List<BlockPos> result = capper.cap(path);
        assertEquals(2, result.size());
    }

    @Test
    void elevenNodes_allSegmentsWithinLimit() {
        SegmentCapper capper = new SegmentCapper(4);
        List<BlockPos> path = new ArrayList<>();
        for (int x = 0; x <= 10; x++) {
            path.add(new BlockPos(x, 64, 0));
        }
        List<BlockPos> result = capper.cap(path);
        for (int i = 0; i < result.size() - 1; i++) {
            double dist = result.get(i).distanceTo(result.get(i + 1));
            assertTrue(dist <= 4.1, "Segment " + i + " has length " + dist + " > 4.1");
        }
    }
}
