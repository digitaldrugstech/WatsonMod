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

    public static BufferBuilder startDrawingLines(Tessellator tessellator)
    {
        RenderLayer lineLayer = WatsonRenderLayers.getNoDepthLinesLayer();
        return tessellator.begin(lineLayer.getDrawMode(), lineLayer.getVertexFormat());
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
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        // East side
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        // North side (don't repeat the vertical lines that are done by the east/west sides)
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        // South side (don't repeat the vertical lines that are done by the east/west sides)
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
    }
    //END TEMP MALILIB

    public static void drawFullBlockOutlinesBatched(float x, float y, float z, Color4f color, BufferBuilder buffer)
    {
        buffer.vertex(x, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + 1F, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x, y, z + 1F).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y, z + 1F).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + 1F, y, z + 1F).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + 1F, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + 1F, y, z + 1F).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y + 1F, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + 1F, y + 1F, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y + 1F, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x, y + 1F, z + 1F).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y + 1F, z + 1F).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + 1F, y + 1F, z + 1F).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + 1F, y + 1F, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + 1F, y + 1F, z + 1F).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x, y + 1F, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + 1F, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + 1F, y + 1F, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y, z + 1F).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x, y + 1F, z + 1F).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + 1F, y, z + 1F).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + 1F, y + 1, z + 1F).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
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

        buffer.vertex(posX, posY, posZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(posX + widthX, posY, posZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(posX, posY + heightY, posZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(posX + widthX, posY + heightY, posZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(posX, posY, posZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(posX, posY + heightY, posZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(posX + widthX, posY, posZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(posX + widthX, posY + heightY, posZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(posX, posY, posZ + widthZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(posX + widthX, posY, posZ + widthZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(posX, posY + heightY, posZ + widthZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(posX + widthX, posY + heightY, posZ + widthZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(posX, posY, posZ + widthZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(posX, posY + heightY, posZ + widthZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(posX + widthX, posY, posZ + widthZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(posX + widthX, posY + heightY, posZ + widthZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(posX + widthX, posY, posZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(posX + widthX, posY, posZ + widthZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(posX, posY, posZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(posX, posY, posZ + widthZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(posX + widthX, posY + heightY, posZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(posX + widthX, posY + heightY, posZ + widthZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(posX, posY + heightY, posZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(posX, posY + heightY, posZ + widthZ).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
    }

    public static void drawBedOutlineBatched(float x, float y, float z, Color4f color, BufferBuilder buffer)
    {
        float shortLength = 0.19f;
        float reverseShortLength = 0.81f;
        float otherSide = 1f;
        float longHeight = 0.56f;

        //left front leg
        buffer.vertex(x, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + shortLength, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x, y, z + shortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x, y + longHeight, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + shortLength, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + shortLength, y + shortLength, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y, z + shortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x, y + shortLength, z + shortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        //right front leg
        buffer.vertex(x + reverseShortLength, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + otherSide, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + otherSide, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + otherSide, y, z + shortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + otherSide, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + otherSide, y + longHeight, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + reverseShortLength, y, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + reverseShortLength, y + shortLength, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + otherSide, y, z + shortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + otherSide, y + shortLength, z + shortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        //left back leg
        buffer.vertex(x, y, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + shortLength, y, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x, y, z + reverseShortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x, y + longHeight, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + shortLength, y, z + 1).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + shortLength, y + shortLength, z + 1).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y, z + reverseShortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x, y + shortLength, z + reverseShortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        //right back leg
        buffer.vertex(x + reverseShortLength, y, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + otherSide, y, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + otherSide, y, z + reverseShortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + otherSide, y, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + otherSide, y, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + otherSide, y + longHeight, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + reverseShortLength, y, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + reverseShortLength, y + shortLength, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + otherSide, y, z + reverseShortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + otherSide, y + shortLength, z + reverseShortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        //middle connections
        buffer.vertex(x + shortLength, y + shortLength, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + reverseShortLength, y + shortLength, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y + shortLength, z + shortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x, y + shortLength, z + reverseShortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + shortLength, y + shortLength, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + reverseShortLength, y + shortLength, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + otherSide, y + shortLength, z + shortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + otherSide, y + shortLength, z + reverseShortLength).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        //top connections
        buffer.vertex(x, y + longHeight, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + otherSide, y + longHeight, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y + longHeight, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x, y + longHeight, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x, y + longHeight, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + otherSide, y + longHeight, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);

        buffer.vertex(x + otherSide, y + longHeight, z).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
        buffer.vertex(x + otherSide, y + longHeight, z + otherSide).color(color.r, color.g, color.b, color.a).normal(0, 0, 0).lineWidth(2.5f);
    }

    /**
     * Assumes a BufferBuilder in the GL_LINES mode has been initialized
     */
    public static void drawBlockModelOutlinesBatched(BlockStateModel model, BlockState state, BlockPos pos, Color4f color, BufferBuilder buffer)
    {
        // 1.21.11 rendering internals changed; use a full block outline fallback.
        drawFullBlockOutlinesBatched(pos.getX(), pos.getY(), pos.getZ(), color, buffer);
    }

    private static void renderModelQuadOutlines(BlockPos pos, Color4f color, List<BakedQuad> quads, BufferBuilder buffer)
    {
        // Kept for compatibility with older callsites; quads are ignored on 1.21.11.
    }

    private static void renderQuadOutlinesBatched(BlockPos pos, Color4f color, BakedQuad quad, BufferBuilder buffer)
    {
        // Legacy no-op on 1.21.11.
    }
}
