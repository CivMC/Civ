package com.github.igotyou.FactoryMod.factories;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.multiBlockStructures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.powerManager.FurnacePowerManager;
import com.github.igotyou.FactoryMod.powerManager.IPowerManager;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.RepairRecipe;
import com.github.igotyou.FactoryMod.recipes.Upgraderecipe;
import com.github.igotyou.FactoryMod.repairManager.IRepairManager;

/**
 * Represents a "classic" factory, which consists of a furnace as powersource, a
 * crafting table as main interaction element between the furnace and the chest,
 * which is used as inventory holder
 *
 */
public class FurnCraftChestFactory extends Factory {
	protected int currentProductionTimer = 0;
	protected List<IRecipe> recipes;
	protected IRecipe currentRecipe;

	public FurnCraftChestFactory(IInteractionManager im, IRepairManager rm,
			IPowerManager ipm, FurnCraftChestStructure mbs, int updateTime,
			String name, List<IRecipe> recipes) {
		super(im, rm, ipm, mbs, updateTime, name);
		this.active = false;
		this.recipes = recipes;
	}

	/**
	 * @return Inventory of the chest or null if there is no chest where one
	 *         should be
	 */
	public Inventory getInventory() {
		if (!(getChest().getType() == Material.CHEST)) {
			return null;
		}
		Chest chestBlock = (Chest) (getChest().getState());
		return chestBlock.getInventory();
	}

	/**
	 * @return Inventory of the furnace or null if there is no furnace where one
	 *         should be
	 */
	public FurnaceInventory getFurnaceInventory() {
		if (!(getFurnace().getType() == Material.FURNACE || getFurnace()
				.getType() == Material.BURNING_FURNACE)) {
			return null;
		}
		Furnace furnaceBlock = (Furnace) (getFurnace().getState());
		return furnaceBlock.getInventory();
	}

	/**
	 * Attempts to turn the factory on and does all the checks needed to ensure
	 * that the factory is allowed to turn on
	 */
	public void attemptToActivate(Player p) {
		// TODO Citadel stuff
		if (mbs.isComplete()) {
			if (hasInputMaterials()) {
				if (pm.powerAvailable()) {
					if (rm.inDisrepair()
							&& !(currentRecipe instanceof RepairRecipe)) {
						if (p != null) {
							p.sendMessage(ChatColor.RED
									+ "This factory is in disrepair, you have to repair it before using it");
						}
						return;
					}
					if (currentRecipe instanceof RepairRecipe
							&& rm.atFullHealth()) {
						if (p != null) {
							p.sendMessage("This factory is already at full health!");
							return;
						}
					}
					if (p != null) {
						p.sendMessage(ChatColor.GREEN + "Activated " + name
								+ " with recipe: "
								+ currentRecipe.getRecipeName());
					}
					activate();
					run();
				} else {
					if (p != null) {
						p.sendMessage(ChatColor.RED
								+ "Failed to activate factory, there is no fuel in the furnace");
					}
				}
			} else {
				if (p != null) {
					p.sendMessage(ChatColor.RED
							+ "Not enough materials available");
				}
			}
		}
	}

	/**
	 * Actually turns the factory on, never use this directly unless you know
	 * what you are doing, use attemptToActivate() instead to ensure the factory
	 * is allowed to turn on
	 */
	public void activate() {
		// lots of code to make the furnace light up, without loosing contents.
		active = true;
		pm.setPowerCounter(0);
		Furnace furnace = (Furnace) (getFurnace().getState());
		byte data = furnace.getData().getData();
		ItemStack[] oldContents = furnace.getInventory().getContents();
		furnace.getInventory().clear();
		getFurnace().setType(Material.BURNING_FURNACE);
		furnace = (Furnace) (getFurnace().getState());
		furnace.setRawData(data);
		furnace.update();
		furnace.setBurnTime(Short.MAX_VALUE);
		furnace.getInventory().setContents(oldContents);
		// reset the production timer
		currentProductionTimer = 0;
	}

	/**
	 * Turns the factory off.
	 */
	public void deactivate() {
		if (active) {
			// lots of code to make the furnace turn off, without loosing
			// contents.
			Furnace furnace = (Furnace) (getFurnace().getState());
			byte data = furnace.getData().getData();
			ItemStack[] oldContents = furnace.getInventory().getContents();
			furnace.getInventory().clear();
			getFurnace().setType(Material.FURNACE);
			furnace = (Furnace) getFurnace().getState();
			furnace.setRawData(data);
			furnace.update();
			furnace.getInventory().setContents(oldContents);

			// put active to false
			active = false;
			// reset the production timer
			currentProductionTimer = 0;
		}
	}

