package com.github.igotyou.FactoryMod.factories;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.events.FactoryActivateEvent;
import com.github.igotyou.FactoryMod.events.RecipeExecuteEvent;
import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.powerManager.FurnacePowerManager;
import com.github.igotyou.FactoryMod.powerManager.IPowerManager;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.recipes.PylonRecipe;
import com.github.igotyou.FactoryMod.recipes.RecipeScalingUpgradeRecipe;
import com.github.igotyou.FactoryMod.recipes.RepairRecipe;
import com.github.igotyou.FactoryMod.recipes.Upgraderecipe;
import com.github.igotyou.FactoryMod.repairManager.IRepairManager;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;

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
	protected Map<IRecipe, Integer> runCount;
	protected Map<IRecipe, Integer> recipeLevel;
	private UUID activator;
	private double citadelBreakReduction;

	private static HashSet<FurnCraftChestFactory> pylonFactories;

	public FurnCraftChestFactory(IInteractionManager im, IRepairManager rm,
			IPowerManager ipm, FurnCraftChestStructure mbs, int updateTime,
			String name, List<IRecipe> recipes, double citadelBreakReduction) {
		super(im, rm, ipm, mbs, updateTime, name);
		this.active = false;
		this.runCount = new HashMap<IRecipe, Integer>();
		this.recipeLevel = new HashMap<IRecipe, Integer>();
		this.citadelBreakReduction = citadelBreakReduction;
		for (IRecipe rec : recipes) {
			addRecipe(rec);
		}
		if (pylonFactories == null) {
			pylonFactories = new HashSet<FurnCraftChestFactory>();
		}
		for (IRecipe rec : recipes) {
			if (rec instanceof PylonRecipe) {
				pylonFactories.add(this);
				break;
			}
		}
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
	public void attemptToActivate(Player p, boolean onStartUp) {
		LoggingUtils.log((p != null ? p.getName() : "Redstone")
				+ " is attempting to activate " + getLogData());
		mbs.recheckComplete();
 
		if (active) {
			return;
		}
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
						}
						return;
					}
					if (!onStartUp && currentRecipe instanceof Upgraderecipe
							&& FactoryMod.getManager().isCitadelEnabled()) {
						// only allow permitted members to upgrade the factory
						ReinforcementManager rm = Citadel
								.getReinforcementManager();
						PlayerReinforcement rein = (PlayerReinforcement) rm
								.getReinforcement(mbs.getCenter());
						if (rein != null) {
							if (p == null) {
								return;
							}
							if (!NameAPI.getGroupManager().hasAccess(rein.getGroup().getName(),
									p.getUniqueId(), PermissionType.getPermission("UPGRADE_FACTORY"))) {
								p.sendMessage(ChatColor.RED + "You dont have permission to upgrade this factory");
								return;
							}
						}
					}
					FactoryActivateEvent fae = new FactoryActivateEvent(this, p);
					Bukkit.getPluginManager().callEvent(fae);
					if (fae.isCancelled()) {
						return;
					}
					if (p != null) {
						int consumptionIntervall = ((InputRecipe) currentRecipe)
								.getFuelConsumptionIntervall() != -1 ? ((InputRecipe) currentRecipe)
								.getFuelConsumptionIntervall() : pm
								.getPowerConsumptionIntervall();
						if (((FurnacePowerManager) pm).getFuelAmountAvailable() < (currentRecipe
								.getProductionTime() / consumptionIntervall)) {
							p.sendMessage(ChatColor.RED
									+ "You don't have enough fuel, the factory will run out of it before completing");
						}
						p.sendMessage(ChatColor.GREEN + "Activated " + name
								+ " with recipe: "
								+ currentRecipe.getName());
						activator = p.getUniqueId();
					}
					activate();
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
		} else {
			rm.breakIt();
		}
	}

	/**
	 * Actually turns the factory on, never use this directly unless you know
	 * what you are doing, use attemptToActivate() instead to ensure the factory
	 * is allowed to turn on
	 */
	public void activate() {
		LoggingUtils.log("Activating " + getLogData());
		active = true;
		pm.setPowerCounter(0);
		turnFurnaceOn(getFurnace());
		// reset the production timer
		currentProductionTimer = 0;
		run();
	}

	/**
	 * Turns the factory off.
	 */
	public void deactivate() {
		LoggingUtils.log("Deactivating " + getLogData());
		if (active) {
			Bukkit.getScheduler().cancelTask(threadId);
			turnFurnaceOff(getFurnace());
			active = false;
			// reset the production timer
			currentProductionTimer = 0;
			activator = null;
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

	public void setRunCount(IRecipe r, Integer count) {
		if (recipes.contains(r)) {
			runCount.put(r, count);
		}
	}
	
	public void setRecipeLevel(IRecipe r, Integer level) {
		if (recipes.contains(r)) {
			recipeLevel.put(r, level);
		}
	}

	/**
	 * @return UUID of the person who activated the factory or null if the
	 *         factory is off or was triggered by redstone
	 */
	public UUID getActivator() {
		return activator;
	}

	public void setActivator(UUID uuid) {
		this.activator = uuid;
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
						// check whether the furnace is on, minecraft sometimes
						// turns it off
						if (getFurnace().getType() != Material.BURNING_FURNACE) {
							turnFurnaceOn(getFurnace());
						}
						// if the time since fuel was last consumed is equal to
						// how often fuel needs to be consumed
						int consumptionIntervall = ((InputRecipe) currentRecipe)
								.getFuelConsumptionIntervall() != -1 ? ((InputRecipe) currentRecipe)
								.getFuelConsumptionIntervall() : pm
								.getPowerConsumptionIntervall();
						if (pm.getPowerCounter() >= consumptionIntervall - 1) {
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
						scheduleUpdate();
					}
					// if there is no fuel Available turn off the factory
					else {
						sendActivatorMessage(ChatColor.GOLD + name
								+ " deactivated, because it ran out of fuel");
						deactivate();
					}
				}

				// if the production timer has reached the recipes production
				// time remove input from chest, and add output material
				else if (currentProductionTimer >= currentRecipe
						.getProductionTime()) {
					LoggingUtils.log("Executing recipe "
							+ currentRecipe.getName() + " for "
							+ getLogData());
					RecipeExecuteEvent ree = new RecipeExecuteEvent(this,
							(InputRecipe) currentRecipe);
					Bukkit.getPluginManager().callEvent(ree);
					if (ree.isCancelled()) {
						LoggingUtils.log("Executing recipe "
								+ currentRecipe.getName() + " for "
								+ getLogData()
								+ " was cancelled over the event");
						deactivate();
						return;
					}
					sendActivatorMessage(ChatColor.GOLD
							+ currentRecipe.getName() + " in " + name
							+ " completed");
					if (currentRecipe instanceof Upgraderecipe || currentRecipe instanceof RecipeScalingUpgradeRecipe) {
						// this if else might look a bit weird, but because
						// upgrading changes the current recipe and a lot of
						// other stuff, this is needed
						currentRecipe.applyEffect(getInventory(), this);
						deactivate();
						return;
					} else {
						currentRecipe.applyEffect(getInventory(), this);
						runCount.put(currentRecipe,
								runCount.get(currentRecipe) + 1);
					}
					currentProductionTimer = 0;
					if (currentRecipe instanceof RepairRecipe
							&& rm.atFullHealth()) {
						// already at full health, dont try to repair further
						sendActivatorMessage(ChatColor.GOLD + name
								+ " repaired to full health");
						deactivate();
						return;
					}
					if (hasInputMaterials() && pm.powerAvailable()) {
						pm.setPowerCounter(0);
						scheduleUpdate();
						// keep going
					} else {
						deactivate();
					}
				}
			} else {
				sendActivatorMessage(ChatColor.GOLD
						+ name
						+ " deactivated, because it ran out of required materials");
				deactivate();
			}
		} else {
			sendActivatorMessage(ChatColor.GOLD + name
					+ " deactivated, because the factory was destroyed");
			deactivate();
		}
	}

	/**
	 * @return All the recipes which are available for this instance
	 */
	public List<IRecipe> getRecipes() {
		return recipes;
	}

	/**
	 * Pylon recipes have a special functionality, which requires them to know
	 * all other factories with pylon recipes on the map. Because of that all of
	 * those factories are kept in a separated hashset, which is provided by
	 * this method
	 * 
	 * @return All factories with a pylon recipe
	 */
	public static HashSet<FurnCraftChestFactory> getPylonFactories() {
		return pylonFactories;
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

	public int getRunCount(IRecipe r) {
		return runCount.get(r);
	}
	
	public int getRecipeLevel(IRecipe r) {
		return recipeLevel.get(r);
	}

	private void sendActivatorMessage(String msg) {
		if (activator != null) {
			Player p = Bukkit.getPlayer(activator);
			if (p != null) {
				p.sendMessage(msg);
			}
		}
	}
	
	/**
	 * Adds the given recipe to this factory
	 * @param rec Recipe to add
	 */
	public void addRecipe(IRecipe rec) {
		recipes.add(rec);
		runCount.put(rec, 0);
		recipeLevel.put(rec, 1);
	}
	
	/**
	 * Removes the given recipe from this factory
	 * @param rec Recipe to remove
	 */
	public void removeRecipe(IRecipe rec) {
		recipes.remove(rec);
		runCount.remove(rec);
		recipeLevel.remove(rec);
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

	public static void removePylon(Factory f) {
		pylonFactories.remove(f);
	}

	public void upgrade(String name, List<IRecipe> recipes, ItemStack fuel,
			int fuelConsumptionIntervall, int updateTime, int maximumHealth, int damageAmountPerDecayIntervall, long gracePeriod, double citadelBreakReduction) {
		LoggingUtils.log("Upgrading " + getLogData() + " to " + name);
		pylonFactories.remove(this);
		deactivate();
		this.name = name;
		this.recipes = recipes;
		this.updateTime = updateTime;
		this.citadelBreakReduction = citadelBreakReduction;
		this.pm = new FurnacePowerManager(getFurnace(), fuel,
				fuelConsumptionIntervall);
		this.rm = new PercentageHealthRepairManager(maximumHealth, maximumHealth, 0, damageAmountPerDecayIntervall, gracePeriod);
		if (recipes.size() != 0) {
			setRecipe(recipes.get(0));
		} else {
			currentRecipe = null;
		}
		runCount = new HashMap<IRecipe, Integer>();
		for (IRecipe rec : recipes) {
			runCount.put(rec, 0);
		}
		for (IRecipe rec : recipes) {
			if (rec instanceof PylonRecipe) {
				pylonFactories.add(this);
				break;
			}
		}
	}
	
	public double getCitadelBreakReduction() {
		return citadelBreakReduction;
	}
}
