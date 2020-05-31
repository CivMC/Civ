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
				setBsiOverlay(Bukkit.getPlayer(player), newValue);
			}
		});
		settingMan.getBsiLocation().registerListener(new SettingChangeListener<String>() {
			@Override
			public void handle(UUID player, PlayerSetting<String> setting, String oldValue, String newValue) {
				setBsiOverlay(Bukkit.getPlayer(player), settingMan.getBsiOverlay().getValue(player));
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
			// Player didn't move by at least one block
			return;
		}

		if (!settingMan.getBsiOverlay().getValue(pme.getPlayer())) {
			return;
		}

		checkLocationForBastions(pme.getPlayer(), to);
	}

	private void setBsiOverlay(Player player, boolean state) {
		updateDisplaySetting(player, settingMan.getBsiLocation(), state,"",
				bsiBottomLine, bsiBoard);
	}

	/**
	 * This method handles the updating of the scoreboard/action bar
	 * as to whether the player is in a bastion field or not.
	 *
	 * @param player to update bastion status to
	 * @param location to check for bastion status
	 */
	public void checkLocationForBastions(Player player, Location location) {
		if (player == null || location == null) {
			return;
		}
		Set<BastionBlock> bastionBlocks = Bastion.getBastionManager().getBlockingBastions(location);
		if (bastionBlocks.isEmpty()) {
			if (settingMan.getShowNoBastion(player.getUniqueId())) {
				updateDisplaySetting(player, settingMan.getBsiLocation(), settingMan.getBsiOverlay().getValue(player.getUniqueId()),
						ChatColor.WHITE + "No Bastion", bsiBottomLine, bsiBoard);
				return;
			}
			bsiBoard.hide(player);
			bsiBottomLine.removePlayer(player);
			return;
		}
		//We want to check if im in the field of a bastion I dont have perms on
		//And if so, display enemy bastion
		//Otherwise we're in a friendly bastion/no bastion
		for (BastionBlock bastions : bastionBlocks) {
			if (!bastions.canPlace(player)) {
				updateDisplaySetting(player, settingMan.getBsiLocation(), settingMan.getBsiOverlay().getValue(player.getUniqueId()),
						"" + ChatColor.RED + ChatColor.BOLD + "Enemy Bastion", bsiBottomLine, bsiBoard);
				return;
			}
		}
		updateDisplaySetting(player, settingMan.getBsiLocation(), settingMan.getBsiOverlay().getValue(player.getUniqueId()),
				ChatColor.GREEN + "Friendly Bastion", bsiBottomLine, bsiBoard);
	}

	/**
	 * This method simply updates the players information
	 *
	 * @param player to update scoreboard/action bar on
	 * @param locSetting PlayerSetting of where to display information
	 * @param state Value of the PlayerSetting
	 * @param text Text to display
	 * @param bottomLine Action Bar to update
	 * @param scoreBoard Scoreboard to update
	 */
	private void updateDisplaySetting(Player player, DisplayLocationSetting locSetting, boolean state, String text,
									  BottomLine bottomLine, CivScoreBoard scoreBoard) {
		if (player == null) {
			return;
		}
		if (text == null) {
			state = false;
		}
		if (!state) {
			// always clean up, value might have been changed
			bottomLine.removePlayer(player);
			scoreBoard.hide(player);
		} else {
			if (locSetting.showOnActionbar(player.getUniqueId())) {
				bottomLine.updatePlayer(player, text);
			}
			if (locSetting.showOnSidebar(player.getUniqueId())) {
				scoreBoard.set(player, text);
			}
		}
	}
}


//fix empty string in setBSIOverlay
