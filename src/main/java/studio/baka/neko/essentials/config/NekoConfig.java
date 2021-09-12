package studio.baka.neko.essentials.config;

import com.google.common.collect.Lists;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import studio.baka.neko.essentials.utils.SavedLocation;

import java.util.List;

@Config(name = "neko-essentials")
public class NekoConfig implements ConfigData {
    public List<String> warpPoints = Lists.newArrayList("zero:minecraft:overworld,0,0,0");

    @Override
    public void validatePostLoad() throws ValidationException {
        for (String point : warpPoints) {
            String[] pointp = point.split(":", 2);
            if (pointp.length != 2) throw new ValidationException("Warp point is invalid. (" + point + ")");
            try {
                SavedLocation.load(pointp[1]);
            } catch (RuntimeException load) {
                throw new ValidationException(load.getMessage());
            }
        }
    }
}
