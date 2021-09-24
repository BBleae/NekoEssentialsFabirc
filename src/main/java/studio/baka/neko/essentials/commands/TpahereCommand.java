package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;
import studio.baka.neko.essentials.utils.TpaRequest;

import java.util.Collection;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class TpahereCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tpahere")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes((context) -> execute(context.getSource(), context.getSource().getPlayer(),
                                EntityArgumentType.getPlayer(context, "target"))))
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .executes((context) -> execute(context.getSource(), context.getSource().getPlayer(),
                                EntityArgumentType.getPlayers(context, "targets")))));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player, Collection<ServerPlayerEntity> targets) {

        for (ServerPlayerEntity target : targets) {
            execute(source, player, target, true);
        }

        source.sendFeedback(new LiteralText("已成功向 ")
                .append(Texts.join(targets.stream().map(PlayerEntity::getDisplayName).toList(), Text.of(", ")))
                .append(" 发送传送请求"), false);

        return 0;
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player, ServerPlayerEntity target) {
        return execute(source, player, target, false);
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player, ServerPlayerEntity target, boolean skipLog) {
        TpaRequest req = new TpaRequest(player.getServer(), player.getUuid(), target.getUuid(), true);
        ((IMixinServerPlayerEntity) player).requestedTpa(req);
        ((IMixinServerPlayerEntity) target).requestTpa(req);
        logger.info(String.format("[tpahere][send] %s -> %s", player, target));

        target.sendSystemMessage(new LiteralText("").append(player.getDisplayName()).append(" 想要你传送到他的位置  ")
                .append(new LiteralText("[接受]").styled(style -> style.withColor(Formatting.AQUA)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/tpaccept " + player.getName().asString())))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + player.getName().asString()))))
                .append("  ")
                .append(new LiteralText("[拒绝]").styled(style -> style.withColor(Formatting.DARK_AQUA)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/tpadeny " + player.getName().asString())))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpadeny " + player.getName().asString())))), Util.NIL_UUID);
        if (!skipLog)
            source.sendFeedback(new LiteralText("已成功向 ")
                    .append(target.getDisplayName())
                    .append(" 发送传送请求"), false);

        return 0;
    }
}
