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
import com.github.igotyou.FactoryMod.utility.MenuBuilder;
import com.github.igotyou.FactoryMod.utility.NiceNames;

public class SorterInteractionManager implements IInteractionManager {
	private Sorter sorter;
	private BlockFurnaceStructure bfs;
	private MenuBuilder mb;

	public SorterInteractionManager(Sorter sorter) {
		setSorter(sorter);
		mb = FactoryMod.getMenuBuilder();
	}

	public SorterInteractionManager() {
		mb = FactoryMod.getMenuBuilder();
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
		// not needed here
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
			if (p.isSneaking()
					&& p.getItemInHand()
							.getType()
							.equals(FactoryMod.getManager()
									.getFactoryInteractionMaterial())) {
				mb.showSorterFace(p, sorter, bf);
				return;
			}
			ItemStack is = p.getItemInHand();
			BlockFace side = sorter.getSide(is);
			if (side == null) {
				sorter.addAssignment(bf, is);
				p.sendMessage(ChatColor.GREEN + "Added "
						+ NiceNames.getName(is) + " to " + bf.toString());
			} else {
				if (side == bf) {
					sorter.removeAssignment(is);
					p.sendMessage(ChatColor.GOLD + "Removed "
							+ NiceNames.getName(is) + " from "
							+ side.toString());
				} else {
					p.sendMessage(ChatColor.RED
							+ "This item is already associated with "
							+ side.toString());
				}
			}
		}
	}

	public void redStoneEvent(BlockRedstoneEvent e) {

	}

}
