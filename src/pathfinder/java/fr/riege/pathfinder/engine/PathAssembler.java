package fr.riege.pathfinder.engine;

import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Vec3;
import fr.riege.api.path.MovementType;
import fr.riege.api.path.Node;
import fr.riege.api.path.Path;
import fr.riege.api.path.PathStatus;
import fr.riege.api.path.Segment;
import fr.riege.api.registry.MovementKeys;
import fr.riege.pathfinder.smooth.CatmullRomSmoother;
import fr.riege.pathfinder.smooth.GradientDescentSmoother;
import fr.riege.pathfinder.smooth.SubBlockSampler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final class PathAssembler {

    private PathAssembler() {}

    static @NotNull Path assemble(@NotNull List<BlockPos> waypoints, @NotNull PathfinderContext ctx) {
        if (waypoints.isEmpty()) return new Path(List.of(), 0, PathStatus.UNREACHABLE);
        double hitboxHalf = ctx.entityPhysicsLayer().getHitboxWidth() / 2.0;
        SubBlockSampler sampler = new SubBlockSampler(ctx.collisionLayer(), hitboxHalf, ctx.randomSeed());
        List<Vec3> sampled  = samplePoints(waypoints, sampler);
        List<Vec3> smoothed = new GradientDescentSmoother(ctx.collisionLayer(), hitboxHalf).smooth(sampled);
        List<Vec3> dense    = new CatmullRomSmoother(ctx.collisionLayer(), hitboxHalf).smooth(smoothed);
        List<Segment> segments = buildSegments(dense);
        double totalCost = segments.stream().mapToDouble(Segment::length).sum();
        return new Path(segments, totalCost, PathStatus.FOUND);
    }

    private static @NotNull List<Vec3> samplePoints(
            @NotNull List<BlockPos> waypoints, @NotNull SubBlockSampler sampler) {
        List<Vec3> points = new ArrayList<>(waypoints.size());
        for (int i = 0; i < waypoints.size(); i++) {
            BlockPos current   = waypoints.get(i);
            BlockPos direction = (i + 1 < waypoints.size()) ? waypoints.get(i + 1) : current;
            points.add(sampler.sample(current, direction));
        }
        return points;
    }

    private static @NotNull List<Segment> buildSegments(@NotNull List<Vec3> dense) {
        List<Segment> segments = new ArrayList<>();
        for (int i = 0; i < dense.size() - 1; i++) {
            Vec3 start  = dense.get(i);
            Vec3 end    = dense.get(i + 1);
            double length = start.distanceTo(end);
            BlockPos pos = new BlockPos(
                (int) Math.floor(start.x()),
                (int) Math.floor(start.y()),
                (int) Math.floor(start.z())
            );
            Node node = new Node(pos, new MovementType(MovementKeys.WALK), 0, 0);
            segments.add(new Segment(List.of(node), start, end, length));
        }
        return segments;
    }
}
