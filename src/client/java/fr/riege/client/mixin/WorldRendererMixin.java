package fr.riege.client.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.riege.client.event.events.RenderWorldEvent;
import fr.riege.client.event.MelonFinderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("java:S107")
@Mixin(LevelRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void onRenderLevel(
            GraphicsResourceAllocator graphicsResourceAllocator,
            DeltaTracker deltaTracker,
            boolean bl,
            Camera camera,
            Matrix4f modelView,
            Matrix4f projection,
            Matrix4f matrix4f3,
            GpuBufferSlice gpuBufferSlice,
            Vector4f vector4f,
            boolean bl2,
            CallbackInfo ci) {
        RenderSystem.getModelViewStack().pushMatrix().mul(modelView);

        Vec3 pos = camera.position();
        MelonFinderEvents.BUS.post(new RenderWorldEvent(
            deltaTracker.getGameTimeDeltaPartialTick(true),
            new Matrix4f(projection),
            pos.x, pos.y, pos.z
        ));

        RenderSystem.getModelViewStack().popMatrix();
    }
}
