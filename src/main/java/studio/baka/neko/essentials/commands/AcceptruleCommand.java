package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class AcceptruleCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("acceptrule")
                .executes((context) -> execute(context.getSource(), context.getSource().getPlayer())));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) {
        logger.info(String.format("[rule][accept] %s", player));
        ((IMixinServerPlayerEntity) player).setAcceptedRules(true);
        source.sendFeedback(new LiteralText("感谢您接受了服务器的规定, 同时也希望您能一直遵守规定!")
                .styled(style -> style.withColor(Formatting.GREEN)), false);
        player.getInventory().offerOrDrop(new ItemStack(Registry.ITEM.get(new Identifier("minecraft:cooked_beef")), 64));
        source.getServer().getPlayerManager().broadcast(new LiteralText("")
                        .append(new LiteralText("欢迎新玩家 ").styled(style -> style.withColor(Formatting.AQUA)))
                        .append(player.getDisplayName())
                        .append(new LiteralText(" 加入服务器!").styled(style -> style.withColor(Formatting.AQUA))),
                MessageType.SYSTEM, Util.NIL_UUID);
        return 0;
    }
}
