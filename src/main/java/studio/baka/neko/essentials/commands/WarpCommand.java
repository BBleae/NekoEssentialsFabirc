package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import studio.baka.neko.essentials.config.NekoConfigParsed;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;
import studio.baka.neko.essentials.utils.SavedLocation;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class WarpCommand {
    public static final DynamicCommandExceptionType INVALID_WARP_POINT_EXCEPTION =
            new DynamicCommandExceptionType((name) -> Text.of("路径点 " + name + " 不存在"));
    private static final DynamicCommandExceptionType INVALID_DIMENSION_EXCEPTION =
            new DynamicCommandExceptionType((id) -> Text.of("invalid warp dimension: " + id));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("warp")
                .then(CommandManager.argument("target", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(
                                Stream.concat(NekoConfigParsed.warpPoints.keySet().stream(), Stream.of("spawn"))
                                        .collect(Collectors.toSet()), builder))
                        .executes((context) -> execute(context.getSource(), context.getSource().getPlayer(),
                                StringArgumentType.getString(context, "target")))));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player, String name) throws CommandSyntaxException {
        if (Objects.equals(name, "spawn")) {
            ServerWorld overworld = source.getServer().getOverworld();
            BlockPos pos = overworld.getSpawnPos();

            logger.info(String.format("[warp] %s -> %s (%s)", player, name, pos));
            ((IMixinServerPlayerEntity) player).setLastLocation(
                    new SavedLocation(player.getServerWorld().getRegistryKey().getValue().toString(),
                            player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()));
            player.teleport(overworld, pos.getX(), pos.getY(), pos.getZ(), 0, 0);

            return 0;
        }

        SavedLocation loc = NekoConfigParsed.warpPoints.get(name);
        if (loc == null) throw INVALID_WARP_POINT_EXCEPTION.create(name);

        RegistryKey<World> registryKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(loc.world));
        ServerWorld serverWorld = source.getServer().getWorld(registryKey);
        if (serverWorld == null) throw INVALID_DIMENSION_EXCEPTION.create(loc.world);

        logger.info(String.format("[warp] %s -> %s (%s)", player, name, loc.asFullString()));
        ((IMixinServerPlayerEntity) player).setLastLocation(
                new SavedLocation(player.getServerWorld().getRegistryKey().getValue().toString(),
                        player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()));
        player.teleport(serverWorld, loc.x, loc.y, loc.z, loc.yaw, loc.pitch);
        source.sendFeedback(Text.of("已传送到路径点 " + name), false);

        return 0;
    }
}
