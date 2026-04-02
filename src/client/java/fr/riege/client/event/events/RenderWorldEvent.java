package fr.riege.client.event.events;

import fr.riege.api.event.Event;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class RenderWorldEvent extends Event {

    private final float partialTick;
    private final Matrix4f projection;
    private final double cameraX;
    private final double cameraY;
    private final double cameraZ;

    public RenderWorldEvent(float partialTick, @NotNull Matrix4f projection,
            double cameraX, double cameraY, double cameraZ) {
        this.partialTick = partialTick;
        this.projection = projection;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZ = cameraZ;
    }

    public float getPartialTick() { return partialTick; }
    public @NotNull Matrix4f getProjection() { return projection; }
    public double getCameraX() { return cameraX; }
    public double getCameraY() { return cameraY; }
    public double getCameraZ() { return cameraZ; }
}
