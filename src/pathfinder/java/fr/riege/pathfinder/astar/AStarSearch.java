package fr.riege.pathfinder.astar;

import fr.riege.api.math.BlockPos;
import fr.riege.api.path.MovementType;
import fr.riege.api.path.Node;
import fr.riege.api.path.PathStatus;
import fr.riege.api.registry.MovementKeys;
import fr.riege.pathfinder.heuristic.IHeuristic;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AStarSearch {

    private final NodeGraph graph;
    private final IHeuristic heuristic;
    private final int maxNodes;
    private PathStatus lastStatus = PathStatus.CANCELLED;

    public AStarSearch(@NotNull NodeGraph graph, @NotNull IHeuristic heuristic, int maxNodes) {
        this.graph = graph;
        this.heuristic = heuristic;
        this.maxNodes = maxNodes;
    }

    public @NotNull List<BlockPos> search(@NotNull BlockPos start, @NotNull BlockPos goal) {
        if (start.equals(goal)) {
            lastStatus = PathStatus.FOUND;
            return Collections.singletonList(start);
        }
        OpenSet openSet = new OpenSet();
        ClosedSet closedSet = new ClosedSet();
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        Map<BlockPos, Double> gCosts = new HashMap<>();
        double startH = heuristic.estimate(start, goal);
        openSet.add(new Node(start, new MovementType(MovementKeys.WALK), 0, startH));
        gCosts.put(start, 0.0);
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            BlockPos currentPos = current.getPos();
            if (currentPos.equals(goal)) {
                lastStatus = PathStatus.FOUND;
                return reconstructPath(cameFrom, currentPos);
            }
            if (closedSet.size() >= maxNodes) {
                lastStatus = PathStatus.TIMEOUT;
                return Collections.emptyList();
            }
            if (closedSet.contains(currentPos)) {
                continue;
            }
            closedSet.add(currentPos);
            processNeighbors(current, goal, openSet, closedSet, cameFrom, gCosts);
        }
        lastStatus = PathStatus.UNREACHABLE;
        return Collections.emptyList();
    }

    private void processNeighbors(@NotNull Node current, @NotNull BlockPos goal,
            @NotNull OpenSet openSet, @NotNull ClosedSet closedSet,
            @NotNull Map<BlockPos, BlockPos> cameFrom, @NotNull Map<BlockPos, Double> gCosts) {
        for (Node neighbor : graph.getNeighbors(current)) {
            BlockPos neighborPos = neighbor.getPos();
            if (closedSet.contains(neighborPos)) {
                continue;
            }
            double newG = neighbor.getGCost();
            if (newG >= gCosts.getOrDefault(neighborPos, Double.MAX_VALUE)) {
                continue;
            }
            gCosts.put(neighborPos, newG);
            cameFrom.put(neighborPos, current.getPos());
            double h = heuristic.estimate(neighborPos, goal);
            openSet.add(new Node(neighborPos, neighbor.getMovementType(), newG, h));
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
