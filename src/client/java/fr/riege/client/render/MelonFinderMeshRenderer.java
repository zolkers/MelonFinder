package fr.riege.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniformStorage;
import org.joml.Matrix4f;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class MelonFinderMeshRenderer implements AutoCloseable {

    private DynamicUniformStorage<MelonFinderMeshUniforms> uniformStorage;
    private final MelonFinderMeshUniforms uniforms = new MelonFinderMeshUniforms();

    private DynamicUniformStorage<MelonFinderMeshUniforms> uniformStorage() {
        if (uniformStorage == null) {
            uniformStorage = new DynamicUniformStorage<>("melonfinder:mesh_data", MelonFinderMeshUniforms.BLOCK_SIZE, 256);
        }
        return uniformStorage;
    }

    public void endFrame() {
        if (uniformStorage != null) uniformStorage.endFrame();
    }

    public void render(MeshData meshData, RenderPipeline pipeline, Matrix4f proj, Matrix4f modelView) {
        MeshData.DrawState draw = meshData.drawState();
        VertexFormat format = draw.format();
        int indexCount = draw.indexCount();

        uniforms.proj.set(proj);
        uniforms.modelView.set(modelView);
        Window window = Minecraft.getInstance().getWindow();
        uniforms.screenWidth  = window.getWidth();
        uniforms.screenHeight = window.getHeight();

        GpuBufferSlice uniformSlice = uniformStorage().writeUniform(uniforms);
        GpuBuffer vertexBuf = format.uploadImmediateVertexBuffer(meshData.vertexBuffer());

        GpuBuffer indexBuf;
        VertexFormat.IndexType indexType;

        if (meshData.indexBuffer() != null) {
            indexBuf  = format.uploadImmediateIndexBuffer(Objects.requireNonNull(meshData.indexBuffer()));
            indexType = draw.indexType();
        } else {
            RenderSystem.AutoStorageIndexBuffer seq  = RenderSystem.getSequentialBuffer(draw.mode());
            indexBuf  = seq.getBuffer(indexCount);
            indexType = seq.type();
        }

        RenderTarget target  = Minecraft.getInstance().getMainRenderTarget();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

        if(target.getColorTextureView() == null) return;

        try (var pass = encoder.createRenderPass(
                () -> "melonfinder",
                target.getColorTextureView(), OptionalInt.empty(),
                target.getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(pipeline);
            pass.setUniform("MeshData", uniformSlice);
            pass.setVertexBuffer(0, vertexBuf);
            pass.setIndexBuffer(indexBuf, indexType);
            pass.drawIndexed(0, 0, indexCount, 1);
        }
    }

    @Override
    public void close() {
        if (uniformStorage != null) uniformStorage.close();
    }
}
