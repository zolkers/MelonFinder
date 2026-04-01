package fr.riege.api.path;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class Path {

    private final List<Segment> segments;
    private final double totalCost;
    private final PathStatus status;

    public Path(@NotNull List<Segment> segments, double totalCost, @NotNull PathStatus status) {
        this.segments = Collections.unmodifiableList(segments);
        this.totalCost = totalCost;
        this.status = status;
    }

    @NotNull
    public List<Segment> getSegments() {
        return segments;
    }

    public double getTotalCost() {
        return totalCost;
    }

    @NotNull
    public PathStatus getStatus() {
        return status;
    }
}
