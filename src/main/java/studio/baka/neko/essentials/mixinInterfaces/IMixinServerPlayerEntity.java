package studio.baka.neko.essentials.mixinInterfaces;

import org.jetbrains.annotations.Nullable;
import studio.baka.neko.essentials.utils.SavedLocation;
import studio.baka.neko.essentials.utils.TpaRequest;

import java.util.HashMap;
import java.util.UUID;

public interface IMixinServerPlayerEntity {
    @Nullable SavedLocation getHomeLocation();

    void setHomeLocation(@Nullable SavedLocation i);

    void requestedTpa(TpaRequest req);

    void requestTpa(TpaRequest req);

    HashMap<UUID, TpaRequest> getTpaReqs();

    HashMap<UUID, TpaRequest> getTpaReqds();
}
