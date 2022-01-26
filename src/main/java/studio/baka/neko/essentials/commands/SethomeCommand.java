package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;
import studio.baka.neko.essentials.utils.SavedLocation;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class SethomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sethome")
                .executes((context) -> execute(context.getSource(), context.getSource().getPlayer())));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) {
        String world = player.getWorld().getRegistryKey().getValue().toString();
        SavedLocation loc = new SavedLocation(world,
                player.getX(), player.getY(), player.getZ(),
                player.getYaw(), player.getPitch());
        logger.info(String.format("[home][set] %s -> %s", player, loc.asFullString()));
        ((IMixinServerPlayerEntity) player).setHomeLocation(loc);
        source.sendFeedback(Text.of("已成功在 " + loc.asString() + " 处设置家"), false);
        return 0;
    }
}
