package net.civmc.heliodor.vein.data;

import net.civmc.heliodor.vein.data.VeinConfig;
import net.civmc.heliodor.vein.data.VerticalBlockPos;
import java.util.List;

public record MeteoricIronVeinConfig(VeinConfig config, List<VerticalBlockPos> positions, int minPositionRadius, int maxPositionRadius, int maxBury) {
    public static final String TYPE_NAME = "meteorite";
}
