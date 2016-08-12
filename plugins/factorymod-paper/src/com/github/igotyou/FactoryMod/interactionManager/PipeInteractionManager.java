package com.github.igotyou.FactoryMod.interactionManager;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Pipe;
import com.github.igotyou.FactoryMod.repairManager.NoRepairDestroyOnBreakManager;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.structures.PipeStructure;
import com.github.igotyou.FactoryMod.utility.MenuBuilder;

public class PipeInteractionManager implements IInteractionManager {
	private Pipe pipe;
	private MenuBuilder mb;
	private FactoryModManager manager;

	public PipeInteractionManager() {
		this.manager = FactoryMod.getManager();
		this.mb = FactoryMod.getMenuBuilder();
	}

	public void setPipe(Pipe pipe) {
		this.pipe = pipe;
	}

	public void rightClick(Player p, Block b, BlockFace bf) {
		// no use for this here
	}

	public void leftClick(Player p, Block b, BlockFace bf) {
		ItemStack hand = p.getInventory().getItemInMainHand();
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
		if (b.equals(((PipeStructure) (pipe.getMultiBlockStructure()))
				.getStart())) {
			if (hand.getType() == manager.getFactoryInteractionMaterial()) {
				mb.showPipeMaterials(p, pipe);
			} else {
				if (pipe.isActive()) {
					p.sendMessage(ChatColor.RED
							+ "You can not modify the allowed materials while the pipe is transferring");
					return;
				}
				List<Material> allowedMats = pipe.getAllowedMaterials();
				if (allowedMats == null
						|| !allowedMats.contains(hand.getType())) {
					pipe.addAllowedMaterial(hand.getType());
					p.sendMessage(ChatColor.GREEN + "Added "
							+ hand.getType().toString()
							+ " as allowed material");
				} else {
					pipe.removeAllowedMaterial(hand.getType());
					p.sendMessage(ChatColor.GOLD + "Removed "
							+ hand.getType().toString()
							+ " as allowed material");
				}
			}
		} else if (b.equals(((PipeStructure) (pipe.getMultiBlockStructure()))
				.getFurnace())) {
			if (pipe.isActive()) {
				pipe.deactivate();
				p.sendMessage(ChatColor.GOLD + pipe.getName()
						+ " has been deactivated");
			} else {
				pipe.attemptToActivate(p, false);
			}
		}
	}

	public void redStoneEvent(BlockRedstoneEvent e, Block factoryBlock) {
		ReinforcementManager rm = FactoryMod.getManager().isCitadelEnabled() ? Citadel
				.getReinforcementManager() : null;
		int threshold = FactoryMod.getManager().getRedstonePowerOn();
		if (factoryBlock.getLocation().equals(
				((PipeStructure) pipe.getMultiBlockStructure()).getFurnace()
						.getLocation())) {
			if (e.getOldCurrent() >= threshold && e.getNewCurrent() < threshold
					&& pipe.isActive()) {
				if ((rm == null || MultiBlockStructure.citadelRedstoneChecks(e
						.getBlock()))) {
					pipe.deactivate();
				}
			} else if (e.getOldCurrent() < threshold
					&& e.getNewCurrent() >= threshold && !pipe.isActive()) {
				if (rm == null
						|| MultiBlockStructure.citadelRedstoneChecks(e
								.getBlock())) {
					pipe.attemptToActivate(null, false);
				}
			} else {
				return;
			}
		}
	}

	public void blockBreak(Player p, Block b) {
		((NoRepairDestroyOnBreakManager) (pipe.getRepairManager())).breakIt();
		if (p != null) {
			p.sendMessage(ChatColor.RED + "Pipe was destroyed");
		}
	}
}
