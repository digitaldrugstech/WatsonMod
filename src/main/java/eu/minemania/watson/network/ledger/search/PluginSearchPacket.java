package eu.minemania.watson.network.ledger.search;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record PluginSearchPacket(String searchData, int pages) implements CustomPayload
{
    public static final Id<PluginSearchPacket> ID = new Id<>(PluginSearchPacketHandler.CHANNEL);
    public static final PacketCodec<PacketByteBuf, PluginSearchPacket> CODEC = CustomPayload.codecOf(PluginSearchPacket::write, null);

    public void write(PacketByteBuf output)
    {
        output.writeString(searchData);
        output.writeInt(pages);
    }

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}
