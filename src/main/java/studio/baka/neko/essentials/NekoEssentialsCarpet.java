package studio.baka.neko.essentials;

import carpet.CarpetExtension;
import carpet.logging.HUDController;
import net.minecraft.text.LiteralText;

public class NekoEssentialsCarpet implements CarpetExtension {
    @Override
    public void registerLoggers() {
        HUDController.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                HUDController.scarpet_headers.put(player.getEntityName(),
                        new LiteralText("§b§m          §r §a[§eNekoCraft§a] §b§m          \n§aTelegram 群组: §7t.me/NekoCraft\n§aQQ 群: §77923309§r"));
                HUDController.addMessage(player, new LiteralText("§b§m                                      §r"));
            });
        });
    }
}
