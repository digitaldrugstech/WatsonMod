package eu.minemania.watson.network.ledger.purge;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record PluginPurgePacket(String searchData) implements CustomPayload
{
    public static final Id<PluginPurgePacket> ID = new Id<>(PluginPurgePacketHandler.CHANNEL);
    public static final PacketCodec<PacketByteBuf, PluginPurgePacket> CODEC = CustomPayload.codecOf(PluginPurgePacket::write, null);

    public void write(PacketByteBuf output)
    {
        output.writeString(searchData);
    }

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}
