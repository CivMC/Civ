/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.bastion;

import isaac.bastion.Bastion;

import java.util.List;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BastionManager implements IBastionManager {
	public boolean canUndraw(List<Player> players, Set<Block> bridgeBlocks) {
		if(players == null || players.size() == 0) {
			return Bastion.getBastionManager().shouldStopBlock(null, bridgeBlocks, null).size() == 0;
		}

		for(Player player : players) {
			if(Bastion.getBastionManager().shouldStopBlock(null, bridgeBlocks, player.getUniqueId()).size() == 0) {
				return true;
			}
		}
		
		return false;
	}
}
