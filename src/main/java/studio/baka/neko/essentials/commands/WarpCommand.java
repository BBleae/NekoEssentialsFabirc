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
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import studio.baka.neko.essentials.config.NekoConfigParsed;
import studio.baka.neko.essentials.utils.SavedLocation;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class WarpCommand {
    public static final DynamicCommandExceptionType INVALID_WARP_POINT_EXCEPTION =
            new DynamicCommandExceptionType((name) -> Text.of("路径点 " + name + " 不存在"));
    private static final DynamicCommandExceptionType INVALID_DIMENSION_EXCEPTION =
            new DynamicCommandExceptionType((id) -> Text.of("invalid warp dimension: " + id));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("warp")
                .then(CommandManager.argument("target", StringArgumentType.word())
                        .suggests(((context, builder) ->
                                CommandSource.suggestMatching(NekoConfigParsed.warpPoints.keySet(), builder)))
                        .executes((context) -> execute(context.getSource(), context.getSource().getPlayer(),
                                StringArgumentType.getString(context, "target")))));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player, String name) throws CommandSyntaxException {
        SavedLocation loc = NekoConfigParsed.warpPoints.get(name);
        if (loc == null) throw INVALID_WARP_POINT_EXCEPTION.create(name);

        RegistryKey<World> registryKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(loc.world));
        ServerWorld serverWorld = source.getServer().getWorld(registryKey);
        if (serverWorld == null) throw INVALID_DIMENSION_EXCEPTION.create(loc.world);

        logger.info(String.format("[warp] %s -> %s (%s)", player, name, loc.asFullString()));
        player.teleport(serverWorld, loc.x, loc.y, loc.z, loc.yaw, loc.pitch);
        source.sendFeedback(Text.of("已传送到路径点 " + name), false);

        return 0;
    }
}
