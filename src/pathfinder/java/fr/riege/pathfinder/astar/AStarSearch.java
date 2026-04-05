package fr.riege.pathfinder.astar;

import fr.riege.api.goal.IGoal;
import fr.riege.api.math.BlockPos;
import fr.riege.api.path.PathStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AStarSearch {

    private static final int MIN_PARTIAL_DIST_SQ = 25; // 5 blocks squared

    private final NodeGraph graph;
    private final long maxComputeMs;
    private final double heuristicWeight;
    private PathStatus lastStatus;
    private int nodesExplored;
    private volatile Map<BlockPos, Double> lastExploredCosts;
    private volatile Map<BlockPos, BlockPos> lastParentMap;

    public AStarSearch(@NotNull NodeGraph graph, long maxComputeMs) {
        this(graph, maxComputeMs, 1.0);
    }

    public AStarSearch(@NotNull NodeGraph graph, long maxComputeMs, double heuristicWeight) {
        this.graph = graph;
        this.maxComputeMs = maxComputeMs;
        this.heuristicWeight = heuristicWeight;
        this.lastStatus = PathStatus.CANCELLED;
        this.lastExploredCosts = Collections.emptyMap();
        this.lastParentMap = Collections.emptyMap();
    }

    public @NotNull List<BlockPos> search(@NotNull BlockPos start, @NotNull IGoal goal) {
        if (goal.isReached(start)) {
            lastStatus = PathStatus.FOUND;
            nodesExplored = 0;
            lastExploredCosts = Collections.emptyMap();
            lastParentMap = Collections.emptyMap();
            return List.of(start);
        }
        Map<Long, SearchNode> nodes = new HashMap<>();
        List<BlockPos> path = runSearch(start, goal, nodes);
        buildSnapshots(nodes);
        return path;
    }

    private @NotNull List<BlockPos> runSearch(@NotNull BlockPos start, @NotNull IGoal goal,
            @NotNull Map<Long, SearchNode> nodes) {
        BinaryHeapOpenSet openSet = new BinaryHeapOpenSet();
        long deadline = System.currentTimeMillis() + maxComputeMs;
        int iterations = 0;

        SearchNode startNode = getOrCreate(nodes, start);
        startNode.setGCost(0);
        startNode.setHCost(heuristicWeight * goal.heuristicCost(start));
        openSet.insert(startNode);

        SearchNode bestSoFar = null;
        double bestSoFarH = Double.MAX_VALUE;

        while (!openSet.isEmpty()) {
            if ((iterations & 63) == 0 && System.currentTimeMillis() >= deadline) {
                lastStatus = PathStatus.TIMEOUT;
                nodesExplored = iterations;
                return partialPath(start, bestSoFar);
            }
            iterations++;

            SearchNode current = openSet.removeMin();
            current.markClosed();

            if (goal.isReached(current.pos())) {
                lastStatus = PathStatus.FOUND;
                nodesExplored = iterations;
                return reconstructPath(current);
            }

            double h = goal.heuristicCost(current.pos());
            if (h < bestSoFarH && distSq(start, current.pos()) >= MIN_PARTIAL_DIST_SQ) {
                bestSoFarH = h;
                bestSoFar = current;
            }

            for (NeighborMove move : graph.getNeighbors(current.pos())) {
                SearchNode neighbor = getOrCreate(nodes, move.to());
                double tentativeG = current.gCost() + move.edgeCost();
                if (neighbor.isClosed() || tentativeG >= neighbor.gCost()) continue;

                neighbor.setGCost(tentativeG);
                neighbor.setHCost(heuristicWeight * goal.heuristicCost(move.to()));
                neighbor.setParent(current);
                openSet.upsert(neighbor);
            }
        }

        nodesExplored = iterations;
        return partialPath(start, bestSoFar);
    }

    private @NotNull List<BlockPos> partialPath(@NotNull BlockPos start, @Nullable SearchNode bestSoFar) {
        if (bestSoFar == null) {
            lastStatus = PathStatus.UNREACHABLE;
            return Collections.emptyList();
        }
        lastStatus = PathStatus.PARTIAL;
        return reconstructPath(bestSoFar);
    }

    private static int distSq(@NotNull BlockPos a, @NotNull BlockPos b) {
        int dx = b.x() - a.x();
        int dy = b.y() - a.y();
        int dz = b.z() - a.z();
        return dx * dx + dy * dy + dz * dz;
    }

    private void buildSnapshots(@NotNull Map<Long, SearchNode> nodes) {
        Map<BlockPos, Double> costs = new HashMap<>();
        Map<BlockPos, BlockPos> parents = new HashMap<>();
        for (SearchNode node : nodes.values()) {
            double g = node.gCost();
            costs.put(node.pos(), g >= Double.MAX_VALUE / 2 ? 100000.0 : g);
            SearchNode parent = node.parent();
            if (parent != null) {
                parents.put(node.pos(), parent.pos());
            }
        }
        lastExploredCosts = Collections.unmodifiableMap(costs);
        lastParentMap = Collections.unmodifiableMap(parents);
    }

    public @NotNull Map<BlockPos, Double> getLastExploredCosts() {
        return lastExploredCosts;
    }

    public @NotNull Map<BlockPos, BlockPos> getLastParentMap() {
        return lastParentMap;
    }

    private @NotNull SearchNode getOrCreate(@NotNull Map<Long, SearchNode> nodes,
            @NotNull BlockPos pos) {
        return nodes.computeIfAbsent(pos.asLong(), k -> new SearchNode(pos));
    }

    private @NotNull List<BlockPos> reconstructPath(@NotNull SearchNode goal) {
        List<BlockPos> path = new ArrayList<>();
        SearchNode current = goal;
        while (current != null) {
            path.add(current.pos());
            current = current.parent();
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
