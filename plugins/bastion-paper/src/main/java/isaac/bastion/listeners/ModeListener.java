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
				updateDisplaySetting(Bukkit.getPlayer(player), settingMan.getBsiLocation(), newValue, "", bsiBottomLine, bsiBoard);
			}
		});
	}
	private static void updateDisplaySetting(Player player, DisplayLocationSetting locSetting, boolean state, String text,
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

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerJoinEvent pje) {
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

		checkLocationForBastions(pme.getPlayer(), pme.getPlayer().getLocation());
	}

	public void checkLocationForBastions(Player player, Location loc) {
		/**
		 * This code should iterate over all the bastions covering a location and handle overlaps
		 */
		Set<BastionBlock> bastionBlocks = Bastion.getBastionManager().getBlockingBastions(loc);
		if (bastionBlocks.isEmpty()) {
			updateDisplaySetting(player, settingMan.getBsiLocation(), true,ChatColor.WHITE + "No Bastion", bsiBottomLine, bsiBoard);
			return;
		}
		//We want to check if im in the field of a bastion I dont have perms on
		//And if so, display enemy bastion
		//Otherwise we're in a friendly bastion/no bastion
		for (BastionBlock bastions : bastionBlocks) {
			if (!bastions.canPlace(player)) {
				updateDisplaySetting(player, settingMan.getBsiLocation(), true,"" + ChatColor.RED + ChatColor.BOLD + "Enemy Bastion", bsiBottomLine, bsiBoard);
				return;
			}
		}
		updateDisplaySetting(player, settingMan.getBsiLocation(), true, ChatColor.GREEN + "Friendly Bastion", bsiBottomLine, bsiBoard);
		return;
	}
}


//change listener to location setting
//fix normal settingchange listener
//fix empty string in registerSetting
