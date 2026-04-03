package fr.riege.pathfinder.astar;

import fr.riege.api.goal.IGoal;
import fr.riege.api.math.BlockPos;
import fr.riege.api.path.PathStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class AStarSearch {

    private final NodeGraph graph;
    private final long maxComputeMs;
    private PathStatus lastStatus;
    private int nodesExplored;

    public AStarSearch(@NotNull NodeGraph graph, long maxComputeMs) {
        this.graph = graph;
        this.maxComputeMs = maxComputeMs;
        this.lastStatus = PathStatus.CANCELLED;
    }

    public @NotNull List<BlockPos> search(@NotNull BlockPos start, @NotNull IGoal goal) {
        if (goal.isReached(start)) {
            lastStatus = PathStatus.FOUND;
            nodesExplored = 0;
            return List.of(start);
        }

        HashMap<Long, SearchNode> nodes = new HashMap<>();
        BinaryHeapOpenSet openSet = new BinaryHeapOpenSet();
        long deadline = System.currentTimeMillis() + maxComputeMs;
        int iterations = 0;

        SearchNode startNode = getOrCreate(nodes, start);
        startNode.gCost = 0;
        startNode.hCost = goal.heuristicCost(start);
        openSet.insert(startNode);

        while (!openSet.isEmpty()) {
            if ((iterations & 63) == 0 && System.currentTimeMillis() >= deadline) {
                lastStatus = PathStatus.TIMEOUT;
                nodesExplored = iterations;
                return Collections.emptyList();
            }
            iterations++;

            SearchNode current = openSet.removeMin();
            current.heapPosition = -1;

            if (goal.isReached(current.pos)) {
                lastStatus = PathStatus.FOUND;
                nodesExplored = iterations;
                return reconstructPath(current);
            }

            for (NeighborMove move : graph.getNeighbors(current.pos)) {
                SearchNode neighbor = getOrCreate(nodes, move.to());
                if (neighbor.isClosed()) continue;

                double tentativeG = current.gCost + move.edgeCost();
                if (tentativeG >= neighbor.gCost) continue;

                neighbor.gCost = tentativeG;
                neighbor.hCost = goal.heuristicCost(move.to());
                neighbor.parent = current;

                if (neighbor.isOpen()) {
                    openSet.update(neighbor);
                } else {
                    openSet.insert(neighbor);
                }
            }
        }

        lastStatus = PathStatus.UNREACHABLE;
        nodesExplored = iterations;
        return Collections.emptyList();
    }

    private @NotNull SearchNode getOrCreate(@NotNull HashMap<Long, SearchNode> nodes,
            @NotNull BlockPos pos) {
        long key = pos.asLong();
        SearchNode node = nodes.get(key);
        if (node == null) {
            node = new SearchNode(pos);
            nodes.put(key, node);
        }
        return node;
    }

    private @NotNull List<BlockPos> reconstructPath(@NotNull SearchNode goal) {
        List<BlockPos> path = new ArrayList<>();
        SearchNode current = goal;
        while (current != null) {
            path.add(current.pos);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public @NotNull PathStatus getLastStatus() {
        return lastStatus;
    }

    public int getNodesExplored() {
        return nodesExplored;
    }
}
