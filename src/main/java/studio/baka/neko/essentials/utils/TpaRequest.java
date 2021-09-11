package studio.baka.neko.essentials.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;

import java.util.UUID;

public class TpaRequest {
    private final MinecraftServer server;
    public final UUID from;
    public final UUID to;
    public long reqTime;
    public boolean finished = false;

    public TpaRequest(MinecraftServer server, UUID from, UUID to) {
        this(server, from, to, Util.getMeasuringTimeMs());
    }

    public TpaRequest(MinecraftServer server, UUID from, UUID to, long reqTime) {
        this.server = server;
        this.from = from;
        this.to = to;
        this.reqTime = reqTime;
    }

    public void setFinished() {
        finished = true;
    }

    public void refresh() {
        this.reqTime = Util.getMeasuringTimeMs();
    }

    public void execute() {
        if (finished) return;
        ServerPlayerEntity from = server.getPlayerManager().getPlayer(this.from);
        ServerPlayerEntity to = server.getPlayerManager().getPlayer(this.to);
        if (from == null || to == null) {
            this.setFinished();
            return;
        }
        from.teleport(to.getServerWorld(), to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
        this.setFinished();
    }
}
