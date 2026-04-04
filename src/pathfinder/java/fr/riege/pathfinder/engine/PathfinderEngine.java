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
import java.util.Map;
import java.util.Optional;

@ApiStatus.Internal
public final class PathfinderEngine {

    private static final double HITBOX_HALF_DIVISOR = 2.0;

    @Nullable
    private PathSession activeSession;
    private boolean computing;
    private volatile Map<BlockPos, Double> lastExploredCosts = Collections.emptyMap();
    private volatile Map<BlockPos, BlockPos> lastParentMap = Collections.emptyMap();

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

    public @NotNull Map<BlockPos, Double> getLastExploredCosts() {
        return lastExploredCosts;
    }

    public @NotNull Map<BlockPos, BlockPos> getLastParentMap() {
        return lastParentMap;
    }

    private @NotNull PathResult runPipeline(@NotNull BlockPos from, @NotNull IGoal goal,
            @NotNull PathfinderContext ctx) {
        long startMs = System.currentTimeMillis();
        NodeGraph graph = new NodeGraph(ctx.evaluatorRegistry(), ctx.worldLayer());
        AStarSearch search = new AStarSearch(graph, ctx.maxComputeMs());
        List<BlockPos> raw = search.search(from, goal);
        lastExploredCosts = search.getLastExploredCosts();
        lastParentMap = search.getLastParentMap();
        if (search.getLastStatus() != PathStatus.FOUND) {
            Path empty = new Path(Collections.emptyList(), 0, search.getLastStatus());
            return new PathResult(empty, System.currentTimeMillis() - startMs, search.getNodesExplored(), Optional.empty());
        }
        List<BlockPos> reduced = new NodeReducer().reduce(raw);
        double hitboxHalf = ctx.entityPhysicsLayer().getHitboxWidth() / HITBOX_HALF_DIVISOR;
        List<BlockPos> smoothed = new PathSmoother(ctx.collisionLayer(), ctx.worldLayer(), hitboxHalf).smooth(reduced);
        List<BlockPos> capped = new SegmentCapper(ctx.maxSegmentLength()).cap(smoothed);
        PathAssembler.AssemblyOutput assembled = PathAssembler.assemble(capped, ctx);
        return new PathResult(
            assembled.path(),
            System.currentTimeMillis() - startMs,
            raw.size(),
            Optional.of(assembled.debugData())
        );
    }
}
