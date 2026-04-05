package fr.riege.pathfinder.follow;

import fr.riege.api.math.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathCursorTest {

    /** Dense path: 10 points spaced 1 block apart along X axis. */
    private List<Vec3> straightPath() {
        return List.of(
            new Vec3(0, 64, 0),
            new Vec3(1, 64, 0),
            new Vec3(2, 64, 0),
            new Vec3(3, 64, 0),
            new Vec3(4, 64, 0),
            new Vec3(5, 64, 0),
            new Vec3(6, 64, 0),
            new Vec3(7, 64, 0),
            new Vec3(8, 64, 0),
            new Vec3(9, 64, 0)
        );
    }

    @Test
    void advance_skipsPointsWithinPassThreshold() {
        PathCursor cursor = new PathCursor(straightPath());
        // Player is at x=2.1 — points 0,1,2 are within 0.5 blocks and should be skipped
        cursor.advance(2.1, 64, 0);
        // cursor should now be past index 2; lookahead should be beyond x=2
        Vec3 lookahead = cursor.getLookahead();
        assertTrue(lookahead.x() > 2.0, "Lookahead must advance past already-passed points");
    }

    @Test
    void getLookahead_returnsPointArcLengthAhead() {
        PathCursor cursor = new PathCursor(straightPath());
        // Player at origin, lookahead distance = 1.5 blocks
        cursor.advance(0, 64, 0);
        Vec3 lookahead = cursor.getLookahead();
        // Should be roughly 1.5 blocks ahead of player position along the path
        assertTrue(lookahead.x() >= 1.0 && lookahead.x() <= 2.5,
            "Lookahead should be ~1.5 blocks ahead but was: " + lookahead.x());
    }

    @Test
    void isDone_falseAtStart() {
        PathCursor cursor = new PathCursor(straightPath());
        assertFalse(cursor.isDone());
    }

    @Test
    void isDone_trueWhenPlayerAtLastPoint() {
        PathCursor cursor = new PathCursor(straightPath());
        // Advance past all points
        cursor.advance(9.0, 64, 0);
        assertTrue(cursor.isDone(), "Cursor must be done when player reaches the last point");
    }

    @Test
    void isNearEnd_trueWhenWithinThreshold() {
        PathCursor cursor = new PathCursor(straightPath());
        // Advance to near the end (x=7, leaving 2 points)
        cursor.advance(7.1, 64, 0);
        assertTrue(cursor.isNearEnd(3), "isNearEnd(3) must be true when <=3 points remain");
    }

    @Test
    void isNearEnd_falseWhenFarFromEnd() {
        PathCursor cursor = new PathCursor(straightPath());
        cursor.advance(0, 64, 0);
        assertFalse(cursor.isNearEnd(3), "isNearEnd(3) must be false when many points remain");
    }

    @Test
    void singlePointPath_isDoneImmediately() {
        PathCursor cursor = new PathCursor(List.of(new Vec3(5, 64, 5)));
        assertTrue(cursor.isDone(), "Single-point path is immediately done");
    }
}
