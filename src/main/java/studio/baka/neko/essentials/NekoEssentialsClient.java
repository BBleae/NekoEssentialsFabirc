package studio.baka.neko.essentials;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import studio.baka.neko.essentials.config.NekoConfig;

public class NekoEssentialsClient implements ClientModInitializer {
    public static final Logger logger = LogManager.getLogger("NekoEssentials");
    public static NekoConfig config;

    @Override
    public void onInitializeClient() {
        logger.trace("onInitializeClient");

        logger.debug("registering configs");
        AutoConfig.register(NekoConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(NekoConfig.class).getConfig();
    }
}
