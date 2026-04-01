package fr.riege.pathfinder.astar;

import fr.riege.api.goal.IGoal;
import fr.riege.api.math.BlockPos;
import fr.riege.api.path.MovementType;
import fr.riege.api.path.Node;
import fr.riege.api.path.PathStatus;
import fr.riege.api.registry.MovementKeys;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AStarSearch {

    private final NodeGraph graph;
    private final int maxNodes;
    private PathStatus lastStatus = PathStatus.CANCELLED;

    public AStarSearch(@NotNull NodeGraph graph, int maxNodes) {
        this.graph = graph;
        this.maxNodes = maxNodes;
    }

    public @NotNull List<BlockPos> search(@NotNull BlockPos start, @NotNull IGoal goal) {
        if (goal.isReached(start)) {
            lastStatus = PathStatus.FOUND;
            return Collections.singletonList(start);
        }
        OpenSet openSet = new OpenSet();
        ClosedSet closedSet = new ClosedSet();
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        Map<BlockPos, Double> gCosts = new HashMap<>();
        double startH = goal.heuristicCost(start);
        openSet.add(new Node(start, new MovementType(MovementKeys.WALK), 0, startH));
        gCosts.put(start, 0.0);
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            BlockPos currentPos = current.pos();
            if (goal.isReached(currentPos)) {
                lastStatus = PathStatus.FOUND;
                return reconstructPath(cameFrom, currentPos);
            }
            if (closedSet.contains(currentPos)) {
                continue;
            }
            closedSet.add(currentPos);
            if (closedSet.size() >= maxNodes) {
                lastStatus = PathStatus.TIMEOUT;
                return Collections.emptyList();
            }
            processNeighbors(current, goal, openSet, closedSet, cameFrom, gCosts);
        }
        lastStatus = PathStatus.UNREACHABLE;
        return Collections.emptyList();
    }

    private void processNeighbors(@NotNull Node current, @NotNull IGoal goal,
            @NotNull OpenSet openSet, @NotNull ClosedSet closedSet,
            @NotNull Map<BlockPos, BlockPos> cameFrom, @NotNull Map<BlockPos, Double> gCosts) {
        for (Node neighbor : graph.getNeighbors(current)) {
            BlockPos neighborPos = neighbor.pos();
            double newG = neighbor.getGCost();
            boolean shouldSkip = closedSet.contains(neighborPos)
                    || newG >= gCosts.getOrDefault(neighborPos, Double.MAX_VALUE);
            if (shouldSkip) continue;
            gCosts.put(neighborPos, newG);
            cameFrom.put(neighborPos, current.pos());
            double h = goal.heuristicCost(neighborPos);
            openSet.add(new Node(neighborPos, neighbor.movementType(), newG, h));
        }
    }

    private @NotNull List<BlockPos> reconstructPath(
            @NotNull Map<BlockPos, BlockPos> cameFrom, @NotNull BlockPos current) {
        List<BlockPos> path = new ArrayList<>();
        BlockPos node = current;
        while (node != null) {
            path.add(node);
            node = cameFrom.get(node);
        }
        Collections.reverse(path);
        return path;
    }

    public @NotNull PathStatus getLastStatus() {
        return lastStatus;
    }
}
