package com.github.igotyou.FactoryMod.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.powerManager.IPowerManager;
import com.github.igotyou.FactoryMod.repairManager.IRepairManager;
import com.github.igotyou.FactoryMod.structures.BlockFurnaceStructure;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.utility.ItemMap;

public class Sorter extends Factory {
	private Map<BlockFace, ItemMap> assignedMaterials;
	private int runTime;
	private int matsPerSide;
	private int sortTime;
	private int sortAmount;

	public Sorter(IInteractionManager im, IRepairManager rm, IPowerManager pm,
			MultiBlockStructure mbs, int updateTime, String name, int sortTime,
			int matsPerSide, int sortAmount) {
		super(im, rm, pm, mbs, updateTime, name);
		assignedMaterials = new HashMap<BlockFace, ItemMap>();
		this.sortTime = sortTime;
		this.sortAmount = sortAmount;
		runTime = 0;
		this.matsPerSide = matsPerSide;
		for (BlockFace bf : MultiBlockStructure.allBlockSides) {
			assignedMaterials.put(bf, new ItemMap());
		}
		assignedMaterials.remove(mbs.getCenter().getBlock().getFace(((BlockFurnaceStructure)mbs).getFurnace()));
	}

	public void attemptToActivate(Player p) {
		mbs.recheckComplete();
		if (mbs.isComplete()) {
			activate();
			if (pm.powerAvailable()) {
				if (sortableMaterialsAvailable()) {
					activate();
				} else {
					if (p != null) {
						p.sendMessage(ChatColor.RED
								+ "Nothing to sort available");
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
		turnFurnaceOn(((BlockFurnaceStructure) mbs).getFurnace());
		active = true;
		run();
	}

	public void deactivate() {
		turnFurnaceOff(((BlockFurnaceStructure) mbs).getFurnace());
		active = false;
	}

	public void run() {
		if (active && mbs.isComplete() && pm.powerAvailable()
				&& sortableMaterialsAvailable()) {
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
				if (pm.getPowerCounter() >= pm.getPowerConsumptionIntervall() - 1) {
					pm.consumePower();
					pm.setPowerCounter(0);

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
				entry.getValue().removeItemStackCompletly(is);
				break;
			}
		}
	}

	public void sortStack() {
		Block center = mbs.getCenter().getBlock();
		Inventory inv = getCenterInventory();
		int leftToSort = sortAmount;
		for (BlockFace bf : MultiBlockStructure.allBlockSides) {
			if (center.getRelative(bf).getState() instanceof InventoryHolder) {
				Inventory relInv = ((InventoryHolder) center.getRelative(bf)
						.getState()).getInventory();
				ItemMap im = assignedMaterials.get(bf);
				for (ItemStack is : inv.getContents()) {
					if (is != null && is.getType() != Material.AIR
							&& im.getAmount(is) != 0) {
						int removeAmount = Math.min(leftToSort, is.getAmount());
						ItemStack rem = is.clone();
						rem.setAmount(removeAmount);
						if (new ItemMap(is).fitsIn(relInv)) {
							inv.removeItem(rem);
							relInv.addItem(rem);
							leftToSort -=removeAmount;
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

	public Inventory getCenterInventory() {
		return ((InventoryHolder) mbs.getCenter().getBlock().getState())
				.getInventory();
	}

	private boolean sortableMaterialsAvailable() {
		for (ItemStack is : getCenterInventory()) {
			if (is != null && is.getType() != Material.AIR) {
				for (Entry<BlockFace, ItemMap> entry : assignedMaterials
						.entrySet()) {
					if (mbs.getCenter().getBlock().getRelative(entry.getKey())
							.getState() instanceof InventoryHolder
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

	public String serialize() {
		StringBuilder sb = new StringBuilder();
		sb.append("SORTER");
		sb.append(separator);
		sb.append(name);
		sb.append(separator);
		sb.append(runTime);
		for (Entry<BlockFace, ItemMap> entry : assignedMaterials.entrySet()) {
			sb.append(separator);
			sb.append(entry.getKey().toString());
			for (ItemStack is : entry.getValue().getItemStackRepresentation()) {
				sb.append(separator);
				sb.append(is.getType().toString());
				sb.append(separator);
				sb.append(is.getDurability());
			}
			sb.append(separator);
			sb.append("STOP");
		}
		for (Block b : mbs.getAllBlocks()) {
			sb.append(serializeBlock(b));
		}
		return sb.toString();
	}

}
