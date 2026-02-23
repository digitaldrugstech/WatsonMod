package eu.minemania.watson.analysis;

import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import eu.minemania.watson.Watson;
import eu.minemania.watson.chat.IChatHandler;
import eu.minemania.watson.chat.IMatchedChatHandler;
import eu.minemania.watson.config.Configs;
import fi.dy.masa.malilib.config.options.ConfigString;
import net.minecraft.text.MutableText;

public class Analysis implements IChatHandler
{
    protected static final ListMultimap<String, IMatchedChatHandler> m =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private static final ConcurrentHashMap<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();
    public static int colorBlock = 0;

    private static final Pattern FORMATTING_PATTERN = Pattern.compile("\u00A7.");

    public boolean dispatchMatchedChat(MutableText chat)
    {
        String unformatted = chat.getString();
        unformatted = FORMATTING_PATTERN.matcher(unformatted).replaceAll("");
        if (Configs.Generic.DEBUG.getBooleanValue())
        {
            Watson.logger.info("unformatted: " + unformatted);
        }
        synchronized (m)
        {
            for (Entry<String, IMatchedChatHandler> entry : m.entries())
            {
                if (Configs.Generic.DEBUG.getBooleanValue())
                {
                    Watson.logger.info("key: " + entry.getKey());
                }
                Pattern pattern = PATTERN_CACHE.computeIfAbsent(entry.getKey(), Pattern::compile);
                Matcher matcher = pattern.matcher(unformatted);
                if (matcher.find())
                {
                    if (Configs.Generic.DEBUG.getBooleanValue())
                    {
                        Watson.logger.info("key matched: " + entry.getKey());
                    }
                    return entry.getValue().onMatchedChat(chat, matcher);
                }
            }
        }
        return true;
    }

    public void addMatchedChatHandler(ConfigString pattern, IMatchedChatHandler handler)
    {
        m.put(pattern.getStringValue(), handler);
    }

    public static void removeMatchedChatHandler(ConfigString pattern)
    {
        String oldKey = pattern.getOldStringValue();
        String newKey = pattern.getStringValue();
        if (oldKey.equals(newKey))
        {
            return;
        }
        synchronized (m)
        {
            List<IMatchedChatHandler> handlers = new ArrayList<>(m.get(oldKey));
            m.removeAll(oldKey);
            for (IMatchedChatHandler handler : handlers)
            {
                m.put(newKey, handler);
            }
        }
        PATTERN_CACHE.remove(oldKey);
    }

    @Override
    public boolean onChat(MutableText chat)
    {
        return dispatchMatchedChat(chat);
    }
}