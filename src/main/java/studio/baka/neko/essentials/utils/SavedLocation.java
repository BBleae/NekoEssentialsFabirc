package studio.baka.neko.essentials.utils;

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

    public String asString() {
        return String.format("[%s, %.2f, %.2f, %.2f]", world, x, y, z);
    }

    public String asFullString() {
        return String.format("[%s, %f, %f, %f, %f, %f]", world, x, y, z, yaw, pitch);
    }

}
