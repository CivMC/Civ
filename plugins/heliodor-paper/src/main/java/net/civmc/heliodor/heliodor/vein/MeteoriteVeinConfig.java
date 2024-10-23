package net.civmc.heliodor.heliodor.vein;

import org.bukkit.Location;
import java.util.List;

public record MeteoriteVeinConfig(VeinConfig config, List<Location> positions, int minPositionRadius, int maxPositionRadius, int maxBury) {
    public static final String TYPE_NAME = "meteorite";
}
