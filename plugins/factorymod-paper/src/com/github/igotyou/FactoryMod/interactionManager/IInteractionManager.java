package com.github.igotyou.FactoryMod.interactionManager;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;

public interface IInteractionManager {
	public void rightClick(Player p, Block b);

	public void leftClick(Player p, Block b);

	public void blockBreak(Player p, Block b);
	
	public void failedTurnOnDisrepair(Player p);
	
	public void failedTurnOnPermission(Player p);

	public void successfullTurnOn(Player p);
	
	public void turnOff(Player p);
	
	public void redStoneEvent(BlockRedstoneEvent e);

}
