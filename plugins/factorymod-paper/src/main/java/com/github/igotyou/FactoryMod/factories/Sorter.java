package com.github.igotyou.FactoryMod.factories;

import com.github.igotyou.FactoryMod.events.FactoryActivateEvent;
import com.github.igotyou.FactoryMod.events.ItemTransferEvent;
import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.powerManager.IPowerManager;
import com.github.igotyou.FactoryMod.repairManager.IRepairManager;
import com.github.igotyou.FactoryMod.structures.BlockFurnaceStructure;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class Sorter extends Factory {
	private Map<BlockFace, ItemMap> assignedMaterials;
	private int runTime;
	private int matsPerSide;
	private int sortTime;
	private int sortAmount;

	public Sorter(IInteractionManager im, IRepairManager rm, IPowerManager pm, MultiBlockStructure mbs, int updateTime,
			String name, int sortTime, int matsPerSide, int sortAmount) {
		super(im, rm, pm, mbs, updateTime, name);
		assignedMaterials = new HashMap<BlockFace, ItemMap>();
		this.sortTime = sortTime;
		this.sortAmount = sortAmount;
		runTime = 0;
		this.matsPerSide = matsPerSide;
		for (BlockFace bf : WorldUtils.ALL_SIDES) {
			assignedMaterials.put(bf, new ItemMap());
		}
	}

	public void attemptToActivate(Player p, boolean onStartUp) {
		LoggingUtils.log((p != null ? p.getName() : "Redstone") + "is attempting to activate " + getLogData());
		mbs.recheckComplete();
		if (mbs.isComplete()) {
			if (pm.powerAvailable(1)) {
				if (sortableMaterialsAvailable()) {
					FactoryActivateEvent fae = new FactoryActivateEvent(this, p);
					Bukkit.getPluginManager().callEvent(fae);
					if (fae.isCancelled()) {
						LoggingUtils.log("Activating of " + getLogData() + " was cancelled by the event");
						return;
					}
					if (p != null) {
						p.sendMessage(ChatColor.GREEN + "Activated " + name);
					}
					activate();
				} else {
					if (p != null) {
						p.sendMessage(ChatColor.RED + "Nothing to sort available");
					}
				}
			} else {
				if (p != null) {
					p.sendMessage(ChatColor.RED + "No fuel available");
				}
			}
		} else {
			rm.breakIt();
		}
	}

	public void setAssignments(Map<BlockFace, ItemMap> assigns) {
		this.assignedMaterials = assigns;
	}

	public void activate() {
		LoggingUtils.log("Activating " + getLogData());
		LoggingUtils.logInventory(mbs.getCenter().getBlock());
		turnFurnaceOn(((BlockFurnaceStructure) mbs).getFurnace());
		active = true;
		run();
	}

	public void deactivate() {
		LoggingUtils.log("Deactivating " + getLogData());
		LoggingUtils.logInventory(mbs.getCenter().getBlock());
		Bukkit.getScheduler().cancelTask(threadId);
		turnFurnaceOff(((BlockFurnaceStructure) mbs).getFurnace());
		active = false;
	}

	public void run() {
		int powerCounter = pm.getPowerCounter() + updateTime;
		int fuelCount = powerCounter / pm.getPowerConsumptionIntervall();

		if (active && mbs.isComplete() && pm.powerAvailable(fuelCount) && sortableMaterialsAvailable()) {
			if (runTime >= sortTime) {
				mbs.recheckComplete();
				if (!mbs.isComplete()) {
					deactivate();
					return;
				}
				sortStack();
				runTime = 0;
				if (sortableMaterialsAvailable()) {
					scheduleUpdate();
				} else {
					deactivate();
				}
			} else {
				Block furnace = ((BlockFurnaceStructure) mbs).getFurnace();
				turnFurnaceOn(furnace);
				// if we need to consume fuel - then do it
				if (fuelCount >= 1) {
					// remove fuel.
					pm.consumePower(fuelCount);
					// update power counter to remained time
					pm.setPowerCounter(powerCounter % pm.getPowerConsumptionIntervall());
				} else {
					pm.increasePowerCounter(updateTime);
				}
				runTime += updateTime;
				scheduleUpdate();
			}
		} else {
			deactivate();
		}
	}

	public boolean assigned(ItemStack is) {
		return getSide(is) != null;
	}

	public BlockFace getSide(ItemStack is) {
		for (Entry<BlockFace, ItemMap> entry : assignedMaterials.entrySet()) {
			if (entry.getValue().getAmount(is) != 0) {
				return entry.getKey();
			}
		}
		return null;
	}

	public void addAssignment(BlockFace bf, ItemStack is) {
		assignedMaterials.get(bf).addItemStack(is.clone());
	}

	public ItemMap getItemsForSide(BlockFace face) {
		return assignedMaterials.get(face);
	}

	public void removeAssignment(ItemStack is) {
		for (Entry<BlockFace, ItemMap> entry : assignedMaterials.entrySet()) {
			if (entry.getValue().getAmount(is) != 0) {
				entry.getValue().removeItemStackCompletely(is);
				break;
			}
		}
	}

	public void sortStack() {
		LoggingUtils.log("Attempting to sort " + getLogData());
		Block center = mbs.getCenter().getBlock();
		Inventory inv = getCenterInventory();
		int leftToSort = sortAmount;
		for (BlockFace bf : WorldUtils.ALL_SIDES) {
			if (center.getRelative(bf).getState() instanceof InventoryHolder) {
				Block b = center.getRelative(bf);
				if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
					// load adjacent chunk for double chest
					MultiBlockStructure.getAdjacentBlocks(b);
				}
				Inventory relInv = ((InventoryHolder) center.getRelative(bf).getState()).getInventory();
				ItemMap im = assignedMaterials.get(bf);
				for (ItemStack is : inv.getContents()) {
					if (is != null && is.getType() != Material.AIR && im != null && im.getAmount(is) != 0) {
						int removeAmount = Math.min(leftToSort, is.getAmount());
						ItemStack rem = is.clone();
						rem.setAmount(removeAmount);
						if (new ItemMap(is).fitsIn(relInv)) {
							ItemTransferEvent ite = new ItemTransferEvent(this, inv, relInv, center,
									center.getRelative(bf), rem);
							Bukkit.getPluginManager().callEvent(ite);
							if (ite.isCancelled()) {
								LoggingUtils.log("Sorting for " + rem.toString() + " in " + getLogData()
										+ " was cancelled over the event");
								continue;
							}
							LoggingUtils.log("Moving " + rem.toString() + " from " + mbs.getCenter().toString() + " to "
									+ center.getRelative(bf).getLocation().toString());
							LoggingUtils.logInventory(inv, "Origin inventory before transfer for " + getLogData());
							LoggingUtils.logInventory(relInv, "Target inventory before transfer for " + getLogData());
							inv.removeItem(rem);
							relInv.addItem(rem);
							LoggingUtils.logInventory(inv, "Origin inventory after transfer for " + getLogData());
							LoggingUtils.logInventory(relInv, "Target inventory after transfer for " + getLogData());
							leftToSort -= removeAmount;
							break;
						}
					}
					if (leftToSort <= 0) {
						break;
					}
				}
			}
			if (leftToSort <= 0) {
				break;
			}
		}
	}

	public void setRunTime(int runtime) {
		this.runTime = runtime;
	}

	public int getRunTime() {
		return runTime;
	}

	public Inventory getCenterInventory() {
		return ((InventoryHolder) mbs.getCenter().getBlock().getState()).getInventory();
	}

	private boolean sortableMaterialsAvailable() {
		for (ItemStack is : getCenterInventory()) {
			if (is != null && is.getType() != Material.AIR) {
				for (Entry<BlockFace, ItemMap> entry : assignedMaterials.entrySet()) {
					if (mbs.getCenter().getBlock().getRelative(entry.getKey()).getState() instanceof InventoryHolder
							&& entry.getValue().getAmount(is) != 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public int getMatsPerSide() {
		return matsPerSide;
	}
}
