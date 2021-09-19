package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;
import studio.baka.neko.essentials.utils.SavedLocation;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class BackCommand {
    private static final SimpleCommandExceptionType NO_BACK_EXCEPTION =
            new SimpleCommandExceptionType(Text.of("没有返回点可以前往"));
    private static final DynamicCommandExceptionType INVALID_DIMENSION_EXCEPTION =
            new DynamicCommandExceptionType((id) -> Text.of("invalid back dimension: " + id));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("back")
                .executes((context) -> execute(context.getSource(), context.getSource().getPlayer())));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        SavedLocation loc = ((IMixinServerPlayerEntity) player).getLastLocation();
        if (loc == null) throw NO_BACK_EXCEPTION.create();

        RegistryKey<World> registryKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(loc.world));
        ServerWorld serverWorld = source.getServer().getWorld(registryKey);
        if (serverWorld == null) throw INVALID_DIMENSION_EXCEPTION.create(loc.world);

        logger.info(String.format("[back][teleport] %s -> %s", player, loc.asFullString()));
        ((IMixinServerPlayerEntity) player).setLastLocation(
                new SavedLocation(player.getServerWorld().getRegistryKey().getValue().toString(),
                        player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()));
        player.teleport(serverWorld, loc.x, loc.y, loc.z, loc.yaw, loc.pitch);
        source.sendFeedback(Text.of("已传送到上次传送的位置"), false);

        return 0;
    }
}
