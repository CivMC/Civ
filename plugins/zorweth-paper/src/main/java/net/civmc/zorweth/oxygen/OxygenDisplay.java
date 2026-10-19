package net.civmc.zorweth.oxygen;

import java.text.DecimalFormat;
import net.civmc.zorweth.ZorwethPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.CivScoreBoard;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.DisplayLocationSetting;

public class OxygenDisplay implements Listener {

    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#");
    private static final DecimalFormat CHANGE_FORMAT = new DecimalFormat("+#;-#");

    private final BooleanSetting showOxygen;
    private final DisplayLocationSetting oxygenLocation;

    private final CivScoreBoard oxygenScoreboard;
    private final BottomLine oxygenBottomLine;

    private final OxygenManager oxygenManager;

    public OxygenDisplay(ZorwethPlugin plugin, OxygenManager oxygenManager) {
        this.oxygenManager = oxygenManager;
        MenuSection menu = plugin.getSettingsMenu();

        showOxygen = new BooleanSetting(plugin, true, "Show current oxygen level", "showOxygen",
            "Should oxygen be shown?");
        menu.registerSetting(showOxygen);
        oxygenLocation = new DisplayLocationSetting(plugin, DisplayLocationSetting.DisplayLocation.SIDEBAR,
            "Oxygen Location", "oxygenLocation", new ItemStack(Material.ARROW), "oxygen location");
        menu.registerSetting(oxygenLocation);

        oxygenScoreboard = ScoreBoardAPI.createBoard("ZorwethOxygenDisplay");
        oxygenBottomLine = BottomLineAPI.createBottomLine("ZorwethOxygenDisplay", 5);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboardHUD(player);
            }
        }, 20, 20);
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        updateScoreboardHUD(event.getPlayer());
    }

    public void updateScoreboardHUD(Player p) {
        double oxygen = oxygenManager.getOxygen(p);
        if (!showOxygen.getValue(p.getUniqueId()) || oxygen >= OxygenBladder.getMaxOxygen(p)) {
            oxygenScoreboard.hide(p);
            oxygenBottomLine.removePlayer(p);
            return;
        }

        final String change = formatChange(oxygenManager.getLastOxygenTickChange(p));
        String text;
        if (oxygen < -0.3) {
            text = ChatColor.DARK_AQUA + "Oxygen: " + ChatColor.BOLD + ChatColor.DARK_RED + "CRITICAL " + change;
        } else if (oxygen < -0.05) {
            text = ChatColor.DARK_AQUA + "Oxygen: " + ChatColor.BOLD + ChatColor.RED + "SUFFOCATING " + change;
        } else if (oxygen < 0) {
            text = ChatColor.DARK_AQUA + "Oxygen: " + ChatColor.RED + "LOW " + change;
        } else {
            text = ChatColor.DARK_AQUA + "Oxygen: " + ChatColor.AQUA + PERCENT_FORMAT.format(oxygen * 1000)
                + " " + change;
        }
        if (oxygenLocation.showOnActionbar(p.getUniqueId())) {
            oxygenBottomLine.updatePlayer(p, text);
        }
        if (oxygenLocation.showOnSidebar(p.getUniqueId())) {
            oxygenScoreboard.set(p, text);
        }
    }

    private String formatChange(final double change) {
        final String formattedChange = CHANGE_FORMAT.format(change * 1000);
        final ChatColor color = change < 0 ? ChatColor.RED : ChatColor.GREEN;
        return ChatColor.GRAY + "(" + color + formattedChange + ChatColor.GRAY + "/s)";
    }
}
