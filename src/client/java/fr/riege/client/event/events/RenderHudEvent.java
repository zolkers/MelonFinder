package fr.riege.client.event.events;

import fr.riege.api.event.Event;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public final class RenderHudEvent extends Event {

    private final GuiGraphics graphics;
    private final float tickDelta;

    public RenderHudEvent(@NotNull GuiGraphics graphics, float tickDelta) {
        this.graphics = graphics;
        this.tickDelta = tickDelta;
    }

    public @NotNull GuiGraphics getGraphics() { return graphics; }
    public float getTickDelta() { return tickDelta; }
}
