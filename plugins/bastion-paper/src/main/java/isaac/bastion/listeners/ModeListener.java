package isaac.bastion.listeners;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.utils.BastionSettingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import vg.civcraft.mc.civmodcore.playersettings.impl.DisplayLocationSetting;
import vg.civcraft.mc.civmodcore.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.scoreboard.side.CivScoreBoard;
import vg.civcraft.mc.civmodcore.scoreboard.side.ScoreBoardAPI;
import vg.civcraft.mc.civmodcore.util.DoubleInteractFixer;

import java.util.Set;


public class ModeListener implements Listener {

	private DoubleInteractFixer interactFixer;
	private BottomLine bsiBottomLine;
	private CivScoreBoard bsiBoard;
	private BastionSettingManager settingMan;

	public ModeListener(Bastion bastion) {
		interactFixer = new DoubleInteractFixer(bastion);
		this.bsiBoard = ScoreBoardAPI.createBoard("bsiDisplay");
		this.bsiBottomLine = BottomLineAPI.createBottomLine("bsiDisplay", 3);
		this.settingMan = Bastion.getSettingManager();
		settingMan.getBsiOverlay().registerListener((player, setting, oldValue, newValue) ->
				updateDisplaySetting(Bukkit.getPlayer(player), settingMan.getBsiLocation(), false, "", bsiBottomLine, bsiBoard));
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
	public void handleInteractBlock(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (interactFixer.checkInteracted(e.getPlayer(), e.getClickedBlock())) {
				return;
			}
		} else if (e.getAction() != Action.LEFT_CLICK_BLOCK) {
			return;
		}
		//Checking setting is true
		if (!settingMan.getBsiOverlay().getValue(e.getPlayer())) {
			return;
		}
		/**
		 * Grabbing the set of bastions overlapping the location here since when bastions overlap, the scoreboard
		 * fights between friendly/enemy bastion every time either event is fired. This way we can simply display
		 * when the click or player location is in an overlapping area.
		 */
		Set<BastionBlock> bastionBlocks = Bastion.getBastionManager().getBlockingBastions(e.getClickedBlock().getLocation());
		//Checking here if there are no bastions at click location
		if (bastionBlocks.size() == 0) {
			e.getPlayer().sendMessage(ChatColor.YELLOW + "No bastion block");
			updateDisplaySetting(e.getPlayer(), settingMan.getBsiLocation(), true, "No Bastion Block", bsiBottomLine, bsiBoard);
			return;
		}
		//Only 1 bastion at click location
		if (bastionBlocks.size() == 1) {
			if (bastionBlocks.iterator().next().canPlace(e.getPlayer())) {
				e.getPlayer().sendMessage(ChatColor.GREEN + "Friendly Bastion");
				updateDisplaySetting(e.getPlayer(), settingMan.getBsiLocation(), true, ChatColor.GREEN + "Inside friendly bastion field", bsiBottomLine, bsiBoard);
				return;
			}
			e.getPlayer().sendMessage(ChatColor.RED + "Enemy Bastion");
			updateDisplaySetting(e.getPlayer(), settingMan.getBsiLocation(), true, ChatColor.RED + "Inside enemy bastion field", bsiBottomLine, bsiBoard);
			return;
		}
		//This should be fired when clicking in a zone where 2 bastions overlap
		e.getPlayer().sendMessage(ChatColor.YELLOW + "Bastions Overlapping");
		updateDisplaySetting(e.getPlayer(), settingMan.getBsiLocation(), true, ChatColor.YELLOW + "Inside overlapped bastion fields", bsiBottomLine, bsiBoard);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent pme) {

		if (!settingMan.getBsiOverlay().getValue(pme.getPlayer())) {
			return;
		}

		Set<BastionBlock> bastionBlocks = Bastion.getBastionManager().getBlockingBastions(pme.getPlayer().getLocation());
		//Checking here if there are no bastions at player location
		if (bastionBlocks.size() == 0) {
			updateDisplaySetting(pme.getPlayer(), settingMan.getBsiLocation(), true, "No Bastion Block", bsiBottomLine, bsiBoard);
			return;
		}
		//Only 1 bastion at player location
		if (bastionBlocks.size() == 1) {
			if (bastionBlocks.iterator().next().canPlace(pme.getPlayer())) {
				updateDisplaySetting(pme.getPlayer(), settingMan.getBsiLocation(), true, ChatColor.GREEN + "Inside friendly bastion field", bsiBottomLine, bsiBoard);
				return;
			}
			updateDisplaySetting(pme.getPlayer(), settingMan.getBsiLocation(), true, ChatColor.RED + "Inside enemy bastion field", bsiBottomLine, bsiBoard);
			return;
		}
		//This should be fired when player is in a zone where 2 bastions overlap
		updateDisplaySetting(pme.getPlayer(), settingMan.getBsiLocation(), true, ChatColor.YELLOW + "Inside overlapped bastion fields", bsiBottomLine, bsiBoard);
	}
}
