package studio.baka.neko.essentials.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;
import studio.baka.neko.essentials.utils.SavedLocation;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity implements IMixinServerPlayerEntity {
    @Nullable
    private SavedLocation homeLocation;

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void afterReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("homeLocation", 10)) {
            NbtCompound nbtCompound = nbt.getCompound("homeLocation");
            homeLocation = new SavedLocation(nbtCompound.getString("world"),
                    nbtCompound.getInt("x"), nbtCompound.getInt("y"), nbtCompound.getInt("z"),
                    nbtCompound.getFloat("yaw"), nbtCompound.getFloat("pitch"));
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    public void afterWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (homeLocation != null) {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putString("world", homeLocation.world);
            nbtCompound.putInt("x", homeLocation.x);
            nbtCompound.putInt("y", homeLocation.y);
            nbtCompound.putInt("z", homeLocation.z);
            nbtCompound.putFloat("yaw", homeLocation.yaw);
            nbtCompound.putFloat("pitch", homeLocation.pitch);
            nbt.put("homeLocation", nbtCompound);
        }
    }

    @Override
    public void setHomeLocation(@Nullable SavedLocation i) {
        homeLocation = i;
    }

    @Override
    public @Nullable SavedLocation getHomeLocation() {
        return homeLocation;
    }
}
