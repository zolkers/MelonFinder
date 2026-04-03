package fr.riege.pathfinder.astar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

final class BinaryHeapOpenSet {

    private static final int INITIAL_CAPACITY = 1024;

    private SearchNode[] heap;
    private int size;

    BinaryHeapOpenSet() {
        this.heap = new SearchNode[INITIAL_CAPACITY + 1];
        this.size = 0;
    }

    void insert(@NotNull SearchNode node) {
        size++;
        if (size >= heap.length) {
            heap = Arrays.copyOf(heap, heap.length * 2);
        }
        heap[size] = node;
        node.setHeapPosition(size);
        bubbleUp(size);
    }

    void update(@NotNull SearchNode node) {
        bubbleUp(node.heapPosition());
    }

    void upsert(@NotNull SearchNode node) {
        if (node.isOpen()) {
            update(node);
        } else {
            insert(node);
        }
    }

    @Nullable SearchNode removeMin() {
        if (size == 0) return null;
        SearchNode min = heap[1];
        heap[1] = heap[size];
        heap[1].setHeapPosition(1);
        heap[size] = null;
        size--;
        if (size > 0) sinkDown(1);
        return min;
    }

    boolean isEmpty() {
        return size == 0;
    }

    private void bubbleUp(int index) {
        SearchNode node = heap[index];
        while (index > 1) {
            int parent = index >>> 1;
            if (heap[parent].fCost() <= node.fCost()) break;
            heap[index] = heap[parent];
            heap[index].setHeapPosition(index);
            index = parent;
        }
        heap[index] = node;
        node.setHeapPosition(index);
    }

    private void sinkDown(int index) {
        SearchNode node = heap[index];
        int child;
        while ((child = index << 1) <= size) {
            if (child + 1 <= size && heap[child + 1].fCost() < heap[child].fCost()) {
                child++;
            }
            if (node.fCost() <= heap[child].fCost()) break;
            heap[index] = heap[child];
            heap[index].setHeapPosition(index);
            index = child;
        }
        heap[index] = node;
        node.setHeapPosition(index);
    }
}
