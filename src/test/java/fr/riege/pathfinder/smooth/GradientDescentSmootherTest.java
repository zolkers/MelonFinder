package fr.riege.pathfinder.smooth;

import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Direction;
import fr.riege.api.math.Vec3;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GradientDescentSmootherTest {

    private ICollisionLayer noCollision() {
        return new ICollisionLayer() {
            @Override public @NonNull List<AABB> getCollisionBoxes(@NonNull BlockPos pos) { return Collections.emptyList(); }
            @Override public boolean hasCollisionAt(@NonNull AABB box) { return false; }
            @Override public double getMaxReach(@NonNull BlockPos from, @NonNull Direction dir, double hitboxHalf) { return 5.0; }
        };
    }

    private ICollisionLayer alwaysCollision() {
        return new ICollisionLayer() {
            @Override public @NonNull List<AABB> getCollisionBoxes(@NonNull BlockPos pos) { return Collections.emptyList(); }
            @Override public boolean hasCollisionAt(@NonNull AABB box) { return true; }
            @Override public double getMaxReach(@NonNull BlockPos from, @NonNull Direction dir, double hitboxHalf) { return 5.0; }
        };
    }

    @Test
    void twoPoints_returnedUnchanged() {
        List<Vec3> points = List.of(new Vec3(0, 64, 0), new Vec3(10, 64, 0));
        GradientDescentSmoother smoother = new GradientDescentSmoother(noCollision(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        assertEquals(2, result.size());
        assertEquals(new Vec3(0, 64, 0), result.getFirst());
        assertEquals(new Vec3(10, 64, 0), result.getLast());
    }

    @Test
    void collinearPoints_noElasticDrift() {
        // All points on a straight X line — midpoint == p, so elastic term is zero
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(5, 64, 0),
            new Vec3(10, 64, 0)
        );
        GradientDescentSmoother smoother = new GradientDescentSmoother(noCollision(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        assertEquals(new Vec3(0, 64, 0), result.getFirst());
        assertEquals(new Vec3(10, 64, 0), result.getLast());
        assertEquals(64.0, result.get(1).y(), "Y must remain unchanged");
        assertEquals(5.0, result.get(1).x(), 0.01, "Collinear interior should not drift on X");
        assertEquals(0.0, result.get(1).z(), 0.01, "Collinear interior should not drift on Z");
    }

    @Test
    void bentPath_interiorPulledTowardMidpoint() {
        // Middle at (5,64,5), neighbors at (0,64,0) and (10,64,0)
        // Midpoint of neighbors = (5,64,0) — elastic pulls interior toward z=0
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(5, 64, 5),
            new Vec3(10, 64, 0)
        );
        GradientDescentSmoother smoother = new GradientDescentSmoother(noCollision(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        assertEquals(3, result.size());
        assertEquals(new Vec3(0, 64, 0), result.getFirst());
        assertTrue(result.get(1).z() < 1.0, "Interior should have moved toward midpoint (z near 0)");
        assertEquals(64.0, result.get(1).y(), "Y must remain unchanged");
        assertEquals(new Vec3(10, 64, 0), result.getLast());
    }

    @Test
    void collision_preventsSmoothingMove() {
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(5, 64, 5),
            new Vec3(10, 64, 0)
        );
        GradientDescentSmoother smoother = new GradientDescentSmoother(alwaysCollision(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        assertEquals(new Vec3(5, 64, 5), result.get(1), "Collision must prevent any movement");
    }

    @Test
    void yCoordinates_neverChanged() {
        List<Vec3> points = List.of(
            new Vec3(0, 60, 0),
            new Vec3(5, 65, 5),
            new Vec3(10, 70, 0)
        );
        GradientDescentSmoother smoother = new GradientDescentSmoother(noCollision(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        assertEquals(65.0, result.get(1).y(), "Y coordinate must never be modified");
    }

    @Test
    void outputSize_equalsInputSize() {
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(3, 64, 2),
            new Vec3(6, 64, 1),
            new Vec3(10, 64, 0)
        );
        GradientDescentSmoother smoother = new GradientDescentSmoother(noCollision(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        assertEquals(points.size(), result.size());
    }
}
