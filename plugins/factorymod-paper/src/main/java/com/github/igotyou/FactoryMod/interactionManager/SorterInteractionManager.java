package com.github.igotyou.FactoryMod.interactionManager;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Sorter;
import com.github.igotyou.FactoryMod.structures.BlockFurnaceStructure;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;

import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.api.ItemNames;
public class SorterInteractionManager implements IInteractionManager {
	
	private Sorter sorter;
	private BlockFurnaceStructure bfs;
	private FactoryModManager manager;

	public SorterInteractionManager(Sorter sorter) {
		if (sorter != null) {
		setSorter(sorter);
		}
		manager = FactoryMod.getInstance().getManager();
	}

	public SorterInteractionManager() {
		this(null);
	}

	public void setSorter(Sorter sorter) {
		this.sorter = sorter;
		this.bfs = (BlockFurnaceStructure) sorter.getMultiBlockStructure();
	}

	@Override
	public void blockBreak(Player p, Block b) {
		sorter.getRepairManager().breakIt();
		if (p != null) {
			p.sendMessage(ChatColor.DARK_RED + "The sorter was destroyed");
		}
	}

	@Override
	public void rightClick(Player p, Block b, BlockFace bf) {
		// not needed here
	}

	@Override
	public void leftClick(Player p, Block b, BlockFace bf) {
		if (manager.isCitadelEnabled()) {
			Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(b);
			if (rein != null && !rein.hasPermission(p, FactoryMod.getInstance().getPermissionManager().getUseFactory())) {
				p.sendMessage(ChatColor.RED + "You dont have permission to interact with this factory");
				return;
			}
		}
		if (b.equals(bfs.getFurnace())) {
			if (p.getInventory().getItemInMainHand().getType()
					.equals(manager.getFactoryInteractionMaterial())) {
				sorter.attemptToActivate(p, false);
			}
		} else { // center
			if (p.isSneaking() && p.getInventory().getItemInMainHand().getType()
					.equals(manager.getFactoryInteractionMaterial())) {
				//TODO
				return;
			}
			ItemStack is = p.getInventory().getItemInMainHand();
			BlockFace side = sorter.getSide(is);
			if (side == null) {
				sorter.addAssignment(bf, is);
				p.sendMessage(ChatColor.GREEN + "Added " + ItemNames.getItemName(is) + " to " + bf.toString());
			} else {
				if (side == bf) {
					sorter.removeAssignment(is);
					p.sendMessage(ChatColor.GOLD + "Removed " + ItemNames.getItemName(is) + " from " + side.toString());
				} else {
					p.sendMessage(ChatColor.RED + "This item is already associated with " + side.toString());
				}
			}
		}
	}

	@Override
	public void redStoneEvent(BlockRedstoneEvent e, Block factoryBlock) {
		int threshold = manager.getRedstonePowerOn();
		if (!factoryBlock.getLocation()
				.equals(((BlockFurnaceStructure) sorter.getMultiBlockStructure()).getFurnace().getLocation())) {
			return;
		}
		if (e.getOldCurrent() >= threshold && e.getNewCurrent() < threshold && sorter.isActive()) {
			if ((!manager.isCitadelEnabled()
					|| MultiBlockStructure.citadelRedstoneChecks(e.getBlock()))) {
				sorter.deactivate();
			}
		} else if (e.getOldCurrent() < threshold && e.getNewCurrent() >= threshold && !sorter.isActive()) {
			if (!manager.isCitadelEnabled()
					|| MultiBlockStructure.citadelRedstoneChecks(e.getBlock())) {
				sorter.attemptToActivate(null, false);
			}
		}
	}
}
