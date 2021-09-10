package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;
import studio.baka.neko.essentials.utils.SavedLocation;

public class SethomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sethome").executes((context) -> {
            return execute(context.getSource(), context.getSource().getPlayer());
        }));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) {
        String world = player.world.getRegistryKey().getValue().toString();
        SavedLocation loc = new SavedLocation(world, player.getBlockX(), player.getBlockY(), player.getBlockZ(), player.getYaw(), player.getPitch());
        ((IMixinServerPlayerEntity) player).setHomeLocation(loc);
        source.sendFeedback(Text.of("hi!"), true);
        return 0;
    }
}
