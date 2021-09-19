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
                        new LiteralText("§3§m            §r §a[§6NekoCraft§a] §3§m            \n§7Telegram:§3@NekoCraft  §7QQ:§37923309§r"));
                if (!HUDController.player_huds.containsKey(player))
                    HUDController.addMessage(player, new LiteralText("§7使用 /log 监听的信息会显示在这里§r"));
                HUDController.addMessage(player, new LiteralText("§3§m                                    §r"));
            });
        });
    }
}
