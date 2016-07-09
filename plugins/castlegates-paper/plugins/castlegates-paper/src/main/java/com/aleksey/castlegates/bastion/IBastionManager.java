/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.bastion;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface IBastionManager {
	void init();
	boolean canUndraw(List<Player> players, List<Block> bridgeBlocks); 
}
