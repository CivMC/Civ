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
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class BastionManager implements IBastionManager {
	private static final String PERMISSION_UNDRAW = "BASTION_BRIDGE_UNDRAW";

	private BastionBlockManager _blockManager;

	public void init() {
		_blockManager = Bastion.getBastionManager();

		LinkedList <PlayerType> memberAndAbove = new LinkedList<>();
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

	private boolean hasBastionAccess(List<Player> players, Block block, ICitadel citadel) {
		PermissionType perm = PermissionType.getPermission(PERMISSION_UNDRAW);
		Location loc = block.getLocation();
		Set<BastionBlock> bastions = _blockManager.getBlockingBastions(loc);

		if (bastions == null)
			return true;

		ReinforcementManager reinManager = Citadel.getInstance().getReinforcementManager();

		for(BastionBlock bastion : bastions) {
			if(bastion.getType().isOnlyDirectDestruction() || !bastion.inField(loc))
				continue;

			Reinforcement rein = reinManager.getReinforcement(bastion.getLocation());

			if(rein == null)
				continue;

			if(players == null)
				return false;

			if(citadel.useJukeAlert() && !rein.getGroup().getName().equalsIgnoreCase(citadel.getGroupName()))
				return false;

			boolean hasAccess = false;

			for (Player player : players) {
				if (bastion.permAccess(player, perm)) {
					hasAccess = true;
					break;
				}
			}

			if (!hasAccess)
				return false;
		}

		return true;
	}
}