package studio.baka.neko.essentials.config;

import studio.baka.neko.essentials.utils.SavedLocation;

import java.util.HashMap;

public class NekoConfigParsed {
    public static HashMap<String, SavedLocation> warpPoints;

    public static void load(NekoConfig config) {
        warpPoints = new HashMap<>();
        for (String point : config.warpPoints) {
            String[] pointp = point.split(":", 2);
            warpPoints.put(pointp[0], SavedLocation.load(pointp[1]));
        }
    }
}
