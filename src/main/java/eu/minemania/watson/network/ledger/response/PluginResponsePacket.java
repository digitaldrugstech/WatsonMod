package eu.minemania.watson.network.ledger.response;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class PluginResponsePacket
{
    private Identifier identifier;
    private int responseCode;

    public PluginResponsePacket(Identifier identifier, int responseCode)
    {
        this.identifier = identifier;
        this.responseCode = responseCode;
    }

    public Identifier getIdentifier()
    {
        return this.identifier;
    }

    public int getResponseCode()
    {
        return this.responseCode;
    }

    public record Payload(PluginResponsePacket content) implements CustomPayload
    {
        public static final Id<Payload> ID = new Id<>(PluginResponsePacketHandler.CHANNEL);
        public static final PacketCodec<PacketByteBuf, Payload> CODEC = CustomPayload.codecOf(null, Payload::new);

        public Payload(PacketByteBuf input)
        {
            this(new PluginResponsePacket(input.readIdentifier(), input.readInt()));
        }

        @Override
        public Id<Payload> getId()
        {
            return ID;
        }
    }
}
