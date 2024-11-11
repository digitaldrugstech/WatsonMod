package eu.minemania.watson.network.ledger.purge;

import eu.minemania.watson.Watson;
import eu.minemania.watson.config.Configs;
import eu.minemania.watson.data.LedgerSearch;
import eu.minemania.watson.gui.GuiLedger.ButtonListenerRolledback.RolledbackMode;
import fi.dy.masa.malilib.network.IPluginClientPlayHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public abstract class PluginPurgePacketHandler<T extends CustomPayload> implements IPluginClientPlayHandler<T>
{
    public static final Identifier CHANNEL = new Identifier("ledger","purge");
    private boolean registered = false;

    private final static PluginPurgePacketHandler<PluginPurgePacket> INSTANCE = new PluginPurgePacketHandler<>()
    {
        @Override
        public void receive(PluginPurgePacket payload, ClientPlayNetworking.Context context)
        {
            this.receivePlayPayload(payload, context);
        }
    };

    public static PluginPurgePacketHandler<PluginPurgePacket> getInstance()
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

    public void encodePayload(List<String> action, List<String> dimension, List<String> block, List<String> entityType, List<String> item, List<String> tag, int range, String source, String timeBefore, String timeAfter)
    {
        LedgerSearch ledgerSearch = new LedgerSearch(action, dimension, block, entityType, item, tag, range, source, timeBefore, timeAfter, RolledbackMode.IGNORED);
        if(Configs.Generic.DEBUG.getBooleanValue())
        {
            Watson.logger.info("action: " + ledgerSearch.getActions());
            Watson.logger.info("dimension: " + ledgerSearch.getDimensions());
            Watson.logger.info("object Block: " + ledgerSearch.getBlocks());
            Watson.logger.info("object Item: " + ledgerSearch.getItems());
            Watson.logger.info("object EntityType: " + ledgerSearch.getEntityTypes());
            Watson.logger.info("object Tag: " + ledgerSearch.getTags());
            Watson.logger.info("range: " + ledgerSearch.getRange());
            Watson.logger.info("source: " + ledgerSearch.getSources());
            Watson.logger.info("timeBefore: " + ledgerSearch.getTimeBefore());
            Watson.logger.info("timeAfter: " + ledgerSearch.getTimeAfter());
            Watson.logger.info("search: " + ledgerSearch.getSearchData());
            Watson.logger.info(CHANNEL);
        }
        INSTANCE.sendPlayPayload(new PluginPurgePacket(ledgerSearch.getSearchData()));
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
