package eu.minemania.watson.network.watson.world;

import eu.minemania.watson.Watson;
import eu.minemania.watson.config.Configs;
import eu.minemania.watson.data.DataManager;
import fi.dy.masa.malilib.network.IPluginClientPlayHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public abstract class PluginWorldPacketHandler<T extends CustomPayload> implements IPluginClientPlayHandler<T>
{
    public static final Identifier CHANNEL = new Identifier("watson","world");
    private boolean registered;

    private final static PluginWorldPacketHandler<PluginWorldPacket.Payload> INSTANCE = new PluginWorldPacketHandler<>()
    {
        @Override
        public void receive(PluginWorldPacket.Payload payload, ClientPlayNetworking.Context context)
        {
            this.receivePlayPayload(payload, context);
        }
    };

    public static PluginWorldPacketHandler<PluginWorldPacket.Payload> getInstance()
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
        return registered;
    }

    @Override
    public void setPlayRegistered(Identifier channel)
    {
        if (channel.equals(this.getPayloadChannel()))
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
        DataManager.setWorldPlugin("");
    }

    @Override
    public void receivePlayPayload(T payload, ClientPlayNetworking.Context context)
    {
        INSTANCE.decodeNbtCompound(((PluginWorldPacket.Payload) payload).content());
    }

    public void decodeNbtCompound(PluginWorldPacket content)
    {
        String world = content.getWorld();
        if (!world.isEmpty())
        {
            this.registered = true;
        }

        if (this.registered)
        {
            DataManager.setWorldPlugin(world);
            if (Configs.Generic.DEBUG.getBooleanValue())
            {
                Watson.logger.info("World: "+ world);
            }
        }
    }

    @Override
    public void encodeWithSplitter(PacketByteBuf buf, ClientPlayNetworkHandler handler)
    {
        // NO-OP
    }
}
