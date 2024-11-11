package eu.minemania.watson.network.ledger.handshake;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

public class PluginHandshakePacket
{
    private Integer protocolVersion;
    private String modId;
    private String version;
    private List<String> actions;

    public PluginHandshakePacket(Integer protocolVersion, String ledgerVersion, List<String> actions)
    {
        this.protocolVersion = protocolVersion;
        this.version = ledgerVersion;
        this.actions = actions;
        this.modId = "";
    }

    public PluginHandshakePacket(Integer protocolVersion, String version, String modId)
    {
        this.protocolVersion = protocolVersion;
        this.version = version;
        this.modId = modId;
        this.actions = List.of();
    }

    public Integer getProtocolVersion()
    {
        return this.protocolVersion;
    }

    public String getVersion()
    {
        return this.version;
    }

    public List<String> getActions()
    {
        return this.actions;
    }

    public NbtCompound toNbt()
    {
        NbtCompound result = new NbtCompound();

        result.putString("modid", this.modId);
        result.putString("version", this.version);
        result.putInt("protocol_version", this.protocolVersion);

        return result;
    }

    public record Payload(PluginHandshakePacket content) implements CustomPayload
    {
        public static final Id<Payload> ID = new Id<>(PluginHandshakePacketHandler.CHANNEL);
        public static final PacketCodec<PacketByteBuf, Payload> CODEC = CustomPayload.codecOf(Payload::write, Payload::new);

        public Payload(PacketByteBuf input)
        {
            this(new PluginHandshakePacket(input.readInt(), input.readString(), actionsList(input)));
        }

        public void write(PacketByteBuf output)
        {
            output.writeNbt(content.toNbt());
        }

        @Override
        public Id<Payload> getId()
        {
            return ID;
        }
    }

    private static List<String> actionsList(PacketByteBuf buf)
    {
        int totalActions = buf.readInt();
        List<String> actionsList = new ArrayList<>();
        if (totalActions > 0)
        {
            for (int i = 0; i < totalActions; i++)
            {
                String action = buf.readString();
                actionsList.add(action);
            }
        }
        return actionsList;
    }
}
