package fr.riege.pathfinder.smooth;

import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Direction;
import fr.riege.api.math.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathSmootherTest {

    private ICollisionLayer openWorld() {
        return new ICollisionLayer() {
            @Override
            public List<AABB> getCollisionBoxes(BlockPos pos) { return Collections.emptyList(); }
            @Override
            public boolean hasCollisionAt(AABB box) { return false; }
            @Override
            public double getMaxReach(BlockPos from, Direction dir, double hitboxHalf) { return 5.0; }
        };
    }

    private ICollisionLayer wallAt(double wallX) {
        return new ICollisionLayer() {
            @Override
            public List<AABB> getCollisionBoxes(BlockPos pos) { return Collections.emptyList(); }

            @Override
            public boolean hasCollisionAt(AABB box) {
                // Treat x >= wallX and x <= wallX+1 as a wall
                return box.getMin().getX() < wallX + 1.0 && box.getMax().getX() > wallX;
            }

            @Override
            public double getMaxReach(BlockPos from, Direction dir, double hitboxHalf) { return 5.0; }
        };
    }

    @Test
    void openPath_fewerNodesThanInput() {
        PathSmoother smoother = new PathSmoother(openWorld(), 0.3);
        // L-shaped path: go east 3, then north 3 — with open world the corner should be culled
        List<BlockPos> path = Arrays.asList(
            new BlockPos(0, 64, 0),
            new BlockPos(1, 64, 0),
            new BlockPos(2, 64, 0),
            new BlockPos(3, 64, 0),
            new BlockPos(3, 64, 1),
            new BlockPos(3, 64, 2),
            new BlockPos(3, 64, 3)
        );
        List<BlockPos> result = smoother.smooth(path);
        assertTrue(result.size() < path.size(), "Smooth should remove intermediate nodes in open space");
    }

    @Test
    void wallBlockingLos_waypointPreserved() {
        // Wall sits between x=2 and x=3. Path goes from (0,64,0) → (2,64,0) → (2,64,2) → (4,64,2)
        // LOS from (0,64,0) to (4,64,2) passes through the wall region; (2,64,0) or (2,64,2) must be kept
        PathSmoother smoother = new PathSmoother(wallAt(3.0), 0.3);
        List<BlockPos> path = Arrays.asList(
            new BlockPos(0, 64, 0),
            new BlockPos(1, 64, 0),
            new BlockPos(2, 64, 0),
            new BlockPos(2, 64, 1),
            new BlockPos(2, 64, 2),
            new BlockPos(3, 64, 2),
            new BlockPos(4, 64, 2)
        );
        List<BlockPos> result = smoother.smooth(path);
        // Result must contain start and end
        assertEquals(new BlockPos(0, 64, 0), result.get(0));
        assertEquals(new BlockPos(4, 64, 2), result.get(result.size() - 1));
        // Some intermediate waypoint must be preserved due to wall
        assertTrue(result.size() >= 3, "Wall should force at least one intermediate waypoint");
    }

    @Test
    void twoNodePath_returnedUnchanged() {
        PathSmoother smoother = new PathSmoother(openWorld(), 0.3);
        List<BlockPos> path = Arrays.asList(
            new BlockPos(0, 64, 0),
            new BlockPos(5, 64, 0)
        );
        List<BlockPos> result = smoother.smooth(path);
        assertEquals(2, result.size());
        assertEquals(new BlockPos(0, 64, 0), result.get(0));
        assertEquals(new BlockPos(5, 64, 0), result.get(1));
    }
}
