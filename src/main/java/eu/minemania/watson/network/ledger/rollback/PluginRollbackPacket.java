package eu.minemania.watson.network.ledger.rollback;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record PluginRollbackPacket(boolean restore, String searchData) implements CustomPayload
{
    public static final Id<PluginRollbackPacket> ID = new Id<>(PluginRollbackPacketHandler.CHANNEL);
    public static final PacketCodec<PacketByteBuf, PluginRollbackPacket> CODEC = CustomPayload.codecOf(PluginRollbackPacket::write, null);

    public void write(PacketByteBuf output)
    {
        output.writeBoolean(restore);
        output.writeString(searchData);
    }

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}
