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

class GradientSmootherTest {

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
        GradientSmoother smoother = new GradientSmoother(noCollision(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        assertEquals(2, result.size());
        assertEquals(new Vec3(0, 64, 0), result.get(0));
        assertEquals(new Vec3(10, 64, 0), result.get(1));
    }

    @Test
    void bentPath_interiorPulledTowardMidpoint() {
        // Middle at (5,64,5), neighbors at (0,64,0) and (10,64,0)
        // Midpoint of neighbors = (5,64,0) — middle gets pulled toward z=0
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(5, 64, 5),
            new Vec3(10, 64, 0)
        );
        GradientSmoother smoother = new GradientSmoother(noCollision(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        assertEquals(3, result.size());
        assertEquals(new Vec3(0, 64, 0), result.get(0));
        assertTrue(result.get(1).z() < 1.0, "Interior point should have moved toward midpoint (z ≈ 0)");
        assertEquals(64.0, result.get(1).y(), "Y must remain unchanged");
        assertEquals(new Vec3(10, 64, 0), result.get(2));
    }

    @Test
    void collision_preventsSmoothingMove() {
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(5, 64, 5),
            new Vec3(10, 64, 0)
        );
        GradientSmoother smoother = new GradientSmoother(alwaysCollision(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        assertEquals(new Vec3(5, 64, 5), result.get(1), "Collision must prevent any movement");
    }
}
