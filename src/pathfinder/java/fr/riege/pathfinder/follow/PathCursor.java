package fr.riege.pathfinder.follow;

import fr.riege.api.math.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class PathCursor {

    private static final double PASS_THRESHOLD = 0.5;
    private static final double LOOKAHEAD_DIST = 1.5;

    private final List<Vec3> path;
    private int cursor;

    public PathCursor(@NotNull List<Vec3> path) {
        this.path = path;
        this.cursor = 0;
    }

    public void advance(double x, double y, double z) {
        while (cursor < path.size() - 1) {
            Vec3 current = path.get(cursor);
            Vec3 next = path.get(cursor + 1);
            double distToCurrent = distTo(x, y, z, current);
            double distToNext = distTo(x, y, z, next);
            if (distToCurrent <= PASS_THRESHOLD || distToNext <= distToCurrent) {
                cursor++;
            } else {
                break;
            }
        }
    }

    public @NotNull Vec3 getLookahead() {
        if (cursor >= path.size()) {
            return path.getLast();
        }
        double remaining = LOOKAHEAD_DIST;
        int i = cursor;
        while (i < path.size() - 1) {
            Vec3 from = path.get(i);
            Vec3 to = path.get(i + 1);
            double segLen = from.distanceTo(to);
            if (remaining <= segLen) {
                double t = remaining / segLen;
                return new Vec3(
                    from.x() + t * (to.x() - from.x()),
                    from.y() + t * (to.y() - from.y()),
                    from.z() + t * (to.z() - from.z())
                );
            }
            remaining -= segLen;
            i++;
        }
        return path.getLast();
    }

    public boolean isDone() {
        return cursor >= path.size() - 1;
    }

    public boolean isNearEnd(int threshold) {
        return (path.size() - cursor) <= threshold;
    }

    private static double distTo(double x, double y, double z, @NotNull Vec3 point) {
        double dx = point.x() - x;
        double dy = point.y() - y;
        double dz = point.z() - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
