package isaac.bastion.listeners;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.utils.BastionSettingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;
import vg.civcraft.mc.civmodcore.playersettings.SettingChangeListener;
import vg.civcraft.mc.civmodcore.playersettings.impl.DisplayLocationSetting;
import vg.civcraft.mc.civmodcore.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.scoreboard.side.CivScoreBoard;
import vg.civcraft.mc.civmodcore.scoreboard.side.ScoreBoardAPI;

import java.util.Set;
import java.util.UUID;


public class ModeListener implements Listener {

	private BottomLine bsiBottomLine;
	private CivScoreBoard bsiBoard;
	private BastionSettingManager settingMan;

	public ModeListener() {
		this.bsiBoard = ScoreBoardAPI.createBoard("bsiDisplay");
		this.bsiBottomLine = BottomLineAPI.createBottomLine("bsiDisplay", 3);
		this.settingMan = Bastion.getSettingManager();
		settingMan.getBsiOverlay().registerListener(new SettingChangeListener<Boolean>() {
			@Override
			public void handle(UUID player, PlayerSetting<Boolean> setting, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					checkLocationForBastions(Bukkit.getPlayer(player), Bukkit.getPlayer(player).getLocation());
					return;
				}
				bsiBoard.hide(Bukkit.getPlayer(player));
				bsiBottomLine.removePlayer(Bukkit.getPlayer(player));
			}
		});
		settingMan.getBsiLocation().registerListener(new SettingChangeListener<String>() {
			@Override
			public void handle(UUID player, PlayerSetting<String> setting, String oldValue, String newValue) {
				checkLocationForBastions(Bukkit.getPlayer(player), Bukkit.getPlayer(player).getLocation());
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent pje) {
		if (!settingMan.getBsiOverlay().getValue(pje.getPlayer())) {
			return;
		}
		checkLocationForBastions(pje.getPlayer(), pje.getPlayer().getLocation());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent pme) {
		Location from = pme.getFrom();
		Location to = pme.getTo();
		if (to == null) {
			return;
		}

		if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
				&& from.getBlockZ() == to.getBlockZ() && from.getWorld().equals(to.getWorld())) {
			return;
		}

		if (!settingMan.getBsiOverlay().getValue(pme.getPlayer())) {
			return;
		}

		checkLocationForBastions(pme.getPlayer(), to);
	}

	/**
	 * This method handles the updating of the scoreboard/action bar
	 * as to whether the player is in a bastion field or not.
	 *
	 * @param player to update bastion status to
	 * @param location to check for bastions
	 */
	public void checkLocationForBastions(Player player, Location location) {
		if (player == null || location == null) {
			return;
		}
		Set<BastionBlock> bastionBlocks = Bastion.getBastionManager().getBlockingBastions(location);
		if (bastionBlocks.isEmpty()) {
			if (settingMan.getShowNoBastion(player.getUniqueId())) {
				updateDisplaySetting(player, settingMan.getBsiLocation(), settingMan.getBsiOverlay().getValue(player.getUniqueId()),
						ChatColor.WHITE + "No Bastion");
				return;
			}
			bsiBoard.hide(player);
			bsiBottomLine.removePlayer(player);
			return;
		}
		boolean isFriendly = false;
		boolean isEnemy = false;
		for (BastionBlock bastions : bastionBlocks) {
			if (!bastions.getType().isBlockReinforcements() || bastions.canPlace(player)) {
				isFriendly = true;
			} else {
				isEnemy = true;
			}
		}
		if (isFriendly && isEnemy) {
			updateDisplaySetting(player, settingMan.getBsiLocation(), settingMan.getBsiOverlay().getValue(player.getUniqueId()),
					"" + ChatColor.YELLOW + "" + ChatColor.BOLD + "Overlapped Bastion");
			return;
		}
		if (isFriendly) {
			updateDisplaySetting(player, settingMan.getBsiLocation(), settingMan.getBsiOverlay().getValue(player.getUniqueId()),
					ChatColor.GREEN + "Friendly Bastion");
			return;
		}
		updateDisplaySetting(player, settingMan.getBsiLocation(), settingMan.getBsiOverlay().getValue(player.getUniqueId()),
				"" + ChatColor.RED + "" + ChatColor.BOLD + "Enemy Bastion");
	}

	/**
	 * This method simply updates the players information
	 *
	 * @param player to update scoreboard/action bar on
	 * @param locSetting PlayerSetting of where to display information
	 * @param state Value of the PlayerSetting
	 * @param text Text to display
	 */
	private void updateDisplaySetting(Player player, DisplayLocationSetting locSetting, boolean state, String text) {
		if (player == null) {
			return;
		}
		if (text == null) {
			state = false;
		}
		if (!state) {
			bsiBottomLine.removePlayer(player);
			bsiBoard.hide(player);
		} else {
			if (locSetting.showOnActionbar(player.getUniqueId())) {
				bsiBottomLine.updatePlayer(player, text);
			}
			if (locSetting.showOnSidebar(player.getUniqueId())) {
				bsiBoard.set(player, text);
			}
		}
	}
}
