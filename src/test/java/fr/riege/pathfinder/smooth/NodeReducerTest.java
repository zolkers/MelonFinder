package fr.riege.pathfinder.smooth;

import fr.riege.api.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NodeReducerTest {

    private final NodeReducer reducer = new NodeReducer();

    @Test
    void straightLine_keepsOnlyStartAndEnd() {
        List<BlockPos> path = Arrays.asList(
            new BlockPos(0, 64, 0),
            new BlockPos(1, 64, 0),
            new BlockPos(2, 64, 0),
            new BlockPos(3, 64, 0)
        );
        List<BlockPos> result = reducer.reduce(path);
        assertEquals(2, result.size());
        assertEquals(new BlockPos(0, 64, 0), result.get(0));
        assertEquals(new BlockPos(3, 64, 0), result.get(1));
    }

    @Test
    void cornerPath_preservesCornerNode() {
        // Goes east then turns north
        List<BlockPos> path = Arrays.asList(
            new BlockPos(0, 64, 0),
            new BlockPos(1, 64, 0),
            new BlockPos(2, 64, 0),
            new BlockPos(2, 64, 1),
            new BlockPos(2, 64, 2)
        );
        List<BlockPos> result = reducer.reduce(path);
        assertTrue(result.contains(new BlockPos(2, 64, 0)));
    }

    @Test
    void emptyPath_returnsEmpty() {
        List<BlockPos> result = reducer.reduce(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void twoNodes_returnsSameTwoNodes() {
        List<BlockPos> path = Arrays.asList(
            new BlockPos(0, 64, 0),
            new BlockPos(1, 64, 0)
        );
        List<BlockPos> result = reducer.reduce(path);
        assertEquals(2, result.size());
        assertEquals(new BlockPos(0, 64, 0), result.get(0));
        assertEquals(new BlockPos(1, 64, 0), result.get(1));
    }
}
