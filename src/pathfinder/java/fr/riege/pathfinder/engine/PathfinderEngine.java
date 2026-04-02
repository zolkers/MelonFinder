package fr.riege.pathfinder.engine;

import fr.riege.api.goal.IGoal;
import fr.riege.api.math.BlockPos;
import fr.riege.api.path.Path;
import fr.riege.api.path.PathResult;
import fr.riege.api.path.PathStatus;
import fr.riege.pathfinder.astar.AStarSearch;
import fr.riege.pathfinder.astar.NodeGraph;
import fr.riege.pathfinder.smooth.NodeReducer;
import fr.riege.pathfinder.smooth.PathSmoother;
import fr.riege.pathfinder.smooth.SegmentCapper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
public final class PathfinderEngine {

    @Nullable
    private PathSession activeSession;
    private boolean computing;

    public @NotNull PathResult compute(@NotNull BlockPos from, @NotNull IGoal goal,
            @NotNull PathfinderContext ctx) {
        cancel();
        activeSession = new PathSession(from, goal);
        computing = true;
        PathResult result = runPipeline(from, goal, ctx);
        computing = false;
        activeSession.setStatus(result.path().status());
        return result;
    }

    public void cancel() {
        if (activeSession != null) {
            activeSession.setStatus(PathStatus.CANCELLED);
            activeSession = null;
        }
        computing = false;
    }

    public boolean isRunning() {
        return computing;
    }

    public @Nullable PathSession getActiveSession() {
        return activeSession;
    }

    private @NotNull PathResult runPipeline(@NotNull BlockPos from, @NotNull IGoal goal,
            @NotNull PathfinderContext ctx) {
        long startMs = System.currentTimeMillis();
        NodeGraph graph = new NodeGraph(ctx.evaluatorRegistry());
        AStarSearch search = new AStarSearch(graph, ctx.maxNodes());
        List<BlockPos> raw = search.search(from, goal);
        if (search.getLastStatus() != PathStatus.FOUND) {
            Path empty = new Path(Collections.emptyList(), 0, search.getLastStatus());
            return new PathResult(empty, System.currentTimeMillis() - startMs, 0);
        }
        List<BlockPos> reduced = new NodeReducer().reduce(raw);
        double hitboxHalf = ctx.entityPhysicsLayer().getHitboxWidth() / 2.0;
        List<BlockPos> smoothed = new PathSmoother(ctx.collisionLayer(), hitboxHalf).smooth(reduced);
        List<BlockPos> capped = new SegmentCapper(ctx.maxSegmentLength()).cap(smoothed);
        Path path = PathAssembler.assemble(capped, ctx);
        return new PathResult(path, System.currentTimeMillis() - startMs, raw.size());
    }
}
