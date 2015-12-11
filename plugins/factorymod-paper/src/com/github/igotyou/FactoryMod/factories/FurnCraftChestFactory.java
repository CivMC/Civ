package com.github.igotyou.FactoryMod.factories;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.Factory;
import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.multiBlockStructures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.powerManager.IPowerManager;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.RepairRecipe;
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
		if (!(getFurnace().getType() == Material.CHEST)) {
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

	public void attemptToActivate(Player p) {
		// TODO Citadel stuff
		if (mbs.isComplete()) {
			if (hasInputMaterials()) {
				if (pm.powerAvailable()) {
					if (rm.inDisrepair()
							&& !(currentRecipe instanceof RepairRecipe)) {
						if (p != null) {
							p.sendMessage("This factory is in disrepair, you have to repair it before using it");
						}
						return;
					}
					if (p != null) {
						p.sendMessage("Activated " + name + " with recipe: "
								+ currentRecipe.getRecipeName());
					}
					activate();
					run();
				} else {
					if (p != null) {
						p.sendMessage("Failed to activate factory, there is no fuel in the furnace");
					}
				}
			}
			else {
				p.sendMessage("Not enough materials available"); //TODO how much is needed
			}
		}
	}

	/**
	 * Turns the factory on
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

	public Block getFurnace() {
		return ((FurnCraftChestStructure) mbs).getFurnace();
	}

	public Block getChest() {
		return ((FurnCraftChestStructure) mbs).getChest();
	}

	/**
	 * @return Whether the factory is currently on or not
	 */
	public boolean getActive() {
		return active;
	}

	/**
	 * @return How long the factory has been running in ticks
	 */
	public int getRunningTime() {
		return currentProductionTimer;
	}

	/**
	 * called by the manager each update cycle
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
						if (pm.getPowerCounter() == pm
								.getPowerConsumptionIntervall() - 1) {
							// remove one fuel.
							pm.consumePower();
							// 0 seconds since last fuel consumption
							pm.setPowerCounter(0);
						}
						// if we don't need to consume fuel, just increment the
						// energy timer
						else {
							pm.increasePowerCounter(updateTime);
						}
						// increment the production timer
						currentProductionTimer += updateTime;
						// schedule next update
						FactoryModPlugin
								.getPlugin()
								.getServer()
								.getScheduler()
								.scheduleSyncDelayedTask(
										FactoryModPlugin.getPlugin(), this,
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
					applyRecipeEffect();
					currentProductionTimer = 0;
					if (hasInputMaterials() && pm.powerAvailable()) {
						pm.setPowerCounter(0);
						FactoryModPlugin
								.getPlugin()
								.getServer()
								.getScheduler()
								.scheduleSyncDelayedTask(
										FactoryModPlugin.getPlugin(), this,
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

	public List<IRecipe> getRecipes() {
		return recipes;
	}

	public IRecipe getCurrentRecipe() {
		return currentRecipe;
	}

	public void setRecipe(IRecipe pr) {
		if (recipes.contains(pr)) {
			currentRecipe = pr;
		}
	}

	public boolean hasInputMaterials() {
		return currentRecipe.enoughMaterialAvailable(getInventory());
	}

	public void applyRecipeEffect() {
		currentRecipe.applyEffect(getInventory());
	}

}
