package fr.riege.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class MinecraftRenderHandle implements RenderHandle {

    private final Matrix4f proj;
    private final MelonFinderMeshRenderer renderer;
    private final double camX;
    private final double camY;
    private final double camZ;

    private final Tesselator tessellator = Tesselator.getInstance();
    private BufferBuilder bufferBuilder;
    private final float[] color = {1.0f, 1.0f, 1.0f, 1.0f};
    private RenderPipeline pipelineDepth;
    private RenderPipeline pipelineNoDepth;

    MinecraftRenderHandle(Matrix4f proj, MelonFinderMeshRenderer renderer,
            double camX, double camY, double camZ) {
        this.proj = proj;
        this.renderer = renderer;
        this.camX = camX;
        this.camY = camY;
        this.camZ = camZ;
    }

    @Override
    public void beginLines(float r, float g, float b, float a) {
        color[0] = r; color[1] = g; color[2] = b; color[3] = a;
        pipelineDepth   = MelonFinderRenderPipelines.LINES_WITH_DEPTH;
        pipelineNoDepth = MelonFinderRenderPipelines.LINES_NO_DEPTH;
        bufferBuilder   = tessellator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
    }

    @Override
    public void emitLine(double x1, double y1, double z1,
                         double x2, double y2, double z2,
                         float lineWidth) {
        if (bufferBuilder == null) return;

        Matrix4f mv  = RenderSystem.getModelViewMatrix();
        Matrix4f mvp = new Matrix4f(proj).mul(mv);

        Vector4f c1 = mvp.transform(new Vector4f((float) x1, (float) y1, (float) z1, 1f));
        Vector4f c2 = mvp.transform(new Vector4f((float) x2, (float) y2, (float) z2, 1f));
        if (Math.abs(c1.w) < 1e-5f || Math.abs(c2.w) < 1e-5f) return;

        var win = Minecraft.getInstance().getWindow();
        float sw = win.getWidth();
        float sh = win.getHeight();
        float dx = (c2.x / c2.w - c1.x / c1.w) * sw;
        float dy = (c2.y / c2.w - c1.y / c1.w) * sh;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-5f) return;

        float px = (-dy / len) * lineWidth / sw;
        float py = ( dx / len) * lineWidth / sh;
        Vector4f pw = new Matrix4f(mvp).invert().transform(new Vector4f(px, py, 0f, 0f));

        float ax = (float) x1 + pw.x * c1.w;
        float ay = (float) y1 + pw.y * c1.w;
        float az = (float) z1 + pw.z * c1.w;
        float bx = (float) x1 - pw.x * c1.w;
        float by = (float) y1 - pw.y * c1.w;
        float bz = (float) z1 - pw.z * c1.w;
        float cx = (float) x2 + pw.x * c2.w;
        float cy = (float) y2 + pw.y * c2.w;
        float cz = (float) z2 + pw.z * c2.w;
        float ex = (float) x2 - pw.x * c2.w;
        float ey = (float) y2 - pw.y * c2.w;
        float ez = (float) z2 - pw.z * c2.w;

        float r = color[0], g = color[1], b = color[2], a = color[3];
        bufferBuilder.addVertex(ax, ay, az).setColor(r, g, b, a);
        bufferBuilder.addVertex(bx, by, bz).setColor(r, g, b, a);
        bufferBuilder.addVertex(cx, cy, cz).setColor(r, g, b, a);
        bufferBuilder.addVertex(bx, by, bz).setColor(r, g, b, a);
        bufferBuilder.addVertex(ex, ey, ez).setColor(r, g, b, a);
        bufferBuilder.addVertex(cx, cy, cz).setColor(r, g, b, a);
    }

    @Override
    public void end(boolean ignoreDepth) {
        if (bufferBuilder == null) return;
        try (MeshData meshData = bufferBuilder.build()) {
            if (meshData != null) {
                renderer.render(meshData,
                    ignoreDepth ? pipelineNoDepth : pipelineDepth,
                    proj, RenderSystem.getModelViewMatrix());
            }
        }
        bufferBuilder = null;
    }

    @Override public double cameraX() { return camX; }
    @Override public double cameraY() { return camY; }
    @Override public double cameraZ() { return camZ; }
}
