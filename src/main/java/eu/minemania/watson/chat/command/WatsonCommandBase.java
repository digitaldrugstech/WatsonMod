package eu.minemania.watson.chat.command;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WatsonCommandBase
{
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
