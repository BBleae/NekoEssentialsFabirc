package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;
import studio.baka.neko.essentials.utils.SavedLocation;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class SpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("spawn")
                .executes((context) -> execute(context.getSource(), context.getSource().getPlayer())));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) {
        ServerWorld serverWorld = source.getServer().getOverworld();
        BlockPos loc = serverWorld.getSpawnPos();

        logger.info(String.format("[spawn][teleport] %s -> %s", player, loc.toString()));
        ((IMixinServerPlayerEntity) player).setLastLocation(
                new SavedLocation(player.getServerWorld().getRegistryKey().getValue().toString(),
                        player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()));
        player.teleport(serverWorld, loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        source.sendFeedback(Text.of("已传送到出生点"), false);

        return 0;
    }
}