	/**
	 * @return The furnace of this factory
	 */
	public Block getFurnace() {
		return ((FurnCraftChestStructure) mbs).getFurnace();
	}

	/**
	 * @return The chest of this factory
	 */
	public Block getChest() {
		return ((FurnCraftChestStructure) mbs).getChest();
	}

	/**
	 * @return How long the factory has been running in ticks
	 */
	public int getRunningTime() {
		return currentProductionTimer;
	}

	/**
	 * Called by the manager each update cycle
	 */
	public void run() {
		if (active && mbs.isComplete()) {
			// if the materials required to produce the current recipe are in
			// the factory inventory
			if (hasInputMaterials()) {
				// if the factory has been working for less than the required
				// time for the recipe
				if (currentProductionTimer < currentRecipe.getProductionTime()) {
					// if the factory power source inventory has enough fuel for
					// at least 1 energyCycle
					if (pm.powerAvailable()) {
						// if the time since fuel was last consumed is equal to
						// how often fuel needs to be consumed
						if (pm.getPowerCounter() >= pm
								.getPowerConsumptionIntervall() - 1) {
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
						currentProductionTimer += updateTime;
						// schedule next update
						FactoryMod
								.getPlugin()
								.getServer()
								.getScheduler()
								.scheduleSyncDelayedTask(
										FactoryMod.getPlugin(), this,
										(long) updateTime);
					}
					// if there is no fuel Available turn off the factory
					else {
						deactivate();
					}
				}

				// if the production timer has reached the recipes production
				// time remove input from chest, and add output material
				else if (currentProductionTimer >= currentRecipe
						.getProductionTime()) {
					if (currentRecipe instanceof Upgraderecipe) {
						// this if else might look a bit weird, but because
						// upgrading changes the current recipe and a lot of
						// other stuff, this is needed
						currentRecipe.applyEffect(getInventory(), this);
						deactivate();
						return;
					} else {
						currentRecipe.applyEffect(getInventory(), this);
					}
					currentProductionTimer = 0;
					if (hasInputMaterials() && pm.powerAvailable()) {
						pm.setPowerCounter(0);
						FactoryMod
								.getPlugin()
								.getServer()
								.getScheduler()
								.scheduleSyncDelayedTask(
										FactoryMod.getPlugin(), this,
										(long) updateTime);
						// keep going
					} else {
						deactivate();
					}
				}
			} else {
				deactivate();
			}
		}
	}

	/**
	 * @return All the recipes which are available for this instance
	 */
	public List<IRecipe> getRecipes() {
		return recipes;
	}

	/**
	 * @return The recipe currently selected in this instance
	 */
	public IRecipe getCurrentRecipe() {
		return currentRecipe;
	}

	/**
	 * Changes the current recipe for this factory to the given one
	 * 
	 * @param pr
	 *            Recipe to switch to
	 */
	public void setRecipe(IRecipe pr) {
		if (recipes.contains(pr)) {
			currentRecipe = pr;
		}
	}

	/**
	 * Sets the internal production timer
	 * 
	 * @param timer
	 *            New timer
	 */
	public void setProductionTimer(int timer) {
		this.currentProductionTimer = timer;
	}

	/**
	 * @return Whether enough materials are available to run the currently
	 *         selected recipe at least once
	 */
	public boolean hasInputMaterials() {
		return currentRecipe.enoughMaterialAvailable(getInventory());
	}

	public void upgrade(String name, List<IRecipe> recipes, ItemStack fuel,
			int fuelConsumptionIntervall, int updateTime) {
		this.name = name;
		this.recipes = recipes;
		this.updateTime = updateTime;
		this.pm = new FurnacePowerManager(this, fuel, fuelConsumptionIntervall);
		this.rm.repair(100);
		if (recipes.size() != 0) {
			setRecipe(recipes.get(0));
		} else {
			currentRecipe = null;
		}
	}

	public String serialize() {
		StringBuilder sb = new StringBuilder();
		String separator = "#";
		sb.append("FCC");
		sb.append(separator);
		sb.append(getName());
		sb.append(separator);
		sb.append(rm.getRawHealth());
		sb.append(separator);
		sb.append(currentProductionTimer);
		sb.append(separator);
		sb.append(currentRecipe.getRecipeName());
		for (Block b : mbs.getAllBlocks()) {
			sb.append(separator);
			sb.append(b.getWorld().getName());
			sb.append(separator);
			sb.append(b.getX());
			sb.append(separator);
			sb.append(b.getY());
			sb.append(separator);
			sb.append(b.getZ());
		}
		return sb.toString();
	}

}
