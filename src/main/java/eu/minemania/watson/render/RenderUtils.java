package eu.minemania.watson.render;

import eu.minemania.watson.config.Configs;
import fi.dy.masa.malilib.util.data.Color4f;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EmptyBlockView;

public class RenderUtils
{
    public static float getLineWidth()
    {
        return (float) Configs.Outlines.ORE_LINEWIDTH.getIntegerValue();
    }

    public static BufferBuilder startDrawingLines(Tessellator tessellator)
    {
        RenderLayer lineLayer = WatsonRenderLayers.getNoDepthLinesLayer();
        return tessellator.begin(lineLayer.getDrawMode(), lineLayer.getVertexFormat());
    }

    /**
     * Emits a line segment with correct normal (line direction) for the LINES shader.
     */
    /**
     * Emits a line segment with correct normal (line direction) for the LINES shader.
     * Uses the global oreLinewidth config setting.
     */
    public static void addLine(BufferBuilder buffer,
                               float x1, float y1, float z1,
                               float x2, float y2, float z2, Color4f color)
    {
        float lw = getLineWidth();
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        buffer.vertex(x1, y1, z1).color(color.r, color.g, color.b, color.a).normal(dx, dy, dz).lineWidth(lw);
        buffer.vertex(x2, y2, z2).color(color.r, color.g, color.b, color.a).normal(dx, dy, dz).lineWidth(lw);
    }

    public static void submitBuffer(BufferBuilder buffer)
    {
        try
        {
            BuiltBuffer builtBuffer = buffer.endNullable();
            if (builtBuffer != null)
            {
                WatsonRenderLayers.getNoDepthLinesLayer().draw(builtBuffer);
                builtBuffer.close();
            }
        }
        catch (Exception e)
        {
            eu.minemania.watson.Watson.logger.warn("Failed to submit render buffer", e);
        }
    }

    //START TEMP MALILIB
    /**
     * Assumes a BufferBuilder in GL_LINES mode has been initialized
     */
    public static void drawBlockBoundingBoxOutlinesBatchedLines(BlockPos pos, Color4f color, double expand, BufferBuilder buffer)
    {
        drawBlockBoundingBoxOutlinesBatchedLines(pos, Vec3d.ZERO, color, expand, buffer);
    }

    /**
     * Assumes a BufferBuilder in GL_LINES mode has been initialized.
     * The cameraPos value will be subtracted from the absolute coordinate values of the passed in BlockPos.
     */
    public static void drawBlockBoundingBoxOutlinesBatchedLines(BlockPos pos, Vec3d cameraPos, Color4f color, double expand, BufferBuilder buffer)
    {
        float minX = (float) (pos.getX() - expand - cameraPos.x);
        float minY = (float) (pos.getY() - expand - cameraPos.y);
        float minZ = (float) (pos.getZ() - expand - cameraPos.z);
        float maxX = (float) (pos.getX() + expand - cameraPos.x + 1);
        float maxY = (float) (pos.getY() + expand - cameraPos.y + 1);
        float maxZ = (float) (pos.getZ() + expand - cameraPos.z + 1);

        drawBoxAllEdgesBatchedLines(minX, minY, minZ, maxX, maxY, maxZ, color, buffer);
    }

    /**
     * Assumes a BufferBuilder in GL_LINES mode has been initialized
     */
    public static void drawBoxAllEdgesBatchedLines(float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                                                   Color4f color, BufferBuilder buffer)
    {
        // West side
        addLine(buffer, minX, minY, minZ, minX, minY, maxZ, color);
        addLine(buffer, minX, minY, maxZ, minX, maxY, maxZ, color);
        addLine(buffer, minX, maxY, maxZ, minX, maxY, minZ, color);
        addLine(buffer, minX, maxY, minZ, minX, minY, minZ, color);

        // East side
        addLine(buffer, maxX, minY, maxZ, maxX, minY, minZ, color);
        addLine(buffer, maxX, minY, minZ, maxX, maxY, minZ, color);
        addLine(buffer, maxX, maxY, minZ, maxX, maxY, maxZ, color);
        addLine(buffer, maxX, maxY, maxZ, maxX, minY, maxZ, color);

        // North side (don't repeat the vertical lines that are done by the east/west sides)
        addLine(buffer, maxX, minY, minZ, minX, minY, minZ, color);
        addLine(buffer, minX, maxY, minZ, maxX, maxY, minZ, color);

        // South side (don't repeat the vertical lines that are done by the east/west sides)
        addLine(buffer, minX, minY, maxZ, maxX, minY, maxZ, color);
        addLine(buffer, maxX, maxY, maxZ, minX, maxY, maxZ, color);
    }
    //END TEMP MALILIB

    public static void drawFullBlockOutlinesBatched(float x, float y, float z, Color4f color, BufferBuilder buffer)
    {
        // Bottom face
        addLine(buffer, x, y, z, x + 1F, y, z, color);
        addLine(buffer, x, y, z, x, y, z + 1F, color);
        addLine(buffer, x, y, z + 1F, x + 1F, y, z + 1F, color);
        addLine(buffer, x + 1F, y, z, x + 1F, y, z + 1F, color);

        // Top face
        addLine(buffer, x, y + 1F, z, x + 1F, y + 1F, z, color);
        addLine(buffer, x, y + 1F, z, x, y + 1F, z + 1F, color);
        addLine(buffer, x, y + 1F, z + 1F, x + 1F, y + 1F, z + 1F, color);
        addLine(buffer, x + 1F, y + 1F, z, x + 1F, y + 1F, z + 1F, color);

        // Vertical edges
        addLine(buffer, x, y, z, x, y + 1F, z, color);
        addLine(buffer, x + 1F, y, z, x + 1F, y + 1F, z, color);
        addLine(buffer, x, y, z + 1F, x, y + 1F, z + 1F, color);
        addLine(buffer, x + 1F, y, z + 1F, x + 1F, y + 1F, z + 1F, color);
    }

