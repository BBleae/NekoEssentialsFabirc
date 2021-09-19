package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public class AtCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("at")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                .executes((context) -> execute(context.getSource(), context.getSource().getPlayer(),
                                        EntityArgumentType.getPlayer(context, "target"),
                                        StringArgumentType.getString(context, "message"))))));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player, ServerPlayerEntity target, String message) {
        if (player.getClientChatVisibility() == ChatVisibility.HIDDEN) {
            player.networkHandler.sendPacket(new GameMessageS2CPacket((new TranslatableText("chat.disabled.options")).formatted(Formatting.RED), MessageType.SYSTEM, Util.NIL_UUID));
        } else {
            player.updateLastActionTime();
            Text text = new TranslatableText("chat.type.text", player.getDisplayName(),
                    new LiteralText("")
                            .append(new LiteralText("@").append(target.getDisplayName())
                                    .styled(style -> style.withColor(Formatting.GREEN)))
                            .append(" ").append(message));
            source.getServer().getPlayerManager().broadcastChatMessage(text, MessageType.CHAT, player.getUuid());
            target.sendSystemMessage(new LiteralText("")
                            .append(new LiteralText("有一位叫 ").styled(style -> style.withColor(Formatting.GREEN)))
                            .append(player.getDisplayName())
                            .append(new LiteralText(" 的小朋友@了你").styled(style -> style.withColor(Formatting.GREEN))),
                    Util.NIL_UUID);
        }
        return 0;
    }
}
