package fr.riege.pathfinder.smooth;

import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Direction;
import fr.riege.api.math.FluidType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathSmootherTest {

    private static final double WALL_X = 3.0;
    private static final double WALL_WIDTH = 1.0;

    private ICollisionLayer openWorld() {
        return new ICollisionLayer() {
            @Override
            public @NonNull List<AABB> getCollisionBoxes(@NonNull BlockPos pos) { return Collections.emptyList(); }
            @Override
            public boolean hasCollisionAt(@NonNull AABB box) { return false; }
            @Override
            public double getMaxReach(@NonNull BlockPos from, @NonNull Direction dir, double hitboxHalf) { return 5.0; }
        };
    }

    private ICollisionLayer wallAt() {
        return new ICollisionLayer() {
            @Override
            public @NonNull List<AABB> getCollisionBoxes(@NonNull BlockPos pos) { return Collections.emptyList(); }

            @Override
            public boolean hasCollisionAt(@NonNull AABB box) {
                return box.min().x() < WALL_X + WALL_WIDTH && box.max().x() > WALL_X;
            }

            @Override
            public double getMaxReach(@NonNull BlockPos from, @NonNull Direction dir, double hitboxHalf) { return 5.0; }
        };
    }

    @Test
    void openPath_fewerNodesThanInput() {
        PathSmoother smoother = new PathSmoother(openWorld(), flatWorld(), 0.3);
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
        PathSmoother smoother = new PathSmoother(wallAt(), flatWorld(), 0.3);
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
        assertEquals(new BlockPos(0, 64, 0), result.getFirst());
        assertEquals(new BlockPos(4, 64, 2), result.getLast());
        assertTrue(result.size() >= 3, "Wall should force at least one intermediate waypoint");
    }

    @Test
    void twoNodePath_returnedUnchanged() {
        PathSmoother smoother = new PathSmoother(openWorld(), flatWorld(), 0.3);
        List<BlockPos> path = Arrays.asList(
            new BlockPos(0, 64, 0),
            new BlockPos(5, 64, 0)
        );
        List<BlockPos> result = smoother.smooth(path);
        assertEquals(2, result.size());
        assertEquals(new BlockPos(0, 64, 0), result.get(0));
        assertEquals(new BlockPos(5, 64, 0), result.get(1));
    }

    private IWorldLayer flatWorld() {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return true; }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return pos.y() == 63; }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
        };
    }

    private IWorldLayer staircaseWorld() {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return true; }
            @Override public boolean isSolid(@NonNull BlockPos pos) {
                if (pos.x() <= 2 && pos.y() == 63) return true;
                if (pos.x() >= 3 && pos.x() <= 4 && pos.y() == 64) return true;
                if (pos.x() >= 5 && pos.y() == 65) return true;
                return false;
            }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
        };
    }

    @Test
    void flatPath_groundPresent_isShortcutted() {
        PathSmoother smoother = new PathSmoother(openWorld(), flatWorld(), 0.3);
        List<BlockPos> path = Arrays.asList(
            new BlockPos(0, 64, 0),
            new BlockPos(1, 64, 0),
            new BlockPos(2, 64, 0),
            new BlockPos(3, 64, 0),
            new BlockPos(4, 64, 0)
        );
        List<BlockPos> result = smoother.smooth(path);
        assertTrue(result.size() < path.size(), "Flat path with ground must still be shortcuttable");
    }

    @Test
    void staircasePath_notShortcuttedAcrossElevation() {
        PathSmoother smoother = new PathSmoother(openWorld(), staircaseWorld(), 0.3);
        List<BlockPos> path = Arrays.asList(
            new BlockPos(0, 64, 0),
            new BlockPos(1, 64, 0),
            new BlockPos(2, 64, 0),
            new BlockPos(3, 65, 0),
            new BlockPos(4, 65, 0),
            new BlockPos(5, 66, 0),
            new BlockPos(6, 66, 0)
        );
        List<BlockPos> result = smoother.smooth(path);
        assertTrue(result.size() > 2, "Staircase must not be fully shortcutted (would cause flying)");
    }
}
