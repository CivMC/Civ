package net.civmc.heliodor.vein;

import java.util.List;

public record MeteoricIronVeinConfig(VeinConfig config, List<VerticalBlockPos> positions, int minPositionRadius, int maxPositionRadius, int maxBury) {
    public static final String TYPE_NAME = "meteorite";
}
