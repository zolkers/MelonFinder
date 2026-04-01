package fr.riege.pathfinder.astar;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class ClosedSet {

    private final HashSet<BlockPos> visited;

    public ClosedSet() {
        this.visited = new HashSet<>();
    }

    public void add(@NotNull BlockPos pos) {
        visited.add(pos);
    }

    public boolean contains(@NotNull BlockPos pos) {
        return visited.contains(pos);
    }

    public int size() {
        return visited.size();
    }
}
