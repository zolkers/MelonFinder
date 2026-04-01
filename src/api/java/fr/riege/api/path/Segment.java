package fr.riege.api.path;

import fr.riege.api.math.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Segment(List<Node> nodes, Vec3 start, Vec3 end, double length) {

    public Segment(@NotNull List<Node> nodes, @NotNull Vec3 start, @NotNull Vec3 end, double length) {
        this.nodes = List.copyOf(nodes);
        this.start = start;
        this.end = end;
        this.length = length;
    }
}
