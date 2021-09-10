package studio.baka.neko.essentials.mixinInterfaces;

import studio.baka.neko.essentials.utils.SavedLocation;

public interface IMixinServerPlayerEntity {
    void setHomeLocation(SavedLocation i);
    SavedLocation getHomeLocation();
}
