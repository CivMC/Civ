package com.github.igotyou.FactoryMod.recipes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import com.github.igotyou.FactoryMod.utility.ItemStackUtils;

public class AOERepairRecipe extends InputRecipe {
	private ItemStack essence;
	private int repairPerEssence;
	private int range;

	public AOERepairRecipe(String name, int productionTime, ItemStack essence,
			int range, int repairPerEssence) {
		super(name, productionTime, new ItemMap(essence));
		this.essence = essence;
		this.range = range;
		this.repairPerEssence = repairPerEssence;
	}

	public ItemStack getRecipeRepresentation() {
		return essence;
	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		Chest c = (Chest) i.getHolder();
		Location loc = c.getLocation();
		List<FurnCraftChestFactory> facs = getNearbyFactoriesSortedByDistance(loc);
		int facCounter = 0;
		int essenceCount = new ItemMap(i).getAmount(essence);
		for (FurnCraftChestFactory fac : facs) {
			PercentageHealthRepairManager rm = (PercentageHealthRepairManager) fac
					.getRepairManager();
			int diff = 100 - rm.getRawHealth();
			if (diff >= repairPerEssence) {
				essenceCount -= Math.min(essenceCount, diff / repairPerEssence);
				facCounter++;
			}
			if (essenceCount <= 0) {
				break;
			}
		}
		ItemMap imp = new ItemMap();
		imp.addItemAmount(essence, new ItemMap(i).getAmount(essence)
				- essenceCount);
		List<ItemStack> bla = imp.getItemStackRepresentation();
		for (ItemStack item : bla) {
			item.setAmount(new ItemMap(i).getAmount(essence) - essenceCount);
			ItemStackUtils.addLore(item, ChatColor.YELLOW + "Will repair "
					+ facCounter + " nearby factories total");
		}
		return bla;
	}

	private List<FurnCraftChestFactory> getNearbyFactoriesSortedByDistance(
			Location loc) {
		LinkedList<FurnCraftChestFactory> list = new LinkedList<FurnCraftChestFactory>();
		Map<FurnCraftChestFactory, Double> distances = new HashMap<FurnCraftChestFactory, Double>();
		for (Factory f : FactoryMod.getManager().getNearbyFactories(loc, range)) {
			if (f instanceof FurnCraftChestFactory) {
				double dist = f.getMultiBlockStructure().getCenter()
						.distance(loc);
				distances.put((FurnCraftChestFactory) f, dist);
				if (list.size() == 0) {
					list.add((FurnCraftChestFactory) f);
				} else {
					for (int j = 0; j < list.size(); j++) {
						if (distances.get(list.get(j)) > dist) {
							list.add(j, (FurnCraftChestFactory) f);
							break;
						}
						if (j == list.size() - 1) {
							list.add(j, (FurnCraftChestFactory) f);
							break;
						}
					}
				}
			}
		}
		return list;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		Chest c = (Chest) i.getHolder();
		Location loc = c.getLocation();
		List<FurnCraftChestFactory> facs = getNearbyFactoriesSortedByDistance(loc);
		ItemStack is = new ItemStack(Material.WORKBENCH);
		int essenceCount = new ItemMap(i).getAmount(essence);
		for (FurnCraftChestFactory fac : facs) {
			PercentageHealthRepairManager rm = (PercentageHealthRepairManager) fac
					.getRepairManager();
			int diff = 100 - rm.getRawHealth();
			if (diff >= repairPerEssence) {
				ItemStackUtils
						.addLore(
								is,
								ChatColor.LIGHT_PURPLE
										+ "Will repair "
										+ fac.getName()
										+ " to "
										+ Math.min(
												100,
												rm.getRawHealth()
														+ (repairPerEssence * Math
																.min(essenceCount,
																		diff
																				/ repairPerEssence))));
				essenceCount -= Math.min(essenceCount, diff / repairPerEssence);
			}
			if (essenceCount <= 0) {
				break;
			}
		}
		List<ItemStack> bla = new LinkedList<ItemStack>();
		bla.add(is);
		return bla;
	}

	public void applyEffect(Inventory i, Factory f) {
		Chest c = (Chest) i.getHolder();
		Location loc = c.getLocation();
		List<FurnCraftChestFactory> facs = getNearbyFactoriesSortedByDistance(loc);
		ItemStack is = new ItemStack(Material.WORKBENCH);
		int essenceCount = new ItemMap(i).getAmount(essence);
		for (FurnCraftChestFactory fac : facs) {
			PercentageHealthRepairManager rm = (PercentageHealthRepairManager) fac
					.getRepairManager();
			int diff = 100 - rm.getRawHealth();
			fac.getMultiBlockStructure().recheckComplete();
			if (diff >= repairPerEssence
					&& fac.getMultiBlockStructure().isComplete()
					&& !fac.isActive()
					&& fac.getPowerManager().powerAvailable()) {
				int rem = Math.min(essenceCount, diff / repairPerEssence);
				ItemStack remStack = essence.clone();
				remStack.setAmount(rem);
				Inventory targetInv = ((InventoryHolder) (fac.getChest()
						.getState())).getInventory();
				if (new ItemMap(remStack).fitsIn(targetInv)) {
					i.removeItem(remStack);
					targetInv.addItem(remStack);
					for (IRecipe rec : fac.getRecipes()) {
						if (rec instanceof RepairRecipe) {
							fac.setRecipe(rec);
							break;
						}
					}
					fac.attemptToActivate(null);
					break;
				}
			}
			if (essenceCount <= 0) {
				break;
			}
		}
	}

}
