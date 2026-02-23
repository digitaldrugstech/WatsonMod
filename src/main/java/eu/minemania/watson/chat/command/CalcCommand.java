package eu.minemania.watson.chat.command;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CalcCommand extends WatsonCommandBase
{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        ClientCommandManager.addClientSideCommand("calc");
        LiteralArgumentBuilder<ServerCommandSource> calc = literal("calc").executes(CalcCommand::help)
                .then(literal("help").executes(CalcCommand::help))
                .then(argument("calculation", greedyString()).executes(CalcCommand::calc));
        dispatcher.register(calc);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int help(CommandContext<ServerCommandSource> context)
    {
        int cmdCount = 0;
        // Cast to raw CommandDispatcher to avoid checkcast of ClientCommandSource to ServerCommandSource.
        // Watson's commands use the default requirement (s -> true) which ignores the source, so null is safe.
        CommandDispatcher rawDispatcher = Command.commandDispatcher;
        for (Object child : rawDispatcher.getRoot().getChildren())
        {
            CommandNode<ServerCommandSource> command = (CommandNode<ServerCommandSource>) child;
            String cmdName = command.getName();
            if (ClientCommandManager.isClientSideCommand(cmdName))
            {
                Map<CommandNode<ServerCommandSource>, String> usage = rawDispatcher.getSmartUsage(command, null);
                for (String u : usage.values())
                {
                    ClientCommandManager.sendFeedback(Text.literal("/" + cmdName + " " + u));
                }
                cmdCount += usage.size();
                if (usage.size() == 0)
                {
                    ClientCommandManager.sendFeedback(Text.literal("/" + cmdName));
                    cmdCount++;
                }
            }
        }
        return cmdCount;
    }

    private static int calc(CommandContext<ServerCommandSource> context)
    {
        String commandLine = getString(context, "calculation");
        StreamTokenizer tokenizer = makeTokenizer(commandLine);
        try
        {
            localOutputT("watson.message.calc.calculation", commandLine, calculation(tokenizer));
        }
        catch (Exception e)
        {
            localErrorT("watson.error.calc.calculation");
        }
        return 1;
    }

    private static StreamTokenizer makeTokenizer(String args)
    {
        // Currently not supporting variables, but define words for future use.
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(args));
        tokenizer.slashSlashComments(false);
        tokenizer.slashStarComments(false);
        tokenizer.wordChars('A', 'Z');
        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('_', '_');
        // These need to be defined as ordinary or they will be parsed as comment
        // introducers. >.<
        tokenizer.ordinaryChar('*');
        tokenizer.ordinaryChar('/');
        tokenizer.ordinaryChar('%');
        return tokenizer;
    }

    private static double calculation(StreamTokenizer tokenizer) throws IOException
    {
        double result = expr(tokenizer);
        if (tokenizer.nextToken() != StreamTokenizer.TT_EOF)
        {
            throw new IOException();
        }
        return result;
    }

    private static double expr(StreamTokenizer tokenizer) throws IOException
    {
        double result = term(tokenizer);
        for (; ; )
        {
            int token = tokenizer.nextToken();
            if (token == '+')
            {
                result += term(tokenizer);
            }
            else if (token == '-')
            {
                result -= term(tokenizer);
            }
            else
            {
                break;
            }
        } // for
        tokenizer.pushBack();
        return result;
    }

    private static double term(StreamTokenizer tokenizer) throws IOException
    {
        double result = factor(tokenizer);
        for (; ; )
        {
            int token = tokenizer.nextToken();
            if (token == '*')
            {
                result *= factor(tokenizer);
            }
            else if (token == '/')
            {
                result /= factor(tokenizer);
            }
            else if (token == '%')
            {
                result %= factor(tokenizer);
            }
            else
            {
                break;
            }
        } // for
        tokenizer.pushBack();
        return result;
    }

    private static double factor(StreamTokenizer tokenizer) throws IOException
    {
        int token = tokenizer.nextToken();
        if (token == StreamTokenizer.TT_NUMBER)
        {
            return tokenizer.nval;
        }
        else if (token == '(')
        {
            double result = expr(tokenizer);
            if (tokenizer.nextToken() != ')')
            {
                throw new IOException();
            }
            return result;
        }
        else if (token == '-')
        {
            return -factor(tokenizer);
        }
        else
        {
            throw new IOException();
        }
    }
}