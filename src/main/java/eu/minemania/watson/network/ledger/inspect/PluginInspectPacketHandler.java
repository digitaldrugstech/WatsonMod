package eu.minemania.watson.network.ledger.inspect;

import eu.minemania.watson.Watson;
import eu.minemania.watson.config.Configs;
import fi.dy.masa.malilib.network.IPluginClientPlayHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public abstract class PluginInspectPacketHandler<T extends CustomPayload> implements IPluginClientPlayHandler<T>
{
    public static final Identifier CHANNEL = Identifier.of("ledger","inspect");
    private boolean registered = false;

    private final static PluginInspectPacketHandler<PluginInspectPacket> INSTANCE = new PluginInspectPacketHandler<>()
    {
        @Override
        public void receive(PluginInspectPacket payload, ClientPlayNetworking.Context context)
        {
            this.receivePlayPayload(payload, context);
        }
    };

    public static PluginInspectPacketHandler<PluginInspectPacket> getInstance()
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

    public void encodePayload(BlockPos pos, int pages)
    {
        if(Configs.Generic.DEBUG.getBooleanValue())
        {
            Watson.logger.info("blockpos: " + pos.toShortString());
            Watson.logger.info("pages: " + pages);
            Watson.logger.info(CHANNEL);
        }

        INSTANCE.sendPlayPayload(new PluginInspectPacket(pos, pages));
    }

    @Override
    public void encodeWithSplitter(PacketByteBuf buf, ClientPlayNetworkHandler handler)
    {
        // NO-OP
    }

    @Override
    public void receivePlayPayload(T payload, ClientPlayNetworking.Context context)
    {
        // NO-OP
    }

}
