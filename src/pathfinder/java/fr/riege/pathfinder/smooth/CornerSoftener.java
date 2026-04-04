package fr.riege.pathfinder.smooth;

import fr.riege.api.math.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects sharp horizontal turns in a Vec3 waypoint list and replaces each
 * tight corner with two softening points — an approach point (before the turn)
 * and a departure point (after the turn).
 *
 * <p>This pre-conditions the waypoints so that downstream spline smoothers
 * (Catmull-Rom) have enough room to curve without cutting into corner geometry.
 * The transformation is purely geometric: no collision checks, no world
 * layer dependency.  Downstream stages handle position validity.
 *
 * <p>Only horizontal direction (XZ plane) is used for the sharpness test;
 * the Y coordinate of each softened point is inherited from the original corner.
 */
public final class CornerSoftener {

    private static final double SHARP_ANGLE_COS = 0.5;  // cos(60°) — turns tighter than 60° are softened
    private static final double MAX_STEP        = 0.4;   // maximum offset in blocks
    private static final double SEGMENT_RATIO   = 0.35;  // step ≤ 35 % of each adjacent segment length
    private static final double ZERO_DIST_GUARD = 1e-9;

    public @NotNull List<Vec3> soften(@NotNull List<Vec3> points) {
        if (points.size() <= 2) {
            return new ArrayList<>(points);
        }
        List<Vec3> result = new ArrayList<>(points.size() + 4);
        result.add(points.getFirst());
        for (int i = 1; i < points.size() - 1; i++) {
            Vec3 prev = points.get(i - 1);
            Vec3 curr = points.get(i);
            Vec3 next = points.get(i + 1);
            if (!softenCorner(prev, curr, next, result)) {
                result.add(curr);
            }
        }
        result.add(points.getLast());
        return result;
    }

    /**
     * Attempts to replace the corner at {@code curr} with approach + departure
     * points.  Returns {@code true} and adds both points if the turn is sharp
     * enough; returns {@code false} if the turn is gentle (caller adds
     * {@code curr} as-is).
     */
    private boolean softenCorner(@NotNull Vec3 prev, @NotNull Vec3 curr,
            @NotNull Vec3 next, @NotNull List<Vec3> out) {
        double dxIn = curr.x() - prev.x();
        double dzIn = curr.z() - prev.z();
        double distIn = Math.sqrt(dxIn * dxIn + dzIn * dzIn);

        double dxOut = next.x() - curr.x();
        double dzOut = next.z() - curr.z();
        double distOut = Math.sqrt(dxOut * dxOut + dzOut * dzOut);

        if (distIn < ZERO_DIST_GUARD || distOut < ZERO_DIST_GUARD) {
            return false;
        }

        double dxInN = dxIn / distIn;
        double dzInN = dzIn / distIn;
        double dxOutN = dxOut / distOut;
        double dzOutN = dzOut / distOut;

        double dot = dxInN * dxOutN + dzInN * dzOutN;
        if (dot >= SHARP_ANGLE_COS) {
            return false;
        }

        double step = Math.min(MAX_STEP, Math.min(distIn * SEGMENT_RATIO, distOut * SEGMENT_RATIO));
        Vec3 approach  = new Vec3(curr.x() - step * dxInN,  curr.y(), curr.z() - step * dzInN);
        Vec3 departure = new Vec3(curr.x() + step * dxOutN, curr.y(), curr.z() + step * dzOutN);

        out.add(approach);
        out.add(departure);
        return true;
    }
}
