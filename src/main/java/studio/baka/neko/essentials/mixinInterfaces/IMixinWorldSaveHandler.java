package studio.baka.neko.essentials.mixinInterfaces;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public interface IMixinWorldSaveHandler {
    @Nullable
    NbtCompound loadPlayerData(GameProfile profile);

    void savePlayerData(GameProfile profile, NbtCompound nbtCompound);
}
