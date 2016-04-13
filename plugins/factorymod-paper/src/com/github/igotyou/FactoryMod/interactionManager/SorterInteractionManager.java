package com.github.igotyou.FactoryMod.interactionManager;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.civmodcore.itemHandling.NiceNames;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.Sorter;
import com.github.igotyou.FactoryMod.structures.BlockFurnaceStructure;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.utility.MenuBuilder;

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
			p.sendMessage(ChatColor.DARK_RED + "The sorter was destroyed");
		}
	}

	public void rightClick(Player p, Block b, BlockFace bf) {
		// not needed here
	}

	public void leftClick(Player p, Block b, BlockFace bf) {
		if (FactoryMod.getManager().isCitadelEnabled()) {
			ReinforcementManager rm = Citadel.getReinforcementManager();
			// is this cast safe? Let's just assume yes for now
			PlayerReinforcement rein = (PlayerReinforcement) rm
					.getReinforcement(b);
			if (rein != null) {
				Group g = rein.getGroup();
				if (!NameAPI.getGroupManager().hasAccess(g.getName(), p.getUniqueId(), PermissionType.getPermission("USE_FACTORY"))) {
					p.sendMessage(ChatColor.RED
							+ "You dont have permission to interact with this factory");
					FactoryMod.sendResponse("FactoryNoPermission", p);
					return;
				}
			}
		}
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
			if (is == null) {
				return;
				//no item in hand
			}
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

	public void redStoneEvent(BlockRedstoneEvent e, Block factoryBlock) {
		ReinforcementManager rm = FactoryMod.getManager().isCitadelEnabled() ? Citadel
				.getReinforcementManager() : null;
		int threshold = FactoryMod.getManager().getRedstonePowerOn();
		if (factoryBlock.getLocation().equals(
				((BlockFurnaceStructure) sorter.getMultiBlockStructure())
						.getFurnace().getLocation())) {
			if (e.getOldCurrent() >= threshold && e.getNewCurrent() < threshold
					&& sorter.isActive()) {
				if ((rm == null || MultiBlockStructure.citadelRedstoneChecks(e
						.getBlock()))) {
					sorter.deactivate();
				}
			} else if (e.getOldCurrent() < threshold
					&& e.getNewCurrent() >= threshold && !sorter.isActive()) {
				if (rm == null
						|| MultiBlockStructure.citadelRedstoneChecks(e
								.getBlock())) {
					sorter.attemptToActivate(null);
				}
			} else {
				return;
			}
		}
	}
}
