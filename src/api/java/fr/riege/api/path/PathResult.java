package fr.riege.api.path;

import org.jetbrains.annotations.NotNull;

public final class PathResult {

    private final Path path;
    private final long computeMs;
    private final int nodesExplored;

    public PathResult(@NotNull Path path, long computeMs, int nodesExplored) {
        this.path = path;
        this.computeMs = computeMs;
        this.nodesExplored = nodesExplored;
    }

    @NotNull
    public Path getPath() {
        return path;
    }

    public long getComputeMs() {
        return computeMs;
    }

    public int getNodesExplored() {
        return nodesExplored;
    }
}
