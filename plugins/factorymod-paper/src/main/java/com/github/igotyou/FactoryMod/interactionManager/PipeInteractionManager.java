package com.github.igotyou.FactoryMod.interactionManager;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Pipe;
import com.github.igotyou.FactoryMod.repairManager.NoRepairDestroyOnBreakManager;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.structures.PipeStructure;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;

public class PipeInteractionManager implements IInteractionManager {
	private Pipe pipe;
	private FactoryModManager manager;

	public PipeInteractionManager() {
		this.manager = FactoryMod.getInstance().getManager();
	}

	public void setPipe(Pipe pipe) {
		this.pipe = pipe;
	}

	@Override
	public void rightClick(Player p, Block b, BlockFace bf) {
		// no use for this here
	}

	@Override
	public void leftClick(Player p, Block b, BlockFace bf) {
		ItemStack hand = p.getInventory().getItemInMainHand();
		if (manager.isCitadelEnabled()) {
			Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(b);
			if (rein != null && !rein.hasPermission(p, FactoryMod.getInstance().getPermissionManager().getUseFactory())) {
				p.sendMessage(ChatColor.RED + "You dont have permission to interact with this factory");
				return;
			}
		}
		if (b.equals(((PipeStructure) (pipe.getMultiBlockStructure())).getStart())) {
			if (hand.getType() == manager.getFactoryInteractionMaterial()) {
				//TODO
			} else {
				if (pipe.isActive()) {
					p.sendMessage(
							ChatColor.RED + "You can not modify the allowed materials while the pipe is transferring");
					return;
				}
				List<Material> allowedMats = pipe.getAllowedMaterials();
				if (allowedMats == null || !allowedMats.contains(hand.getType())) {
					pipe.addAllowedMaterial(hand.getType());
					p.sendMessage(ChatColor.GREEN + "Added " + hand.getType().toString() + " as allowed material");
				} else {
					pipe.removeAllowedMaterial(hand.getType());
					p.sendMessage(ChatColor.GOLD + "Removed " + hand.getType().toString() + " as allowed material");
				}
			}
		} else if (b.equals(((PipeStructure) (pipe.getMultiBlockStructure())).getFurnace())) {
			if (pipe.isActive()) {
				pipe.deactivate();
				p.sendMessage(ChatColor.GOLD + pipe.getName() + " has been deactivated");
			} else {
				pipe.attemptToActivate(p, false);
			}
		}
	}

	@Override
	public void redStoneEvent(BlockRedstoneEvent e, Block factoryBlock) {
		int threshold = manager.getRedstonePowerOn();
		if (!factoryBlock.getLocation()
				.equals(((PipeStructure) pipe.getMultiBlockStructure()).getFurnace().getLocation())) {
			return;
		}
		if (e.getOldCurrent() >= threshold && e.getNewCurrent() < threshold && pipe.isActive()) {
			if ((!manager.isCitadelEnabled()
					|| MultiBlockStructure.citadelRedstoneChecks(e.getBlock()))) {
				pipe.deactivate();
			}
		} else if (e.getOldCurrent() < threshold && e.getNewCurrent() >= threshold && !pipe.isActive()) {
			if (!manager.isCitadelEnabled()
					|| MultiBlockStructure.citadelRedstoneChecks(e.getBlock())) {
				pipe.attemptToActivate(null, false);
			}
		}
	}

	@Override
	public void blockBreak(Player p, Block b) {
		((NoRepairDestroyOnBreakManager) (pipe.getRepairManager())).breakIt();
		if (p != null) {
			p.sendMessage(ChatColor.RED + "Pipe was destroyed");
		}
	}
}
