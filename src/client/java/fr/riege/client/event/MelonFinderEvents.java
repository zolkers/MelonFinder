package fr.riege.client.event;

import fr.riege.api.event.IEventBus;
import fr.riege.layer.event.EventBusImpl;

public final class MelonFinderEvents {

    public static final IEventBus BUS = new EventBusImpl();

    private MelonFinderEvents() {}
}
