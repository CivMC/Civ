package sh.okx.railswitch.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import sh.okx.railswitch.RailSwitchPlugin;

/**
 * Handles loading configuration options for the rail switch plugin.
 */
public final class SwitchPluginConfiguration {

    private static final String TOOL_PATH = "configuration-tool";
    private static final String DISPLAY_RANGE_PATH = "display-range";
    private static final String MAX_DESTINATIONS_PATH = "max-destinations-per-switch";

    private final RailSwitchPlugin plugin;
    private Material toolMaterial;
    private double displayRange;
    private int maxDestinationsPerSwitch;

    public SwitchPluginConfiguration(RailSwitchPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        String materialName = config.getString(TOOL_PATH, "STICK");
        Material material = materialName == null ? null : Material.matchMaterial(materialName, true);
        if (material == null) {
            material = Material.STICK;
            plugin.getLogger().warning("Invalid configuration tool material '" + materialName + "', defaulting to STICK.");
        }
        toolMaterial = material;
        double configuredRange = config.getDouble(DISPLAY_RANGE_PATH, 10.0D);
        if (configuredRange < 1.0D) {
            configuredRange = 1.0D;
        }
        displayRange = configuredRange;
        int configuredMax = config.getInt(MAX_DESTINATIONS_PATH, 32);
        if (configuredMax < 0) {
            configuredMax = 0;
            plugin.getLogger().warning("max-destinations-per-switch cannot be negative; treating as unlimited.");
        }
        maxDestinationsPerSwitch = configuredMax;
    }

    public Material getToolMaterial() {
        return toolMaterial;
    }

    public double getDisplayRange() {
        return displayRange;
    }

    public int getMaxDestinationsPerSwitch() {
        return maxDestinationsPerSwitch;
    }
}
