package studio.baka.neko.essentials.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;
import studio.baka.neko.essentials.utils.SavedLocation;
import studio.baka.neko.essentials.utils.TpaRequest;

import java.util.HashMap;
import java.util.UUID;

import static studio.baka.neko.essentials.NekoEssentials.logger;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements IMixinServerPlayerEntity {
    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow
    public abstract void sendSystemMessage(Text message, UUID sender);

    @Shadow
    public abstract ServerWorld getServerWorld();

    @Nullable
    private SavedLocation homeLocation;
    private final HashMap<UUID, TpaRequest> tpaReqs = new HashMap<>();
    private final HashMap<UUID, TpaRequest> tpaReqds = new HashMap<>();

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void afterReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("homeLocation", 10)) {
            NbtCompound nbtCompound = nbt.getCompound("homeLocation");
            homeLocation = new SavedLocation(nbtCompound.getString("world"),
                    nbtCompound.getDouble("x"), nbtCompound.getDouble("y"), nbtCompound.getDouble("z"),
                    nbtCompound.getFloat("yaw"), nbtCompound.getFloat("pitch"));
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    public void afterWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (homeLocation != null) {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putString("world", homeLocation.world);
            nbtCompound.putDouble("x", homeLocation.x);
            nbtCompound.putDouble("y", homeLocation.y);
            nbtCompound.putDouble("z", homeLocation.z);
            nbtCompound.putFloat("yaw", homeLocation.yaw);
            nbtCompound.putFloat("pitch", homeLocation.pitch);
            nbt.put("homeLocation", nbtCompound);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void afterTick(CallbackInfo ci) {
        long now = Util.getMeasuringTimeMs();
        for (TpaRequest n : tpaReqds.values()) {
            if (n.finished) {
                tpaReqds.remove(n.to);
            } else if (n.reqTime + 30 * 1000L < now) {
                n.setFinished();
                tpaReqds.remove(n.to);
                ServerPlayerEntity to = this.getServerWorld().getServer().getPlayerManager().getPlayer(n.to);
                if (to != null) {
                    logger.debug(String.format("[tpa][timeout] %s -> %s", this.getName().asString(), to.getName().asString()));
                    this.sendSystemMessage(Text.of("发向 " + to.getName().asString() + " 的 TPA 请求已超时"), Util.NIL_UUID);
                    to.sendSystemMessage(Text.of("来自 " + to.getName().asString() + " 的 TPA 请求已超时"), Util.NIL_UUID);
                }
            }
        }
        for (TpaRequest n : tpaReqs.values()) {
            if (n.finished) {
                tpaReqs.remove(n.from);
            }
        }
    }

    @Override
    public @Nullable SavedLocation getHomeLocation() {
        return homeLocation;
    }

    @Override
    public void setHomeLocation(@Nullable SavedLocation i) {
        homeLocation = i;
    }

    public void requestedTpa(TpaRequest req) {
        tpaReqds.put(req.to, req);
    }

    public void requestTpa(TpaRequest req) {
        tpaReqs.put(req.from, req);
    }

    public HashMap<UUID, TpaRequest> getTpaReqs() {
        return tpaReqs;
    }

    public HashMap<UUID, TpaRequest> getTpaReqds() {
        return tpaReqds;
    }
}
