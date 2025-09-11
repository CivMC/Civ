package net.civmc.heliodor.vein.data;

import java.util.List;

public record MeteoricIronVeinConfig(VeinConfig config, List<VerticalBlockPos> positions, int minPositionRadius, int maxPositionRadius, int maxBury, boolean overrideEnderEyes) {
    public static final String TYPE_NAME = "meteorite";
}
