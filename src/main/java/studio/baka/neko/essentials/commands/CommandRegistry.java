package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public class CommandRegistry {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        SethomeCommand.register(dispatcher);
        HomeCommand.register(dispatcher);
        TpaCommand.register(dispatcher);
        TpacceptCommand.register(dispatcher);
        TpadenyCommand.register(dispatcher);
    }
}
