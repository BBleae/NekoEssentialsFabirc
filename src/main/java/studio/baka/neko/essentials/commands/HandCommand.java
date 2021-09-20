package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class HandCommand {
    private static final SimpleCommandExceptionType NO_ITEM_EXCEPTION =
            new SimpleCommandExceptionType(Text.of("请将要展示的物品放在主手"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("hand")
                .executes((context) -> execute(context.getSource(), context.getSource().getPlayer())));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        ItemStack itemStack = player.getMainHandStack();
        if (itemStack.isEmpty())
            throw NO_ITEM_EXCEPTION.create();

        logger.info(String.format("[hand] %s with %s", player, itemStack));
        MutableText text = new LiteralText("")
                .append(player.getDisplayName())
                .append(new LiteralText(" 向你展示了 " + itemStack.getCount() + " 个物品: ")
                        .styled(style -> style.withColor(Formatting.GRAY)))
                .append(itemStack.toHoverableText());
        source.getServer().getPlayerManager().broadcastChatMessage(text, MessageType.CHAT, player.getUuid());
        return 0;
    }
}
