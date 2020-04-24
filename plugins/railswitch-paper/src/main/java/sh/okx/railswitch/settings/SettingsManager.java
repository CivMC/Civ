package sh.okx.railswitch.settings;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import sh.okx.railswitch.RailSwitchPlugin;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;

/**
 * Manages the initialisation and registration of menu settings.
 */
public final class SettingsManager {

    private static RailSwitchMenu menu;

    private static DestinationSetting destSetting;

    private static ResetSetting resetSetting;

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
        // Register those elements
        menu.registerToParentMenu();
        menu.registerSetting(destSetting);
        menu.registerSetting(resetSetting);
    }

    /**
     * Gracefully resets the settings manager. This should only be called within RailSwitch onDisable().
     */
    public static void reset() {
        // TODO: Deregister and unload all the menu elements once PlayerSettingAPI becomes reload safe
        menu = null;
        destSetting = null;
        resetSetting = null;
    }

    /**
     * Sets a player's destination. This is for when the player settings don't themselves provide a way to do so.
     *
     * @param player The player to set the destination to.
     * @param destination The destination to set.
     */
    public static void setDestination(Player player, String destination) {
        Preconditions.checkArgument(player != null);
        if (Strings.isNullOrEmpty(destination)) {
            if (resetSetting != null) {
                resetSetting.resetPlayerDestination(player);
                player.sendMessage(ChatColor.GREEN + "Your destination has been reset.");
            }
            else {
                player.sendMessage(ChatColor.RED + "Could not reset your destination.");
            }
        }
        else {
            if (destSetting != null) {
                destSetting.setValue(player, destination);
                player.sendMessage(ChatColor.GREEN + "Destination set to: " + destination);
            }
            else {
                player.sendMessage(ChatColor.RED + "Could not set your destination.");
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
        return NullCoalescing.chain(() -> destSetting.getValue(player), "");
    }

}
