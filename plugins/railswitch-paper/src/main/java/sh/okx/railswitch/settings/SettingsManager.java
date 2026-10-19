package sh.okx.railswitch.settings;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sh.okx.railswitch.RailSwitchPlugin;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.CivScoreBoard;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardAPI;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.DisplayLocationSetting;

/**
 * Manages the initialisation and registration of menu settings.
 */
public final class SettingsManager {

    private static RailSwitchMenu menu;

    private static DestinationSetting destSetting;

    private static ResetSetting resetSetting;
    private static DisplayLocationSetting destDisplayLocation;

    private static CivScoreBoard destScoreBoard;
    private static BottomLine destBottomLine;

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
        destDisplayLocation = new DisplayLocationSetting(plugin, DisplayLocationSetting.DisplayLocation.SIDEBAR,
            "Dest Display Location", "destDisplayLocation", new ItemStack(Material.ARROW), "destination");

        // Register those elements
        menu.registerToParentMenu();
        menu.registerSetting(destSetting);
        menu.registerSetting(resetSetting);
        menu.registerSetting(destDisplayLocation);

        destScoreBoard = ScoreBoardAPI.createBoard("RailSwitchDestDisplay");
        destBottomLine = BottomLineAPI.createBottomLine("RailSwitchDestDisplay", 4);

        // update player ui if dest setting is changed
        destSetting.registerListener((UUID uuid, PlayerSetting<String> setting, String oldValue, String newValue) -> {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                updateDestScoreboardHud(player);
            }
        });

        // update player ui if scoreboard display location setting is changed
        destDisplayLocation.registerListener((UUID uuid, PlayerSetting<String> setting, String oldValue, String newValue) -> {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                updateDestScoreboardHud(player);
            }
        });
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
                player.sendMessage(ChatColor.RED + "Could not reset your destination.");
            }
        } else {
            if (destSetting != null) {
                destSetting.setValue(player, destination);
                player.sendMessage(ChatColor.GREEN + "Destination set to: " + destination);
            } else {
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
        String value = destSetting.getValue(player);
        if (value == null) {
            return "";
        }
        return value;
    }

    public static void updateDestScoreboardHud(Player p) {
        String dest = getDestination(p);
        if (Strings.isNullOrEmpty(dest)) {
            // remove if dest is empty
            destScoreBoard.hide(p);
            destBottomLine.removePlayer(p);
        } else {
            // set dest on hud
            if (destDisplayLocation.showOnSidebar(p.getUniqueId())) {
                destScoreBoard.set(p, ChatColor.GOLD + "Dest: " + ChatColor.AQUA + dest);
            } else {
                // remove if disabled as the dest might still be showing on the sidebar from before
                destScoreBoard.hide(p);
            }
            if (destDisplayLocation.showOnActionbar(p.getUniqueId())) {
                destBottomLine.updatePlayer(p, ChatColor.GOLD + "Dest: " + ChatColor.AQUA + dest);
            } else {
                // remove if disabled as the dest might still be showing on the actionbar from before
                destBottomLine.removePlayer(p);
            }
        }
    }
}
