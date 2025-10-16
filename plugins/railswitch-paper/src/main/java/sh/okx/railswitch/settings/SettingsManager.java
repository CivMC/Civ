package sh.okx.railswitch.settings;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import sh.okx.railswitch.RailSwitchPlugin;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;

/**
 * Manages the initialisation and registration of menu settings.
 */
public final class SettingsManager {

    private static RailSwitchMenu menu;

    private static DestinationSetting destSetting;

    private static ResetSetting resetSetting;

    private static VisualizationSetting visualModeSetting;


    /**
     * Initialise the settings manager. This should only be called within RailSwitch onEnable().
     *
     * @param plugin The enabled RailSwitch plugin instance.
     */
    public static void init(RailSwitchPlugin plugin) {
        // Create the menu elements
        menu = new RailSwitchMenu();
        destSetting = new DestinationSetting(plugin);
        resetSetting = new ResetSetting(plugin, destSetting);

        visualModeSetting = new VisualizationSetting(
            plugin,
            VisualizationMode.VISUALS,
            "Visualization mode",
            "railswitchVizMode",
            "Cycle between Disabled, Visuals, and Prediction to control how routing overlays are displayed when holding the RailSwitch tool."
        );
        // Register those elements
        menu.registerToParentMenu();
        menu.registerSetting(destSetting);
        menu.registerSetting(resetSetting);
        menu.registerSetting(visualModeSetting);
    }

    /**
     * Gracefully resets the settings manager. This should only be called within RailSwitch onDisable().
      * Note: Menu elements are not deregistered as PlayerSettingAPI does not currently support safe deregistration.
      */
     public static void reset() {
         menu = null;
         destSetting = null;
         resetSetting = null;
     }

    /**
     * Sets a player's destination. This is for when the player settings don't themselves provide a way to do so.
     *
     * @param player      The player to set the destination to.
     * @param destination The destination to set.
     */
    public static void setDestination(Player player, String destination) {
        Preconditions.checkArgument(player != null);
        if (Strings.isNullOrEmpty(destination)) {
            if (resetSetting != null) {
                resetSetting.resetPlayerDestination(player);
                // Do not put a message here since the message is sent in the method above.
            } else {
                player.sendMessage(Component.text("Could not reset your destination.", NamedTextColor.RED));
            }
        } else {
            if (destSetting != null) {
                destSetting.setValue(player, destination);
                player.sendMessage(Component.text("Destination set to: " + destination, NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Could not set your destination.", NamedTextColor.RED));
            }
        }
    }

    /**
     * Gets a player's destination.
     *
     * @param player The player to get the destination for.
     * @return Returns the player's destination, which will never be null.
     */
    public static String getDestination(Player player) {
        String value = destSetting.getValue(player);
        if (value == null) {
            return "";
        }
        return value;
    }

    /**
     * Determines whether prediction displays are enabled for the given player.
     *
     * @param player The player to query.
     * @return {@code true} if prediction displays should be shown, {@code false} otherwise.
     */
    public static boolean isPredictionEnabled(Player player) {
        if (visualModeSetting == null || player == null) {
            return true;
        }
        VisualizationMode mode = visualModeSetting.getValue(player);
        if (mode == null) {
            mode = VisualizationMode.PREDICTION;
        }
        return mode == VisualizationMode.PREDICTION;
    }

    /**
     * Determines whether visualizations are enabled for the given player.
     *
     * @param player The player to query.
     * @return {@code true} if visualizations should be shown, {@code false} otherwise.
     */
    public static boolean isVisualsEnabled(Player player) {
        if (visualModeSetting == null || player == null) {
            return true;
        }
        VisualizationMode mode = visualModeSetting.getValue(player);
        if (mode == null) {
            mode = VisualizationMode.PREDICTION;
        }
        return mode != VisualizationMode.DISABLED;
    }
}
