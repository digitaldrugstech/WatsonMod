package eu.minemania.watson.network.ledger.inspect;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;


public record PluginInspectPacket(BlockPos pos, int pages) implements CustomPayload
{
    public static final Id<PluginInspectPacket> ID = new Id<>(PluginInspectPacketHandler.CHANNEL);
    public static final PacketCodec<PacketByteBuf, PluginInspectPacket> CODEC = CustomPayload.codecOf(PluginInspectPacket::write, null);

    public void write(PacketByteBuf output)
    {
        output.writeBlockPos(pos);
        output.writeInt(pages);
    }

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}
