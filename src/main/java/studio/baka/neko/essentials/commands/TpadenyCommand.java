package studio.baka.neko.essentials.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;
import studio.baka.neko.essentials.utils.TpaRequest;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class TpadenyCommand {
    private static final SimpleCommandExceptionType NO_TPA_EXCEPTION =
            new SimpleCommandExceptionType(Text.of("你还没有收到过任何传送请求"));
    private static final DynamicCommandExceptionType NO_TPA_FROM_EXCEPTION =
            new DynamicCommandExceptionType((playerName) -> Text.of("你还没有收到过来自 " + playerName + " 的任何传送请求"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tpadeny")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes((context) -> execute(context.getSource(), context.getSource().getPlayer(),
                                EntityArgumentType.getPlayer(context, "target"))))
                .executes((context) -> execute(context.getSource(), context.getSource().getPlayer())));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        HashMap<UUID, TpaRequest> reqs = ((IMixinServerPlayerEntity) player).getTpaReqs();
        if (reqs.size() == 0) {
            throw NO_TPA_EXCEPTION.create();
        }
        if (reqs.size() > 1) {
            LiteralText msg = new LiteralText("请从下列待接收请求中选择一个想要拒绝的请求: ");
            PlayerManager playerManager = source.getServer().getPlayerManager();

            List<Text> accepts = Lists.newArrayList();
            for (TpaRequest req : reqs.values()) {
                ServerPlayerEntity from = playerManager.getPlayer(req.from);
                if (from == null) {
                    reqs.remove(req.from);
                } else {
                    accepts.add(Texts.bracketed(new LiteralText("").append(from.getDisplayName()).styled(style -> style
                            .withColor(Formatting.YELLOW)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/tpadeny " + from.getName().asString())))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpadeny " + from.getName().asString())))));
                }
            }
            msg.append(Texts.join(accepts, Text.of(", ")));
            source.sendFeedback(msg, false);
        }
        if (reqs.size() == 1) {
            TpaRequest req = reqs.values().iterator().next();
            ServerPlayerEntity target = source.getServer().getPlayerManager().getPlayer(req.from);
            if (target == null) {
                reqs.remove(req.from);
                throw NO_TPA_EXCEPTION.create();
            }
            return execute(source, player, target);
        }
        return 0;
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player, ServerPlayerEntity target) throws CommandSyntaxException {
        HashMap<UUID, TpaRequest> reqs = ((IMixinServerPlayerEntity) player).getTpaReqs();
        TpaRequest req = reqs.get(target.getUuid());
        if (req == null) {
            throw NO_TPA_FROM_EXCEPTION.create(target.getName().asString());
        }
        logger.info(String.format("[tpa][deny] %s -> %s", target, player));
        req.setFinished();
        reqs.remove(req.from);
        target.sendSystemMessage(new LiteralText("发送到 ").append(player.getDisplayName()).append(" 的传送请求被拒绝")
                .styled(style -> style.withColor(Formatting.RED)), Util.NIL_UUID);
        source.sendFeedback(new LiteralText("已拒绝来自 ").append(target.getDisplayName()).append(" 的传送请求"), false);
        return 0;
    }
}
