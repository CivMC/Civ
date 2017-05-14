/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.plugins.bastion;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class NoBastionManager implements IBastionManager {
	public void init() {
	}

	public boolean canUndraw(List<Player> players, List<Block> bridgeBlocks) {
		return true;
	}
}
