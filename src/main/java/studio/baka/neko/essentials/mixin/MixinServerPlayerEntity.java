package studio.baka.neko.essentials.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
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
    @Nullable
    private SavedLocation lastLocation;
    private boolean acceptedRules = false;
    private final HashMap<UUID, TpaRequest> tpaReqs = new HashMap<>();
    private final HashMap<UUID, TpaRequest> tpaReqds = new HashMap<>();

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void afterReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("homeLocation", 10))
            homeLocation = SavedLocation.formNBT(nbt.getCompound("homeLocation"));
        if (nbt.contains("lastLocation", 10))
            lastLocation = SavedLocation.formNBT(nbt.getCompound("lastLocation"));
        if (nbt.contains("acceptedRules", 1))
            acceptedRules = nbt.getBoolean("acceptedRules");
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    public void afterWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (homeLocation != null)
            nbt.put("homeLocation", homeLocation.asNBT());
        if (lastLocation != null)
            nbt.put("lastLocation", lastLocation.asNBT());
        if (acceptedRules)
            nbt.putBoolean("acceptedRules", true);
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
                    logger.info(String.format("[tpa][timeout] %s -> %s", this, to));
                    this.sendSystemMessage(new LiteralText("发向 ").append(to.getDisplayName()).append("的传送请求已超时"), Util.NIL_UUID);
                    to.sendSystemMessage(new LiteralText("来自 ").append(this.getDisplayName()).append("的传送请求已超时"), Util.NIL_UUID);
                }
            }
        }
        for (TpaRequest n : tpaReqs.values()) {
            if (n.finished) {
                tpaReqs.remove(n.from);
            }
        }
    }

    @Inject(method = "onDeath", at = @At("RETURN"))
    public void afterDeath(DamageSource source, CallbackInfo ci) {
        lastLocation = new SavedLocation(this.getServerWorld().getRegistryKey().getValue().toString(),
                this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
    }

    @Override
    public @Nullable SavedLocation getHomeLocation() {
        return homeLocation;
    }

    @Override
    public void setHomeLocation(@Nullable SavedLocation i) {
        homeLocation = i;
    }

    @Override
    public @Nullable SavedLocation getLastLocation() {
        return lastLocation;
    }

    @Override
    public void setLastLocation(@Nullable SavedLocation i) {
        lastLocation = i;
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

    public boolean getAcceptedRules() {
        return acceptedRules;
    }

    public void setAcceptedRules(boolean i) {
        acceptedRules = i;
    }
}
