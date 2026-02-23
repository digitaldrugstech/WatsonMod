package eu.minemania.watson.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import eu.minemania.watson.Reference;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.util.Identifier;

public class WatsonRenderLayers
{
    private static final RenderLayer NO_DEPTH_LINES = createNoDepthLinesLayer();

    public static RenderLayer getNoDepthLinesLayer()
    {
        return NO_DEPTH_LINES;
    }

    private static RenderLayer createNoDepthLinesLayer()
    {
        RenderPipeline.Snippet lineSnippet = RenderPipelines.RENDERTYPE_LINES_SNIPPET;

        RenderPipeline pipeline = RenderPipeline.builder(lineSnippet)
                .withLocation(Identifier.of(Reference.MOD_ID, "pipeline/watson_lines_no_depth"))
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withBlend(BlendFunction.TRANSLUCENT)
                .build();

        RenderPipeline registered = RenderPipelines.register(pipeline);

        RenderSetup setup = RenderSetup.builder(registered).translucent().build();
        return RenderLayer.of("watson_lines_no_depth_layer", setup);
    }
}
