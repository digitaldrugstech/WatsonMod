package eu.minemania.watson.network.ledger.response;

import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.network.IPluginClientPlayHandler;
import fi.dy.masa.malilib.util.InfoUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public abstract class PluginResponsePacketHandler<T extends CustomPayload> implements IPluginClientPlayHandler<T>
{
    public static final Identifier CHANNEL = new Identifier("ledger","response");
    private boolean registered = false;

    private final static PluginResponsePacketHandler<PluginResponsePacket.Payload> INSTANCE = new PluginResponsePacketHandler<>()
    {
        @Override
        public void receive(PluginResponsePacket.Payload payload, ClientPlayNetworking.Context context)
        {
            this.receivePlayPayload(payload, context);
        }
    };

    public static PluginResponsePacketHandler<PluginResponsePacket.Payload> getInstance()
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

    public void decodePayload(PluginResponsePacket content)
    {
        Identifier identifier = content.getIdentifier();
        int responseCode = content.getResponseCode();
        String response;
        Message.MessageType messageType;

        switch (responseCode)
        {
            case 0 -> {
                response = "watson.message.ledger.no_permission";
                messageType = Message.MessageType.ERROR;
            }
            case 1 -> {
                response = "watson.message.ledger.executing";
                messageType = Message.MessageType.INFO;
            }
            case 2 -> {
                response = "watson.message.ledger.completed";
                messageType = Message.MessageType.SUCCESS;
            }
            case 3 -> {
                response = "watson.message.ledger.error_executing";
                messageType = Message.MessageType.ERROR;
            }
            case 4 -> {
                response = "watson.message.ledger.cannot_execute";
                messageType = Message.MessageType.WARNING;
            }
            default -> {
                response = "watson.message.ledger.unknown";
                messageType = Message.MessageType.ERROR;
            }
        }

        InfoUtils.showGuiOrInGameMessage(messageType, response, identifier.getPath());
    }

    @Override
    public void encodeWithSplitter(PacketByteBuf buf, ClientPlayNetworkHandler handler)
    {
        // NO-OP
    }

    @Override
    public void receivePlayPayload(T payload, ClientPlayNetworking.Context context)
    {
        INSTANCE.decodePayload(((PluginResponsePacket.Payload)payload).content());
    }

}
