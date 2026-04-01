package fr.riege.pathfinder.engine;

import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Vec3;
import fr.riege.api.path.MovementType;
import fr.riege.api.path.Node;
import fr.riege.api.path.Path;
import fr.riege.api.path.PathStatus;
import fr.riege.api.path.Segment;
import fr.riege.api.registry.MovementKeys;
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
        List<Segment> segments = buildSegments(waypoints, sampler);
        double totalCost = segments.stream().mapToDouble(Segment::length).sum();
        return new Path(segments, totalCost, PathStatus.FOUND);
    }

    private static @NotNull List<Segment> buildSegments(
            @NotNull List<BlockPos> waypoints, @NotNull SubBlockSampler sampler) {
        List<Segment> segments = new ArrayList<>();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            BlockPos current = waypoints.get(i);
            BlockPos next = waypoints.get(i + 1);
            BlockPos after = (i + 2 < waypoints.size()) ? waypoints.get(i + 2) : next;
            Vec3 start = sampler.sample(current, next);
            Vec3 end = sampler.sample(next, after);
            double length = start.distanceTo(end);
            Node node = new Node(current, new MovementType(MovementKeys.WALK), 0, 0);
            segments.add(new Segment(List.of(node), start, end, length));
        }
        return segments;
    }
}
