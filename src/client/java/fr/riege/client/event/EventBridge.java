package fr.riege.client.event;

import fr.riege.api.event.EventPhase;
import fr.riege.api.event.IEventBus;
import fr.riege.client.event.events.RenderHudEvent;
import fr.riege.client.event.events.TickEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"deprecation"})
public final class EventBridge {

    private final IEventBus eventBus;
    private long tickCount;

    public EventBridge(@NotNull IEventBus eventBus) {
        this.eventBus = eventBus;
        this.tickCount = 0;
    }

    public void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            tickCount++;
            eventBus.post(new TickEvent(tickCount), EventPhase.PRE);
        });
        ClientTickEvents.END_CLIENT_TICK.register(client ->
            eventBus.post(new TickEvent(tickCount), EventPhase.POST)
        );

        HudRenderCallback.EVENT.register((graphics, tickDeltaManager) ->
            eventBus.post(new RenderHudEvent(graphics, tickDeltaManager.getGameTimeDeltaPartialTick(true)))
        );
    }

    public long getTickCount() {
        return tickCount;
    }
}
