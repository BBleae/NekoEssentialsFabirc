package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;
import studio.baka.neko.essentials.utils.TpaRequest;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class TpaCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tpa")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes((context) -> execute(context.getSource(), context.getSource().getPlayer(),
                                EntityArgumentType.getPlayer(context, "target")))));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player, ServerPlayerEntity target) {
        TpaRequest req = new TpaRequest(player.getServer(), player.getUuid(), target.getUuid());
        ((IMixinServerPlayerEntity) player).requestedTpa(req);
        ((IMixinServerPlayerEntity) target).requestTpa(req);
        logger.info(String.format("[tpa][send] %s -> %s", player, target));

        target.sendSystemMessage(new LiteralText("").append(player.getDisplayName()).append(" 想要传送到你的位置  ")
                .append(new LiteralText("[接受]").styled(style -> style.withColor(Formatting.AQUA)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/tpaccept " + player.getName().asString())))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + player.getName().asString()))))
                .append("  ")
                .append(new LiteralText("[拒绝]").styled(style -> style.withColor(Formatting.DARK_AQUA)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/tpadeny " + player.getName().asString())))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpadeny " + player.getName().asString())))), Util.NIL_UUID);
        source.sendFeedback(new LiteralText("已成功向 ").append(target.getDisplayName()).append(" 发送传送请求"), false);
        return 0;
    }
}
