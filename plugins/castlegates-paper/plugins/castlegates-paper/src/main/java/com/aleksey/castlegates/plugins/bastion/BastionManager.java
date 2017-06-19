/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.plugins.bastion;

import com.aleksey.castlegates.plugins.citadel.ICitadel;
import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.manager.BastionBlockManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.civmodcore.locations.QTBox;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class BastionManager implements IBastionManager {
	private static final String PERMISSION_UNDRAW = "BASTION_BRIDGE_UNDRAW";

	private BastionBlockManager manager;

	public void init() {
		this.manager = Bastion.getBastionManager();

		LinkedList <PlayerType> memberAndAbove = new LinkedList<PlayerType>();
		memberAndAbove.add(PlayerType.MEMBERS);
		memberAndAbove.add(PlayerType.MODS);
		memberAndAbove.add(PlayerType.ADMINS);
		memberAndAbove.add(PlayerType.OWNER);
		PermissionType.registerPermission(PERMISSION_UNDRAW, memberAndAbove, "Allows undrawing bridges/gates above bastions");
	}

	public boolean canUndraw(List<Player> players, List<Block> bridgeBlocks, ICitadel citadel) {
		if (players != null) {
			for(Player player : players) {
				if(player.hasPermission("Bastion.bypass")) {
					return true;
				}
			}
		}

		for(Block block : bridgeBlocks) {
			if(!hasBastionAccess(players, block, citadel)) {
				return false;
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean hasBastionAccess(List<Player> players, Block block, ICitadel citadel) {
		PermissionType perm = PermissionType.getPermission(PERMISSION_UNDRAW);
		Location loc = block.getLocation();
		Set<? extends QTBox> boxes = this.manager.getBlockingBastions(loc);
		Set<BastionBlock> bastions = null;

		if (boxes.size() != 0) {
			bastions = (Set<BastionBlock>) boxes;
		}

		if (bastions == null)  return true;

		ReinforcementManager reinManager = Citadel.getReinforcementManager();
		boolean hasAccess = true;

		for(BastionBlock bastion : bastions) {
			if(!bastion.inField(loc)) {
				continue;
			}

			Reinforcement rein = Citadel.getReinforcementManager().getReinforcement(loc);
			PlayerReinforcement playerRein = rein != null && (rein instanceof PlayerReinforcement) ? (PlayerReinforcement)rein : null;

			if(playerRein == null) continue;

			if(players == null) return false;

			if(citadel.useJukeAlert()) {
				if(citadel.getGroupName().equalsIgnoreCase(playerRein.getGroup().getName())) {
					return true;
				}
			} else {
				for (Player player : players) {
					if (bastion.permAccess(player, perm)) return true;
				}
			}

			hasAccess = false;
		}

		return hasAccess;
	}
}