package fr.riege.api.event.events;

import fr.riege.api.event.Event;
import fr.riege.api.path.PathResult;
import org.jetbrains.annotations.NotNull;

public final class PathCompleteEvent extends Event {

    private final PathResult result;

    public PathCompleteEvent(@NotNull PathResult result) {
        this.result = result;
    }

    public @NotNull PathResult getResult() {
        return result;
    }
}
