package fr.riege.pathfinder.smooth;

import fr.riege.api.math.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CornerSoftenerTest {

    private final CornerSoftener softener = new CornerSoftener();

    @Test
    void singlePoint_returnedUnchanged() {
        List<Vec3> points = List.of(new Vec3(0, 64, 0));
        assertEquals(1, softener.soften(points).size());
    }

    @Test
    void twoPoints_returnedUnchanged() {
        List<Vec3> points = List.of(new Vec3(0, 64, 0), new Vec3(10, 64, 0));
        assertEquals(2, softener.soften(points).size());
    }

    @Test
    void straightLine_noPointsInserted() {
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(5, 64, 0),
            new Vec3(10, 64, 0)
        );
        assertEquals(3, softener.soften(points).size(), "Collinear path must not gain extra waypoints");
    }

    @Test
    void gentleTurn_noPointsInserted() {
        // Direction from (0,0) to (5,0): (1,0). From (5,0) to (10,1): (5,1)/sqrt(26) ≈ (0.98,0.20)
        // dot ≈ 0.98 > threshold → no softening
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(5, 64, 0),
            new Vec3(10, 64, 1)
        );
        assertEquals(3, softener.soften(points).size(), "Gentle turn must not insert softening points");
    }

    @Test
    void rightAngleTurn_cornerReplacedWithTwoPoints() {
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(5, 64, 0),   // 90° corner: +X then +Z
            new Vec3(5, 64, 5)
        );
        List<Vec3> result = softener.soften(points);
        assertEquals(4, result.size(), "90° corner must be replaced with approach + departure");
        assertEquals(new Vec3(0, 64, 0), result.getFirst(), "First point must be unchanged");
        assertEquals(new Vec3(5, 64, 5), result.getLast(),  "Last point must be unchanged");
        // approach: offset toward prev from corner → x < 5, z == 0
        assertTrue(result.get(1).x() < 5.0, "Approach X must be before corner");
        assertEquals(0.0, result.get(1).z(), 1e-9, "Approach Z must be on the incoming segment");
        // departure: offset toward next from corner → x == 5, z > 0
        assertEquals(5.0, result.get(2).x(), 1e-9, "Departure X must be at corner column");
        assertTrue(result.get(2).z() > 0.0, "Departure Z must be on the outgoing segment");
    }

    @Test
    void rightAngleTurn_exactStepPositions() {
        // dist_in = dist_out = 5 → step = min(0.4, 1.75, 1.75) = 0.4
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(5, 64, 0),
            new Vec3(5, 64, 5)
        );
        List<Vec3> result = softener.soften(points);
        assertEquals(4.6, result.get(1).x(), 1e-9, "Approach X must be corner.x - step");
        assertEquals(0.0, result.get(1).z(), 1e-9, "Approach Z must be unchanged");
        assertEquals(5.0, result.get(2).x(), 1e-9, "Departure X must be unchanged");
        assertEquals(0.4, result.get(2).z(), 1e-9, "Departure Z must be corner.z + step");
    }

    @Test
    void rightAngleTurn_yPreservedOnBothSoftenedPoints() {
        List<Vec3> points = List.of(
            new Vec3(0, 65, 0),
            new Vec3(5, 65, 0),
            new Vec3(5, 65, 5)
        );
        List<Vec3> result = softener.soften(points);
        assertEquals(4, result.size());
        assertEquals(65.0, result.get(1).y(), 1e-9, "Approach Y must equal corner Y");
        assertEquals(65.0, result.get(2).y(), 1e-9, "Departure Y must equal corner Y");
    }

    @Test
    void multipleSharpCorners_allSoftened() {
        // U-shape: +X then +Z then -X → two 90° corners
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(5, 64, 0),   // corner 1: +X → +Z
            new Vec3(5, 64, 5),   // corner 2: +Z → -X
            new Vec3(0, 64, 5)
        );
        List<Vec3> result = softener.soften(points);
        assertEquals(6, result.size(), "Two 90° corners must each add one extra waypoint");
    }

    @Test
    void shortSegments_stepScalesDown() {
        // dist_in = dist_out = 0.5 → step = min(0.4, 0.175, 0.175) = 0.175
        // Softening still happens; step is proportionally smaller
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(0.5, 64, 0),  // 90° corner with short segments
            new Vec3(0.5, 64, 0.5)
        );
        List<Vec3> result = softener.soften(points);
        assertEquals(4, result.size(), "Short-segment corner must still be softened with reduced step");
        // approach at (0.5 - 0.175, 64, 0) = (0.325, 64, 0)
        assertEquals(0.5 - 0.175, result.get(1).x(), 1e-9);
        assertEquals(0.175, result.get(2).z(), 1e-9);
    }

    @Test
    void emptyList_returnedUnchanged() {
        assertTrue(softener.soften(List.of()).isEmpty());
    }
}
