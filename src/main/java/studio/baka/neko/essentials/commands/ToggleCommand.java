package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;
import studio.baka.neko.essentials.utils.SavedLocation;

import java.util.Map;
import java.util.HashMap;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class ToggleCommand {
    private static final Map<String, GameMode> playerToGameMode = new HashMap<>();

    private static final DynamicCommandExceptionType INVALID_DIMENSION_EXCEPTION =
            new DynamicCommandExceptionType((id) -> Text.of("invalid home dimension: " + id));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("toggle")
                .executes((context) -> execute(context.getSource(), context.getSource().getPlayer())));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        if (!player.isSpectator()) {
            SavedLocation loc = new SavedLocation(player.getWorld().getRegistryKey().getValue().toString(),
                    player.getX(), player.getY(), player.getZ(),
                    player.getYaw(), player.getPitch());
            logger.info(String.format("[toggle][set] %s -> %s", player, loc.asFullString()));
            ((IMixinServerPlayerEntity) player).setToggleLocation(loc);
            playerToGameMode.put(player.getUuidAsString(), player.interactionManager.getGameMode());
            player.changeGameMode(GameMode.SPECTATOR);
        } else {
            SavedLocation loc = ((IMixinServerPlayerEntity) player).getToggleLocation();
            if (loc != null) {
                RegistryKey<World> registryKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(loc.world));
                ServerWorld serverWorld = source.getServer().getWorld(registryKey);
                if (serverWorld == null) throw INVALID_DIMENSION_EXCEPTION.create(loc.world);

                logger.info(String.format("[toggle][teleport] %s -> %s", player, loc.asFullString()));
                player.teleport(serverWorld, loc.x, loc.y, loc.z, loc.yaw, loc.pitch);
            }
            if (playerToGameMode.containsKey(player.getUuidAsString()) && playerToGameMode.get(player.getUuidAsString()) != null) {
                player.changeGameMode(playerToGameMode.get(player.getUuidAsString()));
                playerToGameMode.remove(player.getUuidAsString());
            } else {
                player.changeGameMode(GameMode.DEFAULT);
            }
        }
        return 0;
    }
}
