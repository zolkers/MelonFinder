package fr.riege.client.event;

import fr.riege.api.event.IEventBus;

public final class MelonFinderEvents {

    public static final IEventBus BUS = new EventBusImpl();

    private MelonFinderEvents() {}
}
