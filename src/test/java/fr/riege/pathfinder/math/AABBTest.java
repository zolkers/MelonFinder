package fr.riege.pathfinder.math;

import fr.riege.api.math.AABB;
import fr.riege.api.math.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AABBTest {
    @Test void intersects_overlapping_returnsTrue() {
        assertTrue(new AABB(new Vec3(0,0,0), new Vec3(1,1,1))
            .intersects(new AABB(new Vec3(0.5,0.5,0.5), new Vec3(1.5,1.5,1.5))));
    }
    @Test void intersects_noOverlap_returnsFalse() {
        assertFalse(new AABB(new Vec3(0,0,0), new Vec3(1,1,1))
            .intersects(new AABB(new Vec3(2,2,2), new Vec3(3,3,3))));
    }
    @Test void expand_returnsLargerBox() {
        AABB expanded = new AABB(new Vec3(0,0,0), new Vec3(1,1,1)).expand(0.3);
        assertEquals(-0.3, expanded.getMin().getX(), 0.0001);
        assertEquals(1.3, expanded.getMax().getX(), 0.0001);
    }
}
