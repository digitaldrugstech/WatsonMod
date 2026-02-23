package eu.minemania.watson.db;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import eu.minemania.watson.config.Configs;
import eu.minemania.watson.render.RenderUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registries;

public class BlockEdit
{
    public long time;
    public String player;
    public String action;
    public int amount;
    public int x;
    public int y;
    public int z;
    public WatsonBlock block;
    public String world;
    public PlayereditSet playereditSet;
    public boolean disabled;
    private final BlockRenderManager blockModelShapes;
    private Block cachedBlock;
    private HashMap<String,Object> additional;

    public BlockEdit(long time, String player, String action, int x, int y, int z, WatsonBlock block, String world, int amount)
    {
        this.time = time;
        this.player = player;
        this.action = action;
        this.amount = amount;
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
        this.world = world;
        MinecraftClient mc = MinecraftClient.getInstance();
        this.blockModelShapes = mc.getBlockRenderManager();
    }

    public void setAdditional(HashMap<String,Object> additional)
    {
        this.additional = additional;
    }

    public HashMap<String,Object> getAdditional()
    {
        return this.additional;
    }

    public void drawOutline(BufferBuilder buffer, Set<Long> drawnOrePositions)
    {
        if (cachedBlock == null) {
            cachedBlock = Registries.BLOCK.get(Identifier.tryParse(block.getName()));
        }
        Block blocks = cachedBlock;
        if (!blocks.getName().getString().toLowerCase().contains("air"))
        {
            renderBlocks(buffer, blocks, drawnOrePositions);
        }
        else
        {
            renderEntities(buffer);
        }
    }

    private void renderBlocks(BufferBuilder buffer, Block blocks, Set<Long> drawnOrePositions)
    {
        Color4f color = block.getEffectiveColor();
        if (!block.getName().equals("minecraft:grass") && !block.getName().equals("minecraft:water") &&
                !block.getName().equals("minecraft:lava"))
        {
            BlockState state = blocks.getDefaultState();
            BlockStateModel model = this.blockModelShapes.getModel(state);
            if (Configs.Lists.SMALLER_RENDER_BOX.getStrings().contains(block.getName()))
            {
                RenderUtils.drawBlockBoundingBoxOutlinesBatchedLines(new BlockPos(x, y, z), color, -0.25, buffer);
            }
            else
            {
                if (isOreNotDrawn(drawnOrePositions))
                {
                    if (blocks instanceof SignBlock || blocks instanceof WallSignBlock)
                    {
                        if (Configs.Outlines.FULL_BLOCK_OUTLINE.getBooleanValue()) {
                            RenderUtils.drawFullBlockOutlinesBatched(x, y, z, color, buffer);
                        } else
                        {
                            RenderUtils.drawSpecialOutlinesBatched(x, y, z, color, buffer, true);
                        }
                    }
                    else if (blocks instanceof ChestBlock || blocks instanceof ShulkerBoxBlock)
                    {
                        RenderUtils.drawFullBlockOutlinesBatched(x, y, z, color, buffer);
                    }
                    else if (blocks instanceof BedBlock)
                    {
                        RenderUtils.drawBedOutlineBatched(x, y, z, color, buffer);
                    }
                    else
                    {
                        if (Configs.Outlines.FULL_BLOCK_OUTLINE.getBooleanValue()) {
                            RenderUtils.drawFullBlockOutlinesBatched(x, y, z, color, buffer);
                        } else {
                            RenderUtils.drawBlockModelOutlinesBatched(model, state, new BlockPos(x, y, z), color, buffer);
                        }
                    }
                }
            }
        }
        else
        {
            if (isOreNotDrawn(drawnOrePositions))
            {
                RenderUtils.drawFullBlockOutlinesBatched(x, y, z, color, buffer);
            }
        }
    }

    private void renderEntities(BufferBuilder buffer)
    {
        Optional<EntityType<?>> entity = EntityType.get(block.getName());
        Color4f color = block.getEffectiveColor();
        if (entity.isPresent())
        {
            if (block.getName().equals("minecraft:item_frame") || block.getName().equals("minecraft:painting"))
            {
                RenderUtils.drawSpecialOutlinesBatched(x, y, z, color, buffer, false);
            }
            else
            {
                RenderUtils.drawFullBlockOutlinesBatched(x, y, z, color, buffer);
            }
        }
        else
        {
            RenderUtils.drawFullBlockOutlinesBatched(x, y, z, color, buffer);
        }
    }

    private boolean isOreNotDrawn(Set<Long> drawnOrePositions)
    {
        if (!Configs.Outlines.ONLY_ORE_BLOCK.getBooleanValue()) return true;
        long packed = ((long)(x & 0x3FFFFFF)) | (((long)(z & 0x3FFFFFF)) << 26) | (((long)(y & 0xFFF)) << 52);
        return drawnOrePositions.add(packed);
    }

    public boolean isCreated()
    {
        return this.action.equals("placed") || this.action.equals("created") || this.action.equals("block-place");
    }

    public boolean isBroken()
    {
        return this.action.equals("broke") || this.action.equals("destroyed") || this.action.equals("block-break");
    }

    public boolean isContAdded()
    {
        return this.action.equals("added") || this.action.equals("put") || this.action.equals("item-insert");
    }

    public boolean isContRemoved()
    {
        return this.action.equals("removed") || this.action.equals("took") || this.action.equals("remove") || this.action.equals("item-remove");
    }

    private boolean isOreBlock(Block block)
    {
        return block instanceof ExperienceDroppingBlock || block instanceof RedstoneOreBlock || block instanceof AmethystBlock || block.equals(Blocks.ANCIENT_DEBRIS) || block.equals(Blocks.GILDED_BLACKSTONE);
    }
}
