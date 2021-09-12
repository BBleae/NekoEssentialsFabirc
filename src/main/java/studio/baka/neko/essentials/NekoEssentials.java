package studio.baka.neko.essentials;

import com.mojang.brigadier.CommandDispatcher;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import studio.baka.neko.essentials.commands.CommandRegistry;
import studio.baka.neko.essentials.config.NekoConfig;
import studio.baka.neko.essentials.config.NekoConfigParsed;

public class NekoEssentials implements DedicatedServerModInitializer {
    public static final Logger logger = LogManager.getLogger("NekoEssentials");
    public static NekoConfig rawConfig;
    public LuckPerms luckPermsApi;

    @Override
    public void onInitializeServer() {
        logger.trace("onInitializeServer");

        logger.debug("registering configs");
        AutoConfig.register(NekoConfig.class, GsonConfigSerializer::new);
        rawConfig = AutoConfig.getConfigHolder(NekoConfig.class).getConfig();
        NekoConfigParsed.load(rawConfig);

        logger.debug("registering event listeners");
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
        CommandRegistrationCallback.EVENT.register(this::onCommandRegistering);

        logger.debug("NekoEssentials initialized");
    }

    private void onServerStarting(MinecraftServer server) {
        logger.trace("onServerStarting");

        logger.debug("get LuckPerms api");
        luckPermsApi = LuckPermsProvider.get();
    }

    private void onServerStopping(MinecraftServer server) {
        logger.trace("onServerStopping");
    }

    private void onCommandRegistering(CommandDispatcher<ServerCommandSource> dispatcher, boolean isDedicated) {
        logger.trace("onCommandRegistering");

        CommandRegistry.register(dispatcher);
    }
}
