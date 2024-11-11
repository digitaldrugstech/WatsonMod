package eu.minemania.watson.network.watson.world;

import com.google.common.base.Charsets;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class PluginWorldPacket
{
    private String world;

    public PluginWorldPacket(String world, PacketByteBuf buf)
    {
        this.world = world;
        buf.readBytes(buf.readableBytes());
    }

    public String getWorld()
    {
        return this.world;
    }

    public record Payload(PluginWorldPacket content) implements CustomPayload
    {
        public static final Id<Payload> ID = new Id<>(PluginWorldPacketHandler.CHANNEL);
        public static final PacketCodec<PacketByteBuf, Payload> CODEC = CustomPayload.codecOf(null, Payload::new);

        public Payload(PacketByteBuf buf)
        {
            this(new PluginWorldPacket(buf.toString(Charsets.UTF_8), buf));
        }

        @Override
        public Id<Payload> getId()
        {
            return ID;
        }
    }
}