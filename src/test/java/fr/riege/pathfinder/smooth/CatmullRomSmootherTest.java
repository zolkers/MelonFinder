package fr.riege.pathfinder.smooth;

import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Direction;
import fr.riege.api.math.FluidType;
import fr.riege.api.math.Vec3;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatmullRomSmootherTest {

    private static final int SAMPLES = 8; // must match CatmullRomSmoother.SAMPLES_PER_GAP

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

    private IWorldLayer solidGroundWorld() {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return false; }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return true; }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
        };
    }

    private IWorldLayer noGroundWorld() {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return false; }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return false; }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
        };
    }

    @Test
    void threeControlPoints_outputHasTwoGapsSampled() {
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(5, 64, 0),
            new Vec3(10, 64, 0)
        );
        CatmullRomSmoother smoother = new CatmullRomSmoother(noCollision(), solidGroundWorld(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        // 2 gaps × 8 samples = 16 points
        assertEquals(2 * SAMPLES, result.size());
    }

    @Test
    void straightLine_outputStaysOnLine() {
        // All points collinear on Z=0 — Catmull-Rom must not introduce lateral deviation
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(4, 64, 0),
            new Vec3(8, 64, 0),
            new Vec3(12, 64, 0)
        );
        CatmullRomSmoother smoother = new CatmullRomSmoother(noCollision(), solidGroundWorld(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        for (Vec3 pt : result) {
            assertEquals(0.0, pt.z(), 1e-9, "Z must stay 0 on a straight line");
            assertEquals(64.0, pt.y(), 1e-9, "Y must stay 64 on a flat path");
        }
    }

    @Test
    void yInterpolation_isLinear() {
        // Two control points at y=60 and y=70 — sub-points should have linearly interpolated Y
        List<Vec3> points = List.of(
            new Vec3(0, 60, 0),
            new Vec3(8, 70, 0)
        );
        CatmullRomSmoother smoother = new CatmullRomSmoother(noCollision(), solidGroundWorld(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        assertEquals(SAMPLES, result.size());
        for (int k = 0; k < SAMPLES; k++) {
            double t = (double) (k + 1) / SAMPLES;
            double expectedY = 60.0 + t * 10.0;
            assertEquals(expectedY, result.get(k).y(), 1e-9, "Y must be linearly interpolated at t=" + t);
        }
    }

    @Test
    void collision_fallsBackToEndpoint() {
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(5, 64, 0),
            new Vec3(10, 64, 0)
        );
        CatmullRomSmoother smoother = new CatmullRomSmoother(alwaysCollision(), solidGroundWorld(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        // Every sub-point must be one of the control points (p1 or p2 of its gap)
        List<Vec3> validFallbacks = List.of(
            new Vec3(0, 64, 0), new Vec3(5, 64, 0), new Vec3(10, 64, 0)
        );
        for (Vec3 pt : result) {
            assertTrue(validFallbacks.contains(pt), "Blocked point must fall back to a control point, got: " + pt);
        }
    }

    @Test
    void singleGap_lastPointEqualsP2() {
        // The last sample at t=1.0 must land exactly on the second control point
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(10, 64, 0)
        );
        CatmullRomSmoother smoother = new CatmullRomSmoother(noCollision(), solidGroundWorld(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        assertEquals(SAMPLES, result.size());
        Vec3 last = result.getLast();
        assertEquals(10.0, last.x(), 1e-9, "Last sub-point X must equal p2.x");
        assertEquals(64.0, last.y(), 1e-9, "Last sub-point Y must equal p2.y");
        assertEquals(0.0,  last.z(), 1e-9, "Last sub-point Z must equal p2.z");
    }

    @Test
    void noGroundSupport_fallsBackToEndpoint() {
        List<Vec3> points = List.of(
            new Vec3(0, 64, 0),
            new Vec3(5, 64, 0),
            new Vec3(10, 64, 0)
        );
        CatmullRomSmoother smoother = new CatmullRomSmoother(noCollision(), noGroundWorld(), 0.3);
        List<Vec3> result = smoother.smooth(points);
        List<Vec3> validEndpoints = List.of(
            new Vec3(0, 64, 0), new Vec3(5, 64, 0), new Vec3(10, 64, 0)
        );
        for (Vec3 pt : result) {
            assertTrue(validEndpoints.contains(pt), "No-ground point must fall back to endpoint, got: " + pt);
        }
    }
}
