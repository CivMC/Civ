package com.github.igotyou.FactoryMod.factories;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.events.FactoryActivateEvent;
import com.github.igotyou.FactoryMod.events.ItemTransferEvent;
import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.powerManager.IPowerManager;
import com.github.igotyou.FactoryMod.repairManager.IRepairManager;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.structures.PipeStructure;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;

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
		LoggingUtils.log((p != null ? p.getName() : "Redstone")
				+ "is attempting to activate " + getLogData());
		mbs.recheckComplete();
		if (mbs.isComplete()) {
			if (transferMaterialsAvailable()) {
				if (pm.powerAvailable()) {
					FactoryActivateEvent fae = new FactoryActivateEvent(this, p);
					Bukkit.getPluginManager().callEvent(fae);
					if (fae.isCancelled()) {
						LoggingUtils.log("Activating for " + getLogData()
								+ " was cancelled by the event");
						return;
					}
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
		} else {
			rm.breakIt();
			p.sendMessage(ChatColor.RED
					+ "Failed to activate pipe, it is missing blocks");
		}
	}

	public void activate() {
		LoggingUtils.log("Activating " + getLogData());
		active = true;
		pm.setPowerCounter(0);
		turnFurnaceOn(((PipeStructure) mbs).getFurnace());
		// reset the production timer
		runTime = 0;
		run();
	}

	public void deactivate() {
		LoggingUtils.log("Deactivating " + getLogData());
		active = false;
		Bukkit.getScheduler().cancelTask(threadId);
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
				Block furnace = ((PipeStructure) mbs).getFurnace();
				if (furnace.getType() != Material.BURNING_FURNACE) {
					turnFurnaceOn(furnace);
				}
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
		LoggingUtils.log("Attempting to transfer for " + getLogData());
		mbs.recheckComplete();
		if (mbs.isComplete()) {
			Inventory sourceInventory = ((InventoryHolder) (((PipeStructure) mbs)
					.getStart().getState())).getInventory();
			Inventory targetInventory = ((InventoryHolder) (((PipeStructure) mbs)
					.getEnd().getState())).getInventory();
			int leftToRemove = transferAmount;
			for (ItemStack is : sourceInventory.getContents()) {
				if (is != null
						&& is.getType() != Material.AIR
						&& is.getAmount() != 0
						&& (allowedMaterials == null || allowedMaterials
								.contains(is.getType()))) {
					int removeAmount = Math.min(leftToRemove, is.getAmount());
					ItemStack removing = is.clone();
					removing.setAmount(removeAmount);
					ItemMap removeMap = new ItemMap(removing);
					if (removeMap.fitsIn(targetInventory)) {
						ItemTransferEvent ite = new ItemTransferEvent(this,
								sourceInventory, targetInventory,
								((PipeStructure) mbs).getStart(),
								((PipeStructure) mbs).getEnd(), removing);
						Bukkit.getPluginManager().callEvent(ite);
						if (ite.isCancelled()) {
							LoggingUtils.log("Transfer for " + removing.toString() + " was cancelled over the event");
							continue;
						}
						LoggingUtils.logInventory(sourceInventory,
								"Origin inventory before transfer for "
										+ getLogData());
						LoggingUtils.logInventory(targetInventory,
								"Target inventory before transfer for "
										+ getLogData());
						if (removeMap.removeSafelyFrom(sourceInventory)) {
							targetInventory.addItem(removing);
						}
						LoggingUtils.logInventory(sourceInventory,
								"Origin inventory after transfer for "
										+ getLogData());
						LoggingUtils.logInventory(targetInventory,
								"Target inventory after transfer for "
										+ getLogData());
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

	public int getRunTime() {
		return runTime;
	}
}
