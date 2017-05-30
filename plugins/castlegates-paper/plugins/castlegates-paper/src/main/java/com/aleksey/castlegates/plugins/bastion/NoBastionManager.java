/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.plugins.bastion;

import java.util.List;

import com.aleksey.castlegates.plugins.jukealert.IJukeAlert;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class NoBastionManager implements IBastionManager {
	public void init() {
	}

	public boolean canUndraw(List<Player> players, List<Block> bridgeBlocks, IJukeAlert jukeAlert) {
		return true;
	}
}
