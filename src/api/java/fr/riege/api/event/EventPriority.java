package fr.riege.api.event;

public final class EventPriority {

    public static final int LOWEST = -100;
    public static final int LOW = -50;
    public static final int NORMAL = 0;
    public static final int HIGH = 50;
    public static final int HIGHEST = 100;
    public static final int MONITOR = 1000;

    private EventPriority() {}
}
