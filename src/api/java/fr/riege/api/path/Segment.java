package fr.riege.api.path;

import fr.riege.api.math.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Segment {

    private final List<Node> nodes;
    private final Vec3 start;
    private final Vec3 end;
    private final double length;

    public Segment(@NotNull List<Node> nodes, @NotNull Vec3 start, @NotNull Vec3 end, double length) {
        this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
        this.start = start;
        this.end = end;
        this.length = length;
    }

    @NotNull
    public List<Node> getNodes() {
        return nodes;
    }

    @NotNull
    public Vec3 getStart() {
        return start;
    }

    @NotNull
    public Vec3 getEnd() {
        return end;
    }

    public double getLength() {
        return length;
    }
}
