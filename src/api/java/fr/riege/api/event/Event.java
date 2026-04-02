package fr.riege.api.event;

public abstract class Event {

    private final long timestamp;
    private boolean cancelled;

    protected Event() {
        this.timestamp = System.currentTimeMillis();
        this.cancelled = false;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }
}
