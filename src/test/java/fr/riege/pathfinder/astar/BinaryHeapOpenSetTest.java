package fr.riege.pathfinder.astar;

import fr.riege.api.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinaryHeapOpenSetTest {

    private SearchNode node(int x, double g, double h) {
        SearchNode n = new SearchNode(new BlockPos(x, 64, 0));
        n.setGCost(g);
        n.setHCost(h);
        return n;
    }

    @Test
    void removeMin_returnsLowestFCost() {
        BinaryHeapOpenSet set = new BinaryHeapOpenSet();
        set.insert(node(0, 10, 5)); // f=15
        set.insert(node(1, 3, 2));  // f=5
        set.insert(node(2, 8, 4));  // f=12

        SearchNode min = set.removeMin();
        assertNotNull(min);
        assertEquals(1, min.pos().x());
    }

    @Test
    void isEmpty_afterPollingAll_returnsTrue() {
        BinaryHeapOpenSet set = new BinaryHeapOpenSet();
        set.insert(node(0, 1, 1));
        set.removeMin();
        assertTrue(set.isEmpty());
    }

    @Test
    void update_reordersCorrectly() {
        BinaryHeapOpenSet set = new BinaryHeapOpenSet();
        SearchNode high = node(0, 10, 10); // f=20
        SearchNode low  = node(1, 1, 1);   // f=2
        set.insert(high);
        set.insert(low);

        // Drop high's cost so it should now be minimum
        high.setGCost(0);
        high.setHCost(0);
        set.update(high);

        SearchNode min = set.removeMin();
        assertNotNull(min);
        assertEquals(0, min.pos().x());
    }

    @Test
    void insert_beyondInitialCapacity_grows() {
        BinaryHeapOpenSet set = new BinaryHeapOpenSet();
        for (int i = 0; i < 2000; i++) {
            set.insert(node(i, i, 0));
        }
        assertFalse(set.isEmpty());
        SearchNode min = set.removeMin();
        assertNotNull(min);
        assertEquals(0, min.pos().x());
    }
}
