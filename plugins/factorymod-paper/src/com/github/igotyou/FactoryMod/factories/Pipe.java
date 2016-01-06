package com.github.igotyou.FactoryMod.factories;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.powerManager.IPowerManager;
import com.github.igotyou.FactoryMod.repairManager.IRepairManager;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.structures.PipeStructure;
import com.github.igotyou.FactoryMod.utility.ItemMap;

public class Pipe extends Factory {
	private List<Material> allowedMaterials;
	private int transferAmount;
	private int transferTimeMultiplier;
	private int runTime;

	public Pipe(IInteractionManager im, IRepairManager rm, IPowerManager pm,
			MultiBlockStructure mbs, int updateTime, String name,
			int transferTimeMultiplier, int transferAmount) {
		super(im, rm, pm, mbs, updateTime, name);
		this.transferTimeMultiplier = transferTimeMultiplier;
		this.transferAmount = transferAmount;
		allowedMaterials = null;
		runTime = 0;
	}

	public void attemptToActivate(Player p) {
		// TODO Citadel stuff
		mbs.recheckComplete();
		if (mbs.isComplete()) {
			if (transferMaterialsAvailable()) {
				if (pm.powerAvailable()) {
					if (p != null) {
						p.sendMessage(ChatColor.GREEN
								+ "Activated pipe transfer");
					}
					activate();
				} else {
					if (p != null) {
						p.sendMessage(ChatColor.RED
								+ "Failed to activate pipe, there is no fuel in the furnace");
					}
				}
			} else {
				if (p != null) {
					p.sendMessage(ChatColor.RED
							+ "No items available to transfer");
				}
			}
		}
	}

	public void activate() {
		active = true;
		pm.setPowerCounter(0);
		turnFurnaceOn(((PipeStructure) mbs).getFurnace());
		// reset the production timer
		runTime = 0;
		run();
	}

	public void deactivate() {
		active = false;
		turnFurnaceOff(((PipeStructure) mbs).getFurnace());
		runTime = 0;
	}

	public void run() {
		if (active && mbs.isComplete() && pm.powerAvailable()
				&& transferMaterialsAvailable()) {
			if (runTime >= getTransferTime()) {
				transfer();
				runTime = 0;
				if (transferMaterialsAvailable()) {
					scheduleUpdate();
				} else {
					deactivate();
				}
			} else {
				// if the time since fuel was last consumed is equal to
				// how often fuel needs to be consumed
				if (pm.getPowerCounter() >= pm.getPowerConsumptionIntervall() - 1) {
					// remove one fuel.
					pm.consumePower();
					// 0 seconds since last fuel consumption
					pm.setPowerCounter(0);
				}
				// if we don't need to consume fuel, just increase the
				// energy timer
				else {
					pm.increasePowerCounter(updateTime);
				}
				// increase the production timer
				runTime += updateTime;
				// schedule next update
				scheduleUpdate();
			}
		} else {
			deactivate();
		}
	}

	public void transfer() {
		if (mbs.isComplete()) {
			Inventory sourceInventory = ((InventoryHolder) (((PipeStructure) mbs)
					.getStart().getState())).getInventory();
			Inventory targetInventory = ((InventoryHolder) (((PipeStructure) mbs)
					.getEnd().getState())).getInventory();
			int leftToRemove = transferAmount;
			for (ItemStack is : sourceInventory.getContents()) {
				if (is != null
						&& is.getType() != Material.AIR
						&& (allowedMaterials == null || allowedMaterials
								.contains(is.getType()))) {
					int removeAmount = Math.min(leftToRemove, is.getAmount());
					ItemStack removing = is.clone();
					removing.setAmount(removeAmount);
					ItemMap removeMap = new ItemMap(removing);
					if (removeMap.fitsIn(targetInventory)) {
						sourceInventory.removeItem(removing);
						targetInventory.addItem(removing);
						leftToRemove -= removeAmount;
					} else {
						break;
					}
					if (leftToRemove <= 0) {
						break;
					}
				}
			}
		}
	}

	public void setRunTime(int time) {
		this.runTime = time;
	}

	public int getTransferTime() {
		return transferTimeMultiplier * ((PipeStructure) mbs).getLength();
	}

	public boolean transferMaterialsAvailable() {
		Block start = ((PipeStructure) mbs).getStart();
		if (start != null && start.getState() instanceof InventoryHolder) {
			Inventory i = ((InventoryHolder) start.getState()).getInventory();
			for (ItemStack is : i.getContents()) {
				if (is != null
						&& (allowedMaterials == null || allowedMaterials
								.contains(is.getType()))) {
					return true;
				}
			}
		}
		return false;
	}

	public void setAllowedMaterials(List<Material> mats) {
		allowedMaterials = mats;
	}

	public List<Material> getAllowedMaterials() {
		return allowedMaterials;
	}

	public void addAllowedMaterial(Material m) {
		if (allowedMaterials == null) {
			allowedMaterials = new LinkedList<Material>();
		}
		allowedMaterials.add(m);
	}

	public void removeAllowedMaterial(Material m) {
		allowedMaterials.remove(m);
		if (allowedMaterials.size() == 0) {
			allowedMaterials = null;
		}
	}

	public String serialize() {
		StringBuilder sb = new StringBuilder();
		sb.append("PIPE");
		sb.append(separator);
		sb.append(name);
		sb.append(separator);
		sb.append(runTime);
		if (allowedMaterials == null) {
			sb.append(separator);
			sb.append("NONE");
		} else {
			for (Material m : allowedMaterials) {
				sb.append(separator);
				sb.append(m.toString());
			}
		}
		sb.append(separator);
		sb.append("BLOCKS");
		for (Block b : mbs.getAllBlocks()) {
			sb.append(serializeBlock(b));
		}
		return sb.toString();
	}

}
