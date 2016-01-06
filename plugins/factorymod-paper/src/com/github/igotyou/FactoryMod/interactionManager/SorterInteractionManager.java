package com.github.igotyou.FactoryMod.interactionManager;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.Sorter;
import com.github.igotyou.FactoryMod.structures.BlockFurnaceStructure;

public class SorterInteractionManager implements IInteractionManager {
	private Sorter sorter;
	private BlockFurnaceStructure bfs;

	public SorterInteractionManager(Sorter sorter) {
		setSorter(sorter);
	}

	public SorterInteractionManager() {

	}

	public void setSorter(Sorter sorter) {
		this.sorter = sorter;
		this.bfs = (BlockFurnaceStructure) sorter.getMultiBlockStructure();
	}

	public void blockBreak(Player p, Block b) {
		sorter.getRepairManager().breakIt();
		if (p != null) {
			p.sendMessage(ChatColor.RED + "The sorter was destroyed");
		}
	}

	public void rightClick(Player p, Block b, BlockFace bf) {
		//not needed here
	}

	public void leftClick(Player p, Block b, BlockFace bf) {
		if (b.equals(bfs.getFurnace())) {
			if (p.getItemInHand()
					.getType()
					.equals(FactoryMod.getManager()
							.getFactoryInteractionMaterial())) {
				sorter.attemptToActivate(p);
			}
		} else { // center
			ItemStack is = p.getItemInHand();
			BlockFace side = sorter.getSide(is);
			if (side == null) {
				sorter.addAssignment(bf, is);
				p.sendMessage(ChatColor.GREEN + "Added "
						+ is.getType().toString() + " to " + bf.toString());
			} else {
				if (side == bf) {
					sorter.removeAssignment(is);
					p.sendMessage(ChatColor.GOLD + "Removed "
							+ is.getType().toString() + " from "
							+ side.toString());
				} else {
					p.sendMessage(ChatColor.RED
							+ "This item is already associated with " + side.toString());
				}
			}
		}
	}

	public void redStoneEvent(BlockRedstoneEvent e) {

	}

}
