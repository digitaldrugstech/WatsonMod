package eu.minemania.watson.render;

import java.util.List;

import com.mojang.blaze3d.vertex.VertexFormat;
import fi.dy.masa.malilib.util.data.Color4f;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

public class RenderUtils
{
    private static final Random RAND = Random.create();
    private static final float LINE_WIDTH = 2.5f;

    public static BufferBuilder startDrawingLines(Tessellator tessellator)
    {
        RenderLayer lineLayer = WatsonRenderLayers.getNoDepthLinesLayer();
        return tessellator.begin(lineLayer.getDrawMode(), lineLayer.getVertexFormat());
    }

    public static void addVertex(BufferBuilder buffer, float x, float y, float z, Color4f color) {
        buffer.vertex(x, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(LINE_WIDTH);
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
        addVertex(buffer, minX, minY, minZ, color);
        addVertex(buffer, minX, minY, maxZ, color);

        addVertex(buffer, minX, minY, maxZ, color);
        addVertex(buffer, minX, maxY, maxZ, color);

        addVertex(buffer, minX, maxY, maxZ, color);
        addVertex(buffer, minX, maxY, minZ, color);

        addVertex(buffer, minX, maxY, minZ, color);
        addVertex(buffer, minX, minY, minZ, color);

        // East side
        addVertex(buffer, maxX, minY, maxZ, color);
        addVertex(buffer, maxX, minY, minZ, color);

        addVertex(buffer, maxX, minY, minZ, color);
        addVertex(buffer, maxX, maxY, minZ, color);

        addVertex(buffer, maxX, maxY, minZ, color);
        addVertex(buffer, maxX, maxY, maxZ, color);

        addVertex(buffer, maxX, maxY, maxZ, color);
        addVertex(buffer, maxX, minY, maxZ, color);

        // North side (don't repeat the vertical lines that are done by the east/west sides)
        addVertex(buffer, maxX, minY, minZ, color);
        addVertex(buffer, minX, minY, minZ, color);

        addVertex(buffer, minX, maxY, minZ, color);
        addVertex(buffer, maxX, maxY, minZ, color);

        // South side (don't repeat the vertical lines that are done by the east/west sides)
        addVertex(buffer, minX, minY, maxZ, color);
        addVertex(buffer, maxX, minY, maxZ, color);

        addVertex(buffer, maxX, maxY, maxZ, color);
        addVertex(buffer, minX, maxY, maxZ, color);
    }
    //END TEMP MALILIB

    public static void drawFullBlockOutlinesBatched(float x, float y, float z, Color4f color, BufferBuilder buffer)
    {
        addVertex(buffer, x, y, z, color);
        addVertex(buffer, x + 1F, y, z, color);

        addVertex(buffer, x, y, z, color);
        addVertex(buffer, x, y, z + 1F, color);

        addVertex(buffer, x, y, z + 1F, color);
        addVertex(buffer, x + 1F, y, z + 1F, color);

        addVertex(buffer, x + 1F, y, z, color);
        addVertex(buffer, x + 1F, y, z + 1F, color);

        addVertex(buffer, x, y + 1F, z, color);
        addVertex(buffer, x + 1F, y + 1F, z, color);

        addVertex(buffer, x, y + 1F, z, color);
        addVertex(buffer, x, y + 1F, z + 1F, color);

        addVertex(buffer, x, y + 1F, z + 1F, color);
        addVertex(buffer, x + 1F, y + 1F, z + 1F, color);

        addVertex(buffer, x + 1F, y + 1F, z, color);
        addVertex(buffer, x + 1F, y + 1F, z + 1F, color);

        addVertex(buffer, x, y, z, color);
        addVertex(buffer, x, y + 1F, z, color);

        addVertex(buffer, x + 1F, y, z, color);
        addVertex(buffer, x + 1F, y + 1F, z, color);

        addVertex(buffer, x, y, z + 1F, color);
        addVertex(buffer, x, y + 1F, z + 1F, color);

        addVertex(buffer, x + 1F, y, z + 1F, color);
        addVertex(buffer, x + 1F, y + 1, z + 1F, color);
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

        addVertex(buffer, posX, posY, posZ, color);
        addVertex(buffer, posX + widthX, posY, posZ, color);

        addVertex(buffer, posX, posY + heightY, posZ, color);
        addVertex(buffer, posX + widthX, posY + heightY, posZ, color);

        addVertex(buffer, posX, posY, posZ, color);
        addVertex(buffer, posX, posY + heightY, posZ, color);

        addVertex(buffer, posX + widthX, posY, posZ, color);
        addVertex(buffer, posX + widthX, posY + heightY, posZ, color);

        addVertex(buffer, posX, posY, posZ + widthZ, color);
        addVertex(buffer, posX + widthX, posY, posZ + widthZ, color);

        addVertex(buffer, posX, posY + heightY, posZ + widthZ, color);
        addVertex(buffer, posX + widthX, posY + heightY, posZ + widthZ, color);

        addVertex(buffer, posX, posY, posZ + widthZ, color);
        addVertex(buffer, posX, posY + heightY, posZ + widthZ, color);

        addVertex(buffer, posX + widthX, posY, posZ + widthZ, color);
        addVertex(buffer, posX + widthX, posY + heightY, posZ + widthZ, color);

        addVertex(buffer, posX + widthX, posY, posZ, color);
        addVertex(buffer, posX + widthX, posY, posZ + widthZ, color);

        addVertex(buffer, posX, posY, posZ, color);
        addVertex(buffer, posX, posY, posZ + widthZ, color);

        addVertex(buffer, posX + widthX, posY + heightY, posZ, color);
        addVertex(buffer, posX + widthX, posY + heightY, posZ + widthZ, color);

        addVertex(buffer, posX, posY + heightY, posZ, color);
        addVertex(buffer, posX, posY + heightY, posZ + widthZ, color);
    }

    public static void drawBedOutlineBatched(float x, float y, float z, Color4f color, BufferBuilder buffer)
    {
        float shortLength = 0.19f;
        float reverseShortLength = 0.81f;
        float otherSide = 1f;
        float longHeight = 0.56f;

        //left front leg
        addVertex(buffer, x, y, z, color);
        addVertex(buffer, x + shortLength, y, z, color);

        addVertex(buffer, x, y, z, color);
        addVertex(buffer, x, y, z + shortLength, color);

        addVertex(buffer, x, y, z, color);
        addVertex(buffer, x, y + longHeight, z, color);

        addVertex(buffer, x + shortLength, y, z, color);
        addVertex(buffer, x + shortLength, y + shortLength, z, color);

        addVertex(buffer, x, y, z + shortLength, color);
        addVertex(buffer, x, y + shortLength, z + shortLength, color);

        //right front leg
        addVertex(buffer, x + reverseShortLength, y, z, color);
        addVertex(buffer, x + otherSide, y, z, color);

        addVertex(buffer, x + otherSide, y, z, color);
        addVertex(buffer, x + otherSide, y, z + shortLength, color);

        addVertex(buffer, x + otherSide, y, z, color);
        addVertex(buffer, x + otherSide, y + longHeight, z, color);

        addVertex(buffer, x + reverseShortLength, y, z, color);
        addVertex(buffer, x + reverseShortLength, y + shortLength, z, color);

        addVertex(buffer, x + otherSide, y, z + shortLength, color);
        addVertex(buffer, x + otherSide, y + shortLength, z + shortLength, color);

        //left back leg
        addVertex(buffer, x, y, z + otherSide, color);
        addVertex(buffer, x + shortLength, y, z + otherSide, color);

        addVertex(buffer, x, y, z + otherSide, color);
        addVertex(buffer, x, y, z + reverseShortLength, color);

        addVertex(buffer, x, y, z + otherSide, color);
        addVertex(buffer, x, y + longHeight, z + otherSide, color);

        addVertex(buffer, x + shortLength, y, z + 1, color);
        addVertex(buffer, x + shortLength, y + shortLength, z + 1, color);

        addVertex(buffer, x, y, z + reverseShortLength, color);
        addVertex(buffer, x, y + shortLength, z + reverseShortLength, color);

        //right back leg
        addVertex(buffer, x + reverseShortLength, y, z + otherSide, color);
        addVertex(buffer, x + otherSide, y, z + otherSide, color);

        addVertex(buffer, x + otherSide, y, z + reverseShortLength, color);
        addVertex(buffer, x + otherSide, y, z + otherSide, color);

        addVertex(buffer, x + otherSide, y, z + otherSide, color);
        addVertex(buffer, x + otherSide, y + longHeight, z + otherSide, color);

        addVertex(buffer, x + reverseShortLength, y, z + otherSide, color);
        addVertex(buffer, x + reverseShortLength, y + shortLength, z + otherSide, color);

        addVertex(buffer, x + otherSide, y, z + reverseShortLength, color);
        addVertex(buffer, x + otherSide, y + shortLength, z + reverseShortLength, color);

        //middle connections
        addVertex(buffer, x + shortLength, y + shortLength, z, color);
        addVertex(buffer, x + reverseShortLength, y + shortLength, z, color);

        addVertex(buffer, x, y + shortLength, z + shortLength, color);
        addVertex(buffer, x, y + shortLength, z + reverseShortLength, color);

        addVertex(buffer, x + shortLength, y + shortLength, z + otherSide, color);
        addVertex(buffer, x + reverseShortLength, y + shortLength, z + otherSide, color);

        addVertex(buffer, x + otherSide, y + shortLength, z + shortLength, color);
        addVertex(buffer, x + otherSide, y + shortLength, z + reverseShortLength, color);

        //top connections
        addVertex(buffer, x, y + longHeight, z, color);
        addVertex(buffer, x + otherSide, y + longHeight, z, color);

        addVertex(buffer, x, y + longHeight, z, color);
        addVertex(buffer, x, y + longHeight, z + otherSide, color);

        addVertex(buffer, x, y + longHeight, z + otherSide, color);
        addVertex(buffer, x + otherSide, y + longHeight, z + otherSide, color);

        addVertex(buffer, x + otherSide, y + longHeight, z, color);
        addVertex(buffer, x + otherSide, y + longHeight, z + otherSide, color);
    }

    /**
     * Assumes a BufferBuilder in the GL_LINES mode has been initialized
     */
    public static void drawBlockModelOutlinesBatched(BlockStateModel model, BlockState state, BlockPos pos, Color4f color, BufferBuilder buffer)
    {
        // 1.21.11 rendering internals changed; use a full block outline fallback.
        drawFullBlockOutlinesBatched(pos.getX(), pos.getY(), pos.getZ(), color, buffer);
    }
}
