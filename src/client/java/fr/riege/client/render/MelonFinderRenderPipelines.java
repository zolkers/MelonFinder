package fr.riege.client.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;

public final class MelonFinderRenderPipelines {

    private static final String MOD_ID = "melonfinder";
    private static final Identifier POS_COLOR_VSH = Identifier.fromNamespaceAndPath(MOD_ID, "core/pos_color");

    static final RenderPipeline LINES_WITH_DEPTH  = tris(DepthTestFunction.LEQUAL_DEPTH_TEST, "lines_depth");
    static final RenderPipeline LINES_NO_DEPTH    = tris(DepthTestFunction.NO_DEPTH_TEST,     "lines_no_depth");

    private static RenderPipeline tris(DepthTestFunction depth, String name) {
        return RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipelines/" + name))
            .withVertexShader(POS_COLOR_VSH)
            .withFragmentShader(POS_COLOR_VSH)
            .withUniform("MeshData", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthWrite(false)
            .withDepthTestFunction(depth)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .build();
    }

    private MelonFinderRenderPipelines() {}
}
