package fr.riege.pathfinder.astar;

import fr.riege.api.path.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.PriorityQueue;

public final class OpenSet {

    private final PriorityQueue<Node> queue;

    public OpenSet() {
        this.queue = new PriorityQueue<>(Comparator.comparingDouble(Node::fCost));
    }

    public void add(@NotNull Node node) {
        queue.add(node);
    }

    public @Nullable Node poll() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}
