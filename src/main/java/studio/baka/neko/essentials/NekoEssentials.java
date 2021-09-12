package studio.baka.neko.essentials;

import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.Unpooled;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
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
        ServerTickEvents.END_SERVER_TICK.register(this::onEndTick);

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

    private void onEndTick(MinecraftServer server) {
        if (server.getTicks() % 64 == 0) {
            String message = "§bNekoCraft§r";
            switch (server.getTicks() / 64 % 2) {
                case 0 -> {
                    ServerWorld overworld = server.getWorld(World.OVERWORLD);
                    if (overworld != null)
                        message = "§b正在经历第" + overworld.getLevelProperties().getTime() + "个tick的NekoCraft§r";
                }
                case 1 -> message = "§b正在与" + server.getPlayerManager().getCurrentPlayerCount() + "只猫猫玩耍的NekoCraft§r";
            }
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.networkHandler.sendPacket(new CustomPayloadS2CPacket(CustomPayloadS2CPacket.BRAND,
                        new PacketByteBuf(Unpooled.buffer()).writeString(message)));
            }
        }
    }
}
