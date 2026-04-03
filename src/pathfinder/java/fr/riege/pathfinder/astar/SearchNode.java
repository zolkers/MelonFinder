package fr.riege.pathfinder.astar;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SearchNode {

    final @NotNull BlockPos pos;
    @Nullable SearchNode parent;
    double gCost;
    double hCost;
    int heapPosition; // 0 = unseen, >0 = in heap, -1 = closed

    SearchNode(@NotNull BlockPos pos) {
        this.pos = pos;
        this.gCost = Double.MAX_VALUE;
        this.heapPosition = 0;
    }

    double fCost() {
        return gCost + hCost;
    }

    boolean isClosed() {
        return heapPosition == -1;
    }

    boolean isOpen() {
        return heapPosition > 0;
    }
}
