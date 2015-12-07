package com.github.igotyou.FactoryMod.interactionManager;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface IInteractionManager {
	public void rightClick(Player p, Block b);

	public void leftClick(Player p, Block b);

	public void blockBreak(Player p, Block b);

}