    public static void drawSpecialOutlinesBatched(float x, float y, float z, Color4f color, BufferBuilder buffer, boolean sign)
    {
        float posX = x + 0.25F / 2;
        float posY = y + 0.25F / 2;
        float posZ = z + 0.015F;
        float widthX = (12 / 32.0F) * 2;
        float heightY = (12 / 32.0F) * 2;
        float widthZ = (1.0F / 32.0F) * 2;

        if (sign)
        {
            posX = posX - 0.1F;
            posY = posY + 0.2F;
            widthX = widthX + 0.2F;
            heightY = heightY - 0.3F;
        }

        // Front face
        addLine(buffer, posX, posY, posZ, posX + widthX, posY, posZ, color);
        addLine(buffer, posX, posY + heightY, posZ, posX + widthX, posY + heightY, posZ, color);
        addLine(buffer, posX, posY, posZ, posX, posY + heightY, posZ, color);
        addLine(buffer, posX + widthX, posY, posZ, posX + widthX, posY + heightY, posZ, color);

        // Back face
        addLine(buffer, posX, posY, posZ + widthZ, posX + widthX, posY, posZ + widthZ, color);
        addLine(buffer, posX, posY + heightY, posZ + widthZ, posX + widthX, posY + heightY, posZ + widthZ, color);
        addLine(buffer, posX, posY, posZ + widthZ, posX, posY + heightY, posZ + widthZ, color);
        addLine(buffer, posX + widthX, posY, posZ + widthZ, posX + widthX, posY + heightY, posZ + widthZ, color);

        // Connecting edges
        addLine(buffer, posX + widthX, posY, posZ, posX + widthX, posY, posZ + widthZ, color);
        addLine(buffer, posX, posY, posZ, posX, posY, posZ + widthZ, color);
        addLine(buffer, posX + widthX, posY + heightY, posZ, posX + widthX, posY + heightY, posZ + widthZ, color);
        addLine(buffer, posX, posY + heightY, posZ, posX, posY + heightY, posZ + widthZ, color);
    }

    public static void drawBedOutlineBatched(float x, float y, float z, Color4f color, BufferBuilder buffer)
    {
        float s = 0.19f;
        float rs = 0.81f;
        float o = 1f;
        float h = 0.56f;

        // Left front leg
        addLine(buffer, x, y, z, x + s, y, z, color);
        addLine(buffer, x, y, z, x, y, z + s, color);
        addLine(buffer, x, y, z, x, y + h, z, color);
        addLine(buffer, x + s, y, z, x + s, y + s, z, color);
        addLine(buffer, x, y, z + s, x, y + s, z + s, color);

        // Right front leg
        addLine(buffer, x + rs, y, z, x + o, y, z, color);
        addLine(buffer, x + o, y, z, x + o, y, z + s, color);
        addLine(buffer, x + o, y, z, x + o, y + h, z, color);
        addLine(buffer, x + rs, y, z, x + rs, y + s, z, color);
        addLine(buffer, x + o, y, z + s, x + o, y + s, z + s, color);

        // Left back leg
        addLine(buffer, x, y, z + o, x + s, y, z + o, color);
        addLine(buffer, x, y, z + o, x, y, z + rs, color);
        addLine(buffer, x, y, z + o, x, y + h, z + o, color);
        addLine(buffer, x + s, y, z + o, x + s, y + s, z + o, color);
        addLine(buffer, x, y, z + rs, x, y + s, z + rs, color);

        // Right back leg
        addLine(buffer, x + rs, y, z + o, x + o, y, z + o, color);
        addLine(buffer, x + o, y, z + rs, x + o, y, z + o, color);
        addLine(buffer, x + o, y, z + o, x + o, y + h, z + o, color);
        addLine(buffer, x + rs, y, z + o, x + rs, y + s, z + o, color);
        addLine(buffer, x + o, y, z + rs, x + o, y + s, z + rs, color);

        // Middle connections
        addLine(buffer, x + s, y + s, z, x + rs, y + s, z, color);
        addLine(buffer, x, y + s, z + s, x, y + s, z + rs, color);
        addLine(buffer, x + s, y + s, z + o, x + rs, y + s, z + o, color);
        addLine(buffer, x + o, y + s, z + s, x + o, y + s, z + rs, color);

        // Top connections
        addLine(buffer, x, y + h, z, x + o, y + h, z, color);
        addLine(buffer, x, y + h, z, x, y + h, z + o, color);
        addLine(buffer, x, y + h, z + o, x + o, y + h, z + o, color);
        addLine(buffer, x + o, y + h, z, x + o, y + h, z + o, color);
    }

    /**
     * Draws outline matching the block's VoxelShape (collision/outline shape).
     * Falls back to full block outline if shape is empty.
     */
    public static void drawBlockModelOutlinesBatched(BlockStateModel model, BlockState state, BlockPos pos, Color4f color, BufferBuilder buffer)
    {
        VoxelShape shape = state.getOutlineShape(EmptyBlockView.INSTANCE, pos);
        if (shape.isEmpty())
        {
            drawFullBlockOutlinesBatched(pos.getX(), pos.getY(), pos.getZ(), color, buffer);
            return;
        }

        float px = pos.getX();
        float py = pos.getY();
        float pz = pos.getZ();

        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) ->
                drawBoxAllEdgesBatchedLines(
                        px + (float) minX, py + (float) minY, pz + (float) minZ,
                        px + (float) maxX, py + (float) maxY, pz + (float) maxZ,
                        color, buffer));
    }
}
