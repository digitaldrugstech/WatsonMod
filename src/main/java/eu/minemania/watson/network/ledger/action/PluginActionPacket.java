package eu.minemania.watson.network.ledger.action;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class PluginActionPacket
{
    private BlockPos blockPos;
    private String type;
    private Identifier dim;
    private Identifier oldObj;
    private Identifier newObj;
    private String source;
    private long time;
    private boolean rolledBack;
    private String additionalData;

    public PluginActionPacket(BlockPos blockPos, String type, Identifier dim, Identifier oldObj, Identifier newObj, String source, long time, boolean rolledBack, String additionalData)
    {
        this.blockPos = blockPos;
        this.type = type;
        this.dim = dim;
        this.oldObj = oldObj;
        this.newObj = newObj;
        this.source = source;
        this.time = time;
        this.rolledBack = rolledBack;
        this.additionalData = additionalData;
    }

    public BlockPos getBlockPos()
    {
        return this.blockPos;
    }

    public String getType()
    {
        return this.type;
    }

    public Identifier getDimension()
    {
        return this.dim;
    }

    public Identifier getOldObject()
    {
        return this.oldObj;
    }

    public Identifier getNewObject()
    {
        return this.newObj;
    }

    public String getSource()
    {
        return this.source;
    }

    public long getTime()
    {
        return this.time;
    }

    public boolean isRolledBack()
    {
        return this.rolledBack;
    }

    public String getAdditionalData()
    {
        return this.additionalData;
    }

    public record Payload(PluginActionPacket content) implements CustomPayload
    {
        public static final Id<Payload> ID = new Id<>(PluginActionPacketHandler.CHANNEL);
        public static final PacketCodec<PacketByteBuf, Payload> CODEC = CustomPayload.codecOf(null, Payload::new);

        public Payload(PacketByteBuf input)
        {
            this(new PluginActionPacket(input.readBlockPos(), input.readString(), input.readIdentifier(), input.readIdentifier(), input.readIdentifier(), input.readString(), input.readLong(), input.readBoolean(), input.readString()));
        }

        @Override
        public Id<Payload> getId()
        {
            return ID;
        }
    }
}
