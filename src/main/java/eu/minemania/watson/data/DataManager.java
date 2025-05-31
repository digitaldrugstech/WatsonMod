package eu.minemania.watson.data;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import eu.minemania.watson.Reference;
import eu.minemania.watson.Watson;
import eu.minemania.watson.chat.ChatMessage;
import eu.minemania.watson.config.Configs;
import eu.minemania.watson.db.BlockEditSet;
import eu.minemania.watson.db.Filters;
import eu.minemania.watson.db.LedgerInfo;
import eu.minemania.watson.db.TimeStamp;
import eu.minemania.watson.gui.GuiConfigs.ConfigGuiTab;
import eu.minemania.watson.network.ledger.action.PluginActionPacket;
import eu.minemania.watson.network.ledger.action.PluginActionPacketHandler;
import eu.minemania.watson.network.ledger.handshake.PluginHandshakePacket;
import eu.minemania.watson.network.ledger.handshake.PluginHandshakePacketHandler;
import eu.minemania.watson.network.ledger.inspect.PluginInspectPacket;
import eu.minemania.watson.network.ledger.inspect.PluginInspectPacketHandler;
import eu.minemania.watson.network.ledger.purge.PluginPurgePacket;
import eu.minemania.watson.network.ledger.purge.PluginPurgePacketHandler;
import eu.minemania.watson.network.ledger.response.PluginResponsePacket;
import eu.minemania.watson.network.ledger.response.PluginResponsePacketHandler;
import eu.minemania.watson.network.ledger.rollback.PluginRollbackPacket;
import eu.minemania.watson.network.ledger.rollback.PluginRollbackPacketHandler;
import eu.minemania.watson.network.ledger.search.PluginSearchPacket;
import eu.minemania.watson.network.ledger.search.PluginSearchPacketHandler;
import eu.minemania.watson.network.watson.world.PluginWorldPacket;
import eu.minemania.watson.network.watson.world.PluginWorldPacketHandler;
import eu.minemania.watson.selection.EditSelection;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.gui.interfaces.IDirectoryCache;
import fi.dy.masa.malilib.network.ClientPlayHandler;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;

public class DataManager implements IDirectoryCache
{
    private static final DataManager INSTANCE = new DataManager();

    protected static final Pattern DATE_PATTERN = Pattern.compile("^(\\d{4})-(\\d{1,2})-(\\d{1,2})$");
    protected static final Pattern ABSOLUTE_TIME = Pattern.compile("(\\d{1,2})-(\\d{1,2}) (\\d{1,2}):(\\d{1,2}):(\\d{1,2})");
    private static final Map<String, File> LAST_DIRECTORIES = new HashMap<>();
    private static final ArrayList<String> setNames = new ArrayList<>();

    private static ConfigGuiTab configGuiTab = ConfigGuiTab.GENERIC;
    private static boolean canSave;
    private static long clientTickStart;
    private static String worldName = "";
    private static String ledgerVersion = "";
    private static LedgerInfo ledgerInfo;
    private static final ArrayList<String> ledgerActions = new ArrayList<>();

    private final EditSelection editselection = new EditSelection();

    protected Filters filters = new Filters();

    private final static PluginActionPacketHandler<PluginActionPacket.Payload> ACTION = PluginActionPacketHandler.getInstance();
    private final static PluginHandshakePacketHandler<PluginHandshakePacket.Payload> HANDSHAKE = PluginHandshakePacketHandler.getInstance();
    private final static PluginInspectPacketHandler<PluginInspectPacket> INSPECT = PluginInspectPacketHandler.getInstance();
    private final static PluginPurgePacketHandler<PluginPurgePacket> PURGE = PluginPurgePacketHandler.getInstance();
    private final static PluginResponsePacketHandler<PluginResponsePacket.Payload> RESPONSE = PluginResponsePacketHandler.getInstance();
    private final static PluginRollbackPacketHandler<PluginRollbackPacket> ROLLBACK = PluginRollbackPacketHandler.getInstance();
    private final static PluginSearchPacketHandler<PluginSearchPacket> SEARCH = PluginSearchPacketHandler.getInstance();
    private final static PluginWorldPacketHandler<PluginWorldPacket.Payload> WORLD = PluginWorldPacketHandler.getInstance();

