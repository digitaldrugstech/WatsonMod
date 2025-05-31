package eu.minemania.watson.network.ledger.action;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.minemania.watson.Watson;
import eu.minemania.watson.config.Configs;
import eu.minemania.watson.db.BlockEdit;
import eu.minemania.watson.db.WatsonBlock;
import eu.minemania.watson.db.WatsonBlockRegistery;
import eu.minemania.watson.scheduler.SyncTaskQueue;
import eu.minemania.watson.scheduler.tasks.AddBlockEditTask;
import fi.dy.masa.malilib.network.IPluginClientPlayHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public abstract class PluginActionPacketHandler<T extends CustomPayload> implements IPluginClientPlayHandler<T>
{
    public static final Identifier CHANNEL = Identifier.of("ledger","action");
    private boolean registered = false;

    private final static PluginActionPacketHandler<PluginActionPacket.Payload> INSTANCE = new PluginActionPacketHandler<>()
    {
        @Override
        public void receive(PluginActionPacket.Payload payload, ClientPlayNetworking.Context context)
        {
            this.receivePlayPayload(payload, context);
        }
    };

    public static PluginActionPacketHandler<PluginActionPacket.Payload> getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Identifier getPayloadChannel()
    {
        return CHANNEL;
    }

    @Override
    public boolean isPlayRegistered(Identifier channel)
    {
        return this.registered;
    }

    @Override
    public void setPlayRegistered(Identifier channel)
    {
        if (channel.equals(CHANNEL))
        {
            this.registered = true;
        }
    }

    @Override
    public void reset(Identifier channel)
    {
        if (!channel.equals(CHANNEL))
        {
            return;
        }

        INSTANCE.unregisterPlayReceiver();
    }

    public void decodePayload(PluginActionPacket content)
    {
        BlockPos pos = content.getBlockPos();
        String type = content.getType();
        Identifier dimension = content.getDimension();
        Identifier oldObj = content.getOldObject();
        Identifier newObj = content.getNewObject();
        String source = content.getSource();
        long time = content.getTime() * 1000;
        boolean rolledBack = content.isRolledBack();
        String additionalData = content.getAdditionalData();
        int count = 1;
        String id = "";
        AtomicReference<String> variant = new AtomicReference<>("");

        WatsonBlock watsonBlock = WatsonBlockRegistery.getInstance().getWatsonBlockByName(!type.contains("break") ? newObj.toString() : oldObj.toString());

        if (!additionalData.isEmpty())
        {
            try {
                NbtCompound nbtCompound = StringNbtReader.parse(additionalData);
                ItemStack itemStack = ItemStack.fromNbtOrEmpty(MinecraftClient.getInstance().world.getRegistryManager(), nbtCompound);

                if (!itemStack.isEmpty())
                {
                    count = itemStack.getCount();
                    id = itemStack.getRegistryEntry().getIdAsString();
                    if (itemStack.getRegistryEntry().getIdAsString().equals("minecraft:painting")) {
                        NbtComponent nbtComponent = itemStack.getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT);
                        if (!nbtComponent.isEmpty())
                        {
                            nbtComponent.get(PaintingEntity.VARIANT_MAP_CODEC)
                                    .result()
                                    .ifPresent(
                                            variantMap -> {
                                                variant.set(variantMap.getIdAsString());
                                            }
                                    );
                        }
                    }
                } else {
                    Watson.logger.error("Failed to parse NBT data: " + additionalData);
                }
            } catch (CommandSyntaxException e) {
                Watson.logger.error("Failed to parse NBT data: " + additionalData);
            }
        }

        if (Configs.Generic.DEBUG.getBooleanValue())
        {
            Watson.logger.info("watsonblock: " + watsonBlock.getName());
            Watson.logger.info("pos: " + pos.toString());
            Watson.logger.info("type: " + type);
            Watson.logger.info("dim: " + dimension.toString());
            Watson.logger.info("oldobj: " + oldObj);
            Watson.logger.info("newobj: " + newObj);
            Watson.logger.info("source: " + source);
            Watson.logger.info("time: " + time);
            Watson.logger.info("rolled back: " + rolledBack);
            Watson.logger.info("additional: " + additionalData);
            Watson.logger.info("count: " + count);
            Watson.logger.info("id: " + id);
            Watson.logger.info("variant: " + variant);
        }

        HashMap<String, Object> additionalDataMap = new HashMap<>();
        addDataIfNotEmpty(additionalDataMap, "rolledBack", rolledBack);
        addDataIfNotEmpty(additionalDataMap, "id", id);
        addDataIfNotEmpty(additionalDataMap, "variant", variant);

        BlockEdit edit = new BlockEdit(time, source, type, pos.getX(), pos.getY(), pos.getZ(), watsonBlock, dimension.toString(), count);
        edit.setAdditional(additionalDataMap);
        SyncTaskQueue.getInstance().addTask(new AddBlockEditTask(edit, false));
    }

    @Override
    public void encodeWithSplitter(PacketByteBuf buf, ClientPlayNetworkHandler handler)
    {
        // NO-OP
    }

    @Override
    public void receivePlayPayload(T payload, ClientPlayNetworking.Context context)
    {
        INSTANCE.decodePayload(((PluginActionPacket.Payload)payload).content());
    }

    private void addDataIfNotEmpty(HashMap<String, Object> additionalDataMap, String key, Object value)
    {
        if (value instanceof AtomicReference<?>) {
            value = ((AtomicReference<?>) value).get();
        }
        if (ObjectUtils.isNotEmpty(value))
        {
            additionalDataMap.put(key, value);
        }
    }
}
