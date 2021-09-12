package studio.baka.neko.essentials.utils;

import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class SavedLocation {
    public final String world;
    public final double x;
    public final double y;
    public final double z;
    public final float yaw;
    public final float pitch;

    public SavedLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static SavedLocation load(String str) {
        String[] strs = str.split(",");
        if (strs.length == 4) {
            return new SavedLocation(strs[0],
                    Double.parseDouble(strs[1]), Double.parseDouble(strs[2]), Double.parseDouble(strs[3]),
                    0, 0);
        } else if (strs.length == 6) {
            return new SavedLocation(strs[0],
                    Double.parseDouble(strs[1]), Double.parseDouble(strs[2]), Double.parseDouble(strs[3]),
                    Float.parseFloat(strs[4]), Float.parseFloat(strs[5]));
        } else {
            throw new RuntimeException("Location string is invalid. (" + str + ")");
        }
    }

    public String save() {
        return String.format("%s,%f,%f,%f,%f,%f", world, x, y, z, yaw, pitch);
    }

    public String asString() {
        return String.format("[%s, %.2f, %.2f, %.2f]", world, x, y, z);
    }

    public String asFullString() {
        return String.format("[%s, %f, %f, %f, %f, %f]", world, x, y, z, yaw, pitch);
    }

}
