package com.github.igotyou.FactoryMod.interactionManager;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Pipe;
import com.github.igotyou.FactoryMod.repairManager.NoRepairDestroyOnBreakManager;
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
		ItemStack hand = p.getItemInHand();
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
				pipe.attemptToActivate(p);
			}
		}
	}

	public void redStoneEvent(BlockRedstoneEvent e) {
		// soon
	}

	public void blockBreak(Player p, Block b) {
		((NoRepairDestroyOnBreakManager) (pipe.getRepairManager())).breakIt();
		if (p != null) {
			p.sendMessage(ChatColor.RED + "Pipe was destroyed");
		}
	}

}
