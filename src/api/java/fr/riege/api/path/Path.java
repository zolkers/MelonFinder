package fr.riege.api.path;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Path(List<Segment> segments, double totalCost, PathStatus status) {

    public Path(@NotNull List<Segment> segments, double totalCost, @NotNull PathStatus status) {
        this.segments = List.copyOf(segments);
        this.totalCost = totalCost;
        this.status = status;
    }
}
