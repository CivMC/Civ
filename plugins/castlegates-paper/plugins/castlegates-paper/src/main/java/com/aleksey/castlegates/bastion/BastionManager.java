/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.bastion;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.manager.BastionBlockManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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
		PermissionType.registerPermission(PERMISSION_UNDRAW, memberAndAbove, "Allows undrawing bridge/gates above bastion");
	}
	
	public boolean canUndraw(List<Player> players, List<Block> bridgeBlocks) {
		if (players != null) {
			for(Player player : players) {
				if(player.hasPermission("Bastion.bypass")) {
					return true;
				}
			}
		}
		
		for(Block block : bridgeBlocks) {
			if(!hasBastionAccess(players, block)) {
				return false;
			}
		}
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private boolean hasBastionAccess(List<Player> players, Block block) {
		PermissionType perm = PermissionType.getPermission(PERMISSION_UNDRAW);
		Location loc = block.getLocation();
		Set<? extends QTBox> boxes = this.manager.getBlockingBastions(loc);
		Set<BastionBlock> bastions = null;
		
		if (boxes.size() != 0) {
			bastions = (Set<BastionBlock>) boxes;
		}

		if (bastions == null) {
			return true;
		}
		
		boolean hasAccess = true;
		
		for(BastionBlock bastion : bastions) {
			if(!bastion.inField(loc)) {
				continue;
			}
			
			if(players == null) {
				return false;
			}
			
			for(Player player : players) {
				if (bastion.permAccess(player, perm)) {
					return true;
				}
			}
			
			hasAccess = false;
		}
		
		return hasAccess;
	}
}
