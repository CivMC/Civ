/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.bastion;

import java.util.List;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface IBastionManager {
	boolean canUndraw(List<Player> players, Set<Block> bridgeBlocks); 
}
