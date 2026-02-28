package eu.minemania.watson.chat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public class WatsonCommandBase
{
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static int printCommandUsage()
    {
        int cmdCount = 0;
        CommandDispatcher rawDispatcher = Command.commandDispatcher;
        if (rawDispatcher == null) return 0;

        Object source = null;
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
        if (handler != null)
        {
            source = handler.getCommandSource();
        }

        for (Object child : rawDispatcher.getRoot().getChildren())
        {
            CommandNode<ServerCommandSource> command = (CommandNode<ServerCommandSource>) child;
            String cmdName = command.getName();
            if (ClientCommandManager.isClientSideCommand(cmdName))
            {
                Map<CommandNode<ServerCommandSource>, String> usage = rawDispatcher.getSmartUsage(command, source);
                for (String u : usage.values())
                {
                    ClientCommandManager.sendFeedback(Text.literal("/" + cmdName + " " + u));
                }
                cmdCount += usage.size();
                if (usage.isEmpty())
                {
                    ClientCommandManager.sendFeedback(Text.literal("/" + cmdName));
                    cmdCount++;
                }
            }
        }
        return cmdCount;
    }

    public static void localOutput(String message)
    {
        MutableText chat = Text.literal(message);
        chat.formatted(Formatting.AQUA);
        ClientCommandManager.sendFeedback(chat);
    }

    public static void localOutputT(String translationKey, Object... args)
    {
        MutableText chat = Text.translatable(translationKey, args);
        chat.formatted(Formatting.AQUA);
        ClientCommandManager.sendFeedback(chat);
    }

    public static void localError(String message)
    {
        MutableText chat = Text.literal(message);
        chat.formatted(Formatting.DARK_RED);
        ClientCommandManager.sendFeedback(chat);
    }

    public static void localErrorT(String translationKey, Object... args)
    {
        MutableText chat = Text.translatable(translationKey, args);
        chat.formatted(Formatting.DARK_RED);
        ClientCommandManager.sendFeedback(chat);
    }
}
