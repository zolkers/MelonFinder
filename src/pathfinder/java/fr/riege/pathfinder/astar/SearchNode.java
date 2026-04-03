package fr.riege.pathfinder.astar;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SearchNode {

    private final @NotNull BlockPos pos;
    private @Nullable SearchNode parent;
    private double gCost;
    private double hCost;
    private int heapPosition; // 0 = unseen, >0 = in heap, -1 = closed

    SearchNode(@NotNull BlockPos pos) {
        this.pos = pos;
        this.gCost = Double.MAX_VALUE;
        this.heapPosition = 0;
    }

    @NotNull BlockPos pos() { return pos; }
    @Nullable SearchNode parent() { return parent; }
    double gCost() { return gCost; }
    int heapPosition() { return heapPosition; }

    void setParent(@Nullable SearchNode parent) { this.parent = parent; }
    void setGCost(double gCost) { this.gCost = gCost; }
    void setHCost(double hCost) { this.hCost = hCost; }
    void setHeapPosition(int heapPosition) { this.heapPosition = heapPosition; }
    void markClosed() { this.heapPosition = -1; }

    double fCost() { return gCost + hCost; }
    boolean isClosed() { return heapPosition == -1; }
    boolean isOpen() { return heapPosition > 0; }
}