    private DataManager()
    {

    }

    private static DataManager getInstance()
    {
        return INSTANCE;
    }

    public static IDirectoryCache getDirectoryCache()
    {
        return INSTANCE;
    }

    public static void setClientTick(long time)
    {
        clientTickStart = time;
    }

    public static void onClientTickStart()
    {
        clientTickStart = System.currentTimeMillis();
    }

    public static long getClientTickStartTime()
    {
        return clientTickStart;
    }

    public static ConfigGuiTab getConfigGuiTab()
    {
        return configGuiTab;
    }

    public static void setConfigGuiTab(ConfigGuiTab tab)
    {
        configGuiTab = tab;
    }

    public static EditSelection getEditSelection()
    {
        return getInstance().editselection;
    }

    public static Filters getFilters()
    {
        return getInstance().filters;
    }

    public static void setWorldPlugin(String world)
    {
        worldName = world;
    }

    public static String getWorldPlugin()
    {
        return worldName;
    }

    public static void setLedgerVersion(String version)
    {
        ledgerVersion = version;
    }

    public static String getLedgerVersion()
    {
        return ledgerVersion;
    }

    public static void setLedgerActions(List<String> allowedLedgerActions)
    {
        ledgerActions.addAll(allowedLedgerActions);
    }

    public static ArrayList<String> getLedgerActions()
    {
        return ledgerActions;
    }

    @Override
    @Nullable
    public File getCurrentDirectoryForContext(String context)
    {
        return LAST_DIRECTORIES.get(context);
    }

    @Override
    public void setCurrentDirectoryForContext(String context, File dir)
    {
        LAST_DIRECTORIES.put(context, dir);
    }

    public static void load()
    {
        Path file = getCurrentStorageFile();

        JsonElement element = JsonUtils.parseJsonFileAsPath(file);

        if (element != null && element.isJsonObject())
        {
            LAST_DIRECTORIES.clear();

            JsonObject root = element.getAsJsonObject();

            if (JsonUtils.hasObject(root, "last_directories"))
            {
                JsonObject obj = root.get("last_directories").getAsJsonObject();

                for (Map.Entry<String, JsonElement> entry : obj.entrySet())
                {
                    String name = entry.getKey();
                    JsonElement el = entry.getValue();

                    if (el.isJsonPrimitive())
                    {
                        File dir = new File(el.getAsString());

                        if (dir.exists() && dir.isDirectory())
                        {
                            LAST_DIRECTORIES.put(name, dir);
                        }
                    }
                }
            }

            if (JsonUtils.hasString(root, "config_gui_tab"))
            {
                try
                {
                    configGuiTab = ConfigGuiTab.valueOf(root.get("config_gui_tab").getAsString());
                }
                catch (Exception ignored)
                {
                }

                if (configGuiTab == null)
                {
                    configGuiTab = ConfigGuiTab.GENERIC;
                }
            }
        }

        canSave = true;
    }

    public static void save()
    {
        save(false);
    }

    public static void save(boolean forceSave)
    {
        if (!canSave && !forceSave)
        {
            return;
        }

        JsonObject root = new JsonObject();
        JsonObject objDirs = new JsonObject();

        for (Map.Entry<String, File> entry : LAST_DIRECTORIES.entrySet())
        {
            objDirs.add(entry.getKey(), new JsonPrimitive(entry.getValue().getAbsolutePath()));
        }

        root.add("last_directories", objDirs);

        root.add("config_gui_tab", new JsonPrimitive(configGuiTab.name()));

        Path file = getCurrentStorageFile();
        JsonUtils.writeJsonToFileAsPath(root, file);

        canSave = false;
    }

