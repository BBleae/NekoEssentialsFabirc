package studio.baka.neko.essentials.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;

import java.util.UUID;

public class TpaRequest {
    public final UUID from;
    public final UUID to;
    private final MinecraftServer server;
    public long reqTime;
    public boolean finished = false;
    public boolean here;

    public TpaRequest(MinecraftServer server, UUID from, UUID to) {
        this(server, from, to, Util.getMeasuringTimeMs(), false);
    }

    public TpaRequest(MinecraftServer server, UUID from, UUID to, boolean here) {
        this(server, from, to, Util.getMeasuringTimeMs(), here);
    }

    public TpaRequest(MinecraftServer server, UUID from, UUID to, long reqTime, boolean here) {
        this.server = server;
        this.from = from;
        this.to = to;
        this.reqTime = reqTime;
        this.here = here;
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
        if (!this.here) {
            ((IMixinServerPlayerEntity) from).setLastLocation(
                    new SavedLocation(from.getWorld().getRegistryKey().getValue().toString(),
                            from.getX(), from.getY(), from.getZ(), from.getYaw(), from.getPitch()));
            from.teleport(to.getWorld(), to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
        } else {
            ((IMixinServerPlayerEntity) to).setLastLocation(
                    new SavedLocation(to.getWorld().getRegistryKey().getValue().toString(),
                            to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch()));
            to.teleport(from.getWorld(), from.getX(), from.getY(), from.getZ(), from.getYaw(), from.getPitch());
        }
        this.setFinished();
    }
}
