package fr.riege.client.render;

import net.minecraft.client.renderer.DynamicUniformStorage;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public final class MelonFinderMeshUniforms implements DynamicUniformStorage.DynamicUniform {

    static final int BLOCK_SIZE = 256;

    final Matrix4f proj      = new Matrix4f();
    final Matrix4f modelView = new Matrix4f();
    float screenWidth        = 1.0f;
    float screenHeight       = 1.0f;

    @Override
    public void write(@NonNull final ByteBuffer buf) {
        proj.get(0, buf);
        modelView.get(64, buf);
        buf.putFloat(128, screenWidth);
        buf.putFloat(132, screenHeight);
    }
}