    public static Path getCurrentConfigDirectory()
    {
        return FileUtils.getConfigDirectoryAsPath().resolve(Reference.MOD_ID);
    }

    public static File getPlayereditsBaseDirectory()
    {
        File dir = FileUtils.getCanonicalFileIfPossible(new File(FileUtils.getMinecraftDirectoryAsPath().toFile(), "playeredits"));

        if (!dir.exists() && !dir.mkdirs())
        {
            Watson.logger.warn("Failed to create the playeredit directory '{}'", dir.getAbsolutePath());
        }

        return dir;
    }

    private static Path getCurrentStorageFile()
    {
        Path dir = getCurrentConfigDirectory();

        if (!Files.exists(dir))
        {
            FileUtils.createDirectoriesIfMissing(dir);
        }

        if (!Files.isDirectory(dir))
        {
            Watson.logger.warn("Failed to create the config directory '{}'", dir.toAbsolutePath());
        }

        return dir.resolve(getStorageFileName());
    }

    private static String getStorageFileName()
    {
        String name = StringUtils.getWorldOrServerName();

        if (name == null)
        {
            return Reference.MOD_ID + "_default.json";
        }

        return Reference.MOD_ID + "_" + name + ".json";
    }

    public static String getServerIP()
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        ServerInfo serverData = mc.getCurrentServerEntry();
        if (!mc.isInSingleplayer() && serverData != null)
        {
            return serverData.address;
        }
        else
        {
            return null;
        }
    }

    public static void saveBlockEditFile(String fileName)
    {
        PlayerEntity playerEntity = MinecraftClient.getInstance().player;
        if (playerEntity == null)
        {
            return;
        }
        if (fileName == null)
        {
            String player = (String) getEditSelection().getVariables().get("player");
            if (player == null)
            {
                ChatMessage.localErrorT("watson.message.blockedit.no_player");
                return;
            }
            else
            {
                Calendar calendar = Calendar.getInstance();
                fileName = String.format("%s-%4d-%02d-%02d-%02d.%02d.%02d", player, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
            }
        }

        File file = new File(getPlayereditsBaseDirectory(), fileName + ".txt");
        try
        {
            BlockEditSet edits = DataManager.getEditSelection().getBlockEditSet();
            int editCount = edits.save(file);
            int annoCount = edits.getAnnotations().size();
            StringUtils.sendOpenFileChatMessage(playerEntity, "%s", file);
            ChatMessage.localOutputT("watson.message.blockedit.edits_annotations.saved", editCount, annoCount, fileName);
        }
        catch (IOException e)
        {
            Watson.logger.error("error saving BlockEditSet to " + file, e);
            ChatMessage.localErrorT("watson.message.blockedit.not_saved", fileName);
        }
    }

    public static void loadBlockEditFile(String fileName)
    {
        File file = new File(getPlayereditsBaseDirectory(), fileName + ".txt");
        if (!file.canRead())
        {
            File[] files = getInstance().getBlockEditFileList(fileName);
            if (files.length > 0)
            {
                file = files[files.length - 1];
            }
        }

        if (file.canRead())
        {
            try
            {
                BlockEditSet edits = DataManager.getEditSelection().getBlockEditSet();
                int editCount = edits.load(file);
                int annoCount = edits.getAnnotations().size();
                ChatMessage.localOutputT("watson.message.blockedit.edits_annotations.loaded", editCount, annoCount, file.getName());
            }
            catch (Exception e)
            {
                Watson.logger.error("error loading BlockEditSet from " + file, e);
                ChatMessage.localErrorT("watson.message.blockedit.not_loaded", fileName);
            }
        }
        else
        {
            ChatMessage.localErrorT("watson.message.blockedit.not_read", fileName);
        }
    }

    public static void listBlockEditFiles(String prefix, int page)
    {
        File[] files = getInstance().getBlockEditFileList(prefix);
        if (files.length == 0)
        {
            ChatMessage.localOutputT("watson.message.blockedit.not_match");
        }
        else
        {
            if (files.length == 1)
            {
                ChatMessage.localOutputT("watson.message.blockedit.match_file.1");
            }
            else
            {
                ChatMessage.localOutputT("watson.message.blockedit.match_file.more", files.length);
            }

            int pages = (files.length + Configs.Plugin.PAGE_LINES.getIntegerValue() - 1) / Configs.Plugin.PAGE_LINES.getIntegerValue();
            if (page > pages)
            {
                ChatMessage.localErrorT("watson.message.blockedit.highest_page", page);
            }
            else
            {
                ChatMessage.localOutputT("watson.message.blockedit.pages", page, pages);

                int start = (page - 1) * Configs.Plugin.PAGE_LINES.getIntegerValue();
                int end = Math.min(files.length, page * Configs.Plugin.PAGE_LINES.getIntegerValue());

                for (int i = start; i < end; ++i)
                {
                    ChatMessage.localOutput("     " + files[i].getName(), true);
                }

                ChatMessage.localOutputT("watson.message.blockedit.pages", page, pages);
                if (page < pages)
                {
                    ChatMessage.localOutputT("watson.message.blockedit.next_page", Configs.Generic.WATSON_PREFIX.getStringValue(), prefix, (page + 1));
                }
            }
        }
    }

    public static void deleteBlockEditFiles(String prefix)
    {
        File[] files = getInstance().getBlockEditFileList(prefix);
        if (files.length > 0)
        {
            int failed = 0;
            for (File file : files)
            {
                if (file.delete())
                {
                    ChatMessage.localOutputT("watson.message.blockedit.deleted", file.getName());
                }
                else
                {
                    ++failed;
                }
            }
            String message = "watson.message.blockedit.deleted_matching";
            if (failed == 0)
            {
                ChatMessage.localOutputT(message, (files.length - failed), files.length, prefix);
            }
            else
            {
                ChatMessage.localErrorT(message, (files.length - failed), files.length, prefix);
            }
        }
        else
        {
            ChatMessage.localOutputT("watson.message.blockedit.no_matching", prefix);
        }
    }

    @SuppressWarnings("MagicConstant")
    public static void expireBlockEditFiles(String date)
    {
        Matcher m = DATE_PATTERN.matcher(date);
        if (m.matches())
        {
            Calendar expiry = Calendar.getInstance();
            long expiryTime;
            try
            {
                int year = Integer.parseInt(m.group(1));
                int month = Integer.parseInt(m.group(2));
                int day = Integer.parseInt(m.group(3));

                expiry.setLenient(false);
                expiry.set(year, month - 1, day, 0, 0);

                expiryTime = expiry.getTimeInMillis();
            }
            catch (Exception e)
            {
                ChatMessage.localErrorT("watson.message.blockedit.date_not_valid", date);
                return;
            }

            int deleted = 0;
            int failed = 0;
            File[] files = getInstance().getBlockEditFileList("*");
            for (File file : files)
            {
                if (file.lastModified() < expiryTime)
                {
                    if (file.delete())
                    {
                        ++deleted;
                        ChatMessage.localOutputT("watson.message.blockedit.deleted", file.getName());
                    }
                    else
                    {
                        ++failed;
                        ChatMessage.localErrorT("watson.message.blockedit.not_delete", file.getName());
                    }
                }
            }
            if (deleted + failed == 0)
            {
                ChatMessage.localOutputT("watson.message.blockedit.nothing_between", date);
            }
            else
            {
                String message = "watson.message.blockedit.deleted_older";
                if (failed == 0)
                {
                    ChatMessage.localOutputT(message, deleted, deleted + failed, date);
                }
                else
                {
                    ChatMessage.localErrorT(message, deleted, deleted + failed, date);
                }
            }
        }
        else
        {
            ChatMessage.localErrorT("watson.message.blockedit.date_form");
        }
    }

    public File[] getBlockEditFileList(String prefix)
    {
        File[] files = getPlayereditsBaseDirectory().listFiles(new CaseInsensitivePrefixFileFilter(prefix));
        if (files != null)
        {
            Arrays.sort(files);
        }
        return files;
    }

    public static long getTimeDiff(String time)
    {
        Matcher absolute = ABSOLUTE_TIME.matcher(time);
        if (absolute.matches())
        {
            int month = Integer.parseInt(absolute.group(1));
            int day = Integer.parseInt(absolute.group(2));
            int hour = Integer.parseInt(absolute.group(3));
            int minute = Integer.parseInt(absolute.group(4));
            int second = Integer.parseInt(absolute.group(5));
            if (month != 0 || day != 0 || hour != 0 || minute != 0 || second != 0)
            {
                return TimeStamp.timeDiff(month, day, hour, minute, second);
            }
            else
            {
                return 0;
            }
        }
        else
        {
            InfoUtils.showGuiOrInGameMessage(Message.MessageType.ERROR, "watson.gui.label.blockedit.info.format");
            return -1;
        }
    }

    public static class CaseInsensitivePrefixFileFilter implements FileFilter
    {
        protected String _lowerPrefix;

        public CaseInsensitivePrefixFileFilter(String prefix)
        {
            _lowerPrefix = (prefix == null || prefix.equals("*")) ? "" : prefix.toLowerCase();
        }

        @Override
        public boolean accept(File file)
        {
            return file.isFile() && file.canRead() && (_lowerPrefix.isEmpty() || file.getName().toLowerCase().startsWith(_lowerPrefix));
        }
    }

    public static ArrayList<String> getAllItemEntitiesStringIdentifiers()
    {
        if (!setNames.isEmpty())
        {
            return setNames;
        }

        setNames.addAll(getBlocks());
        setNames.addAll(getItems());
        setNames.addAll(getEntityTypes());

        return setNames;
    }

    public static ArrayList<String> getBlocks()
    {
        ArrayList<String> blocks = new ArrayList<>();

        Registries.BLOCK.forEach(block -> blocks.add(Registries.BLOCK.getId(block).toString()));

        blocks.sort(String::compareTo);

        return blocks;
    }

    public static ArrayList<String> getItems()
    {
        ArrayList<String> items = new ArrayList<>();

        Registries.ITEM.forEach(item -> items.add(Registries.ITEM.getId(item).toString()));

        items.sort(String::compareTo);

        return items;
    }

    public static ArrayList<String> getEntityTypes()
    {
        ArrayList<String> entityTypes = new ArrayList<>();

        Registries.ENTITY_TYPE.forEach(entityType -> entityTypes.add(Registries.ENTITY_TYPE.getId(entityType).toString()));

        entityTypes.sort(String::compareTo);

        return entityTypes;
    }

    public static ArrayList<String> getTags()
    {
        ArrayList<String> tags = new ArrayList<>();
        ArrayList<String> deDupTags = new ArrayList<>();

        Registries.BLOCK.streamTags().forEach((block) -> tags.add("#"+block.id().toString()));
        Registries.ENTITY_TYPE.streamTags().forEach((entity) -> tags.add("#"+entity.id().toString()));
        Registries.ITEM.streamTags().forEach((item) -> tags.add("#"+item.id().toString()));

        for (String tag : tags) {
            if (!deDupTags.contains(tag)) {
                deDupTags.add(tag);
            }
        }

        deDupTags.sort(String::compareTo);

        return deDupTags;
    }

    public static void setLedgerInfo(LedgerInfo ledgerInfo)
    {
        DataManager.ledgerInfo = ledgerInfo;
    }

    public static LedgerInfo getLedgerInfo()
    {
        return ledgerInfo;
    }

    public static void reset(boolean isLogout)
    {
        if (isLogout)
        {
            ACTION.reset(ACTION.getPayloadChannel());
            HANDSHAKE.reset(HANDSHAKE.getPayloadChannel());
            INSPECT.reset(INSPECT.getPayloadChannel());
            PURGE.reset(PURGE.getPayloadChannel());
            RESPONSE.reset(RESPONSE.getPayloadChannel());
            ROLLBACK.reset(ROLLBACK.getPayloadChannel());
            SEARCH.reset(SEARCH.getPayloadChannel());
            WORLD.reset(WORLD.getPayloadChannel());
        }
    }

    public static void onWorldPre()
    {
        ACTION.registerPlayReceiver(PluginActionPacket.Payload.ID, ACTION::receivePlayPayload);
        HANDSHAKE.registerPlayReceiver(PluginHandshakePacket.Payload.ID, HANDSHAKE::receivePlayPayload);
        RESPONSE.registerPlayReceiver(PluginResponsePacket.Payload.ID, RESPONSE::receivePlayPayload);
        WORLD.registerPlayReceiver(PluginWorldPacket.Payload.ID, WORLD::receivePlayPayload);
    }

    public static void onWorldJoin()
    {
        HANDSHAKE.encodePayload(new PluginHandshakePacket(Reference.LEDGER_PROTOCOL, Reference.MOD_VERSION, Reference.MOD_ID));
    }

    public static void registerPayloads()
    {
        ClientPlayHandler.getInstance().registerClientPlayHandler(ACTION);
        ClientPlayHandler.getInstance().registerClientPlayHandler(HANDSHAKE);
        ClientPlayHandler.getInstance().registerClientPlayHandler(INSPECT);
        ClientPlayHandler.getInstance().registerClientPlayHandler(PURGE);
        ClientPlayHandler.getInstance().registerClientPlayHandler(RESPONSE);
        ClientPlayHandler.getInstance().registerClientPlayHandler(ROLLBACK);
        ClientPlayHandler.getInstance().registerClientPlayHandler(SEARCH);
        ClientPlayHandler.getInstance().registerClientPlayHandler(WORLD);

        ACTION.registerPlayPayload(PluginActionPacket.Payload.ID, PluginActionPacket.Payload.CODEC, PluginActionPacketHandler.TO_CLIENT);
        HANDSHAKE.registerPlayPayload(PluginHandshakePacket.Payload.ID, PluginHandshakePacket.Payload.CODEC, PluginHandshakePacketHandler.BOTH_SERVER);
        INSPECT.registerPlayPayload(PluginInspectPacket.ID, PluginInspectPacket.CODEC, PluginInspectPacketHandler.TO_SERVER);
        PURGE.registerPlayPayload(PluginPurgePacket.ID, PluginPurgePacket.CODEC, PluginPurgePacketHandler.TO_SERVER);
        RESPONSE.registerPlayPayload(PluginResponsePacket.Payload.ID, PluginResponsePacket.Payload.CODEC, PluginResponsePacketHandler.TO_CLIENT);
        ROLLBACK.registerPlayPayload(PluginRollbackPacket.ID, PluginRollbackPacket.CODEC, PluginRollbackPacketHandler.TO_SERVER);
        SEARCH.registerPlayPayload(PluginSearchPacket.ID, PluginSearchPacket.CODEC, PluginSearchPacketHandler.TO_SERVER);
        WORLD.registerPlayPayload(PluginWorldPacket.Payload.ID, PluginWorldPacket.Payload.CODEC, PluginWorldPacketHandler.TO_CLIENT);
    }

    public static PluginInspectPacketHandler<PluginInspectPacket> getInspectHandler()
    {
        return INSPECT;
    }

    public static PluginPurgePacketHandler<PluginPurgePacket> getPurgeHandler()
    {
        return PURGE;
    }

    public static PluginSearchPacketHandler<PluginSearchPacket> getSearchHandler()
    {
        return SEARCH;
    }

    public static PluginRollbackPacketHandler<PluginRollbackPacket> getRollbackHandler()
    {
        return ROLLBACK;
    }
}