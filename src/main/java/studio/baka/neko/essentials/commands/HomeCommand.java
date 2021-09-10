package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;
import studio.baka.neko.essentials.utils.SavedLocation;

public class HomeCommand {
    private static final SimpleCommandExceptionType NO_HOME_EXCEPTION = new SimpleCommandExceptionType(Text.of("no home"));
    private static final DynamicCommandExceptionType INVALID_DIMENSION_EXCEPTION = new DynamicCommandExceptionType((id) -> {
        return Text.of("invalid home dimension: " + id);
    });

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("home").executes((context) -> {
            return execute(context.getSource(), context.getSource().getPlayer());
        }));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        SavedLocation loc = ((IMixinServerPlayerEntity) player).getHomeLocation();
        if (loc == null) throw NO_HOME_EXCEPTION.create();
        RegistryKey<World> registryKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(loc.world));
        ServerWorld serverWorld = source.getServer().getWorld(registryKey);
        if (serverWorld == null) {
            throw INVALID_DIMENSION_EXCEPTION.create(loc.world);
        }
        player.teleport(serverWorld, loc.x, loc.y, loc.z, loc.yaw, loc.pitch);
        source.sendFeedback(Text.of("hi!"), true);
        return 0;
    }
}
