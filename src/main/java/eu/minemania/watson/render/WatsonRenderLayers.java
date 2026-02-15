package eu.minemania.watson.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import eu.minemania.watson.Reference;
import eu.minemania.watson.Watson;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class WatsonRenderLayers
{
    private static final RenderLayer NO_DEPTH_LINES = createNoDepthLinesLayer();

    public static RenderLayer getNoDepthLinesLayer()
    {
        return NO_DEPTH_LINES;
    }

    private static RenderLayer createNoDepthLinesLayer()
    {
        try
        {
            Field snippetField = RenderPipelines.class.getDeclaredField("RENDERTYPE_LINES_SNIPPET");
            snippetField.setAccessible(true);
            RenderPipeline.Snippet lineSnippet = (RenderPipeline.Snippet) snippetField.get(null);

            RenderPipeline pipeline = RenderPipeline.builder(lineSnippet)
                    .withLocation(Identifier.of(Reference.MOD_ID, "pipeline/watson_lines_no_depth"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .build();

            // 1.21.11 maps this as private; call reflectively to ensure shader preloading.
            Method register = RenderPipelines.class.getDeclaredMethod("register", RenderPipeline.class);
            register.setAccessible(true);
            RenderPipeline registered = (RenderPipeline) register.invoke(null, pipeline);

            RenderSetup setup = RenderSetup.builder(registered).translucent().build();
            return RenderLayer.of("watson_lines_no_depth_layer", setup);
        }
        catch (Exception e)
        {
            Watson.logger.warn("Failed to create no-depth line pipeline, falling back to lines_translucent", e);
            return RenderLayers.linesTranslucent();
        }
    }
}
