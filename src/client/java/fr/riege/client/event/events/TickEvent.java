package fr.riege.client.event.events;

import fr.riege.api.event.Event;

public final class TickEvent extends Event {

    private final long tick;

    public TickEvent(long tick) {
        this.tick = tick;
    }

    public long getTick() {
        return tick;
    }
}
