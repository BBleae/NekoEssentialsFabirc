package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class DenyruleCommand {
    private static final SimpleCommandExceptionType ACCEPTED_EXCEPTION =
            new SimpleCommandExceptionType(Text.of("你已经同意遵守了服务器规定"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("denyrule")
                .executes((context) -> execute(context.getSource(), context.getSource().getPlayer())));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        logger.info(String.format("[rule][deny] %s", player));
        if (((IMixinServerPlayerEntity) player).getAcceptedRules())
            throw ACCEPTED_EXCEPTION.create();
        player.networkHandler.disconnect(Text.of("§e[NekoCraft] §c你拒绝遵守服务器规定"));
        return 0;
    }
}
