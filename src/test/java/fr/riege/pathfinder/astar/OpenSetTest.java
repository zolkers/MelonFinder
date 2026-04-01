package fr.riege.pathfinder.astar;

import fr.riege.api.math.BlockPos;
import fr.riege.api.path.MovementType;
import fr.riege.api.path.Node;
import fr.riege.api.registry.MovementKeys;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class OpenSetTest {

    private Node node(int x, double g, double h) {
        return new Node(new BlockPos(x, 64, 0), new MovementType(MovementKeys.WALK), g, h);
    }

    @Test
    void poll_returnsLowestFCost() {
        OpenSet set = new OpenSet();
        set.add(node(0, 10, 5));
        set.add(node(1, 3, 2));
        set.add(node(2, 8, 4));
        assertEquals(1, Objects.requireNonNull(set.poll()).pos().x());
    }

    @Test
    void isEmpty_afterPollingAll_returnsTrue() {
        OpenSet set = new OpenSet();
        set.add(node(0, 1, 1));
        set.poll();
        assertTrue(set.isEmpty());
    }
}
