package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
        logger.debug(String.format("[tpa][send] %s -> %s", player.getName().asString(), target.getName().asString()));
        source.sendFeedback(Text.of("已成功向 " + target.getName().asString() + " 发送传送请求"), false);
        return 0;
    }
}
