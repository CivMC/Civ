package com.github.igotyou.FactoryMod.factories;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.events.FactoryActivateEvent;
import com.github.igotyou.FactoryMod.events.RecipeExecuteEvent;
import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.powerManager.FurnacePowerManager;
import com.github.igotyou.FactoryMod.utility.Direction;
import com.github.igotyou.FactoryMod.utility.IIOFInventoryProvider;
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
import com.github.igotyou.FactoryMod.utility.IOSelector;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

/**
 * Represents a "classic" factory, which consists of a furnace as powersource, a
 * crafting table as main interaction element between the furnace and the chest,
 * which is used as inventory holder
 *
 */
public class FurnCraftChestFactory extends Factory implements IIOFInventoryProvider {
	protected int currentProductionTimer = 0;
	protected List<IRecipe> recipes;
	protected IRecipe currentRecipe;
	protected Map<IRecipe, Integer> runCount;
	protected Map<IRecipe, Integer> recipeLevel;
	private UUID activator;
	private double citadelBreakReduction;
	private boolean autoSelect;
	private @Nullable IOSelector furnaceIoSelector;
	private @Nullable IOSelector tableIoSelector;
	private UiMenuMode uiMenuMode;

	private static HashSet<FurnCraftChestFactory> pylonFactories;

	public FurnCraftChestFactory(IInteractionManager im, IRepairManager rm, IPowerManager ipm,
			FurnCraftChestStructure mbs, int updateTime, String name, List<IRecipe> recipes,
			double citadelBreakReduction) {
		super(im, rm, ipm, mbs, updateTime, name);
		if (ipm instanceof FurnacePowerManager) {
			((FurnacePowerManager) ipm).setIofProvider(this);
		}
		this.active = false;
		this.runCount = new HashMap<>();
		this.recipeLevel = new HashMap<>();
		this.recipes = new ArrayList<>();
		this.citadelBreakReduction = citadelBreakReduction;
		this.autoSelect = false;
		this.uiMenuMode = UiMenuMode.SIMPLE;
		for (IRecipe rec : recipes) {
			addRecipe(rec);
		}
		if (pylonFactories == null) {
			pylonFactories = new HashSet<>();
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
		if (getChest().getType() != Material.CHEST && getChest().getType() != Material.TRAPPED_CHEST) {
			return null;
		}
		Chest chestBlock = (Chest) (getChest().getState());
		return chestBlock.getInventory();
	}

	public Inventory getInputInventory() {
		List<Inventory> invs = new ArrayList<>(12);
		getInventoriesForIoType(invs, iosel -> iosel::getInputs);
		return new MultiInventoryWrapper(invs);
	}

	public Inventory getOutputInventory() {
		List<Inventory> invs = new ArrayList<>(12);
		getInventoriesForIoType(invs, iosel -> iosel::getOutputs);
		return new MultiInventoryWrapper(invs);
	}

	public Inventory getFuelInventory() {
		if (!getFurnaceIOSelector().hasFuel() && !getTableIOSelector().hasFuel()) {
			return getFurnaceInventory();
		}
		ArrayList<Inventory> invs = new ArrayList<>(13);
		getInventoriesForIoType(invs, iosel -> iosel::getFuel);
		invs.add(getFurnaceInventory());
		return new MultiInventoryWrapper(invs);
	}

	private void getInventoriesForIoType(List<Inventory> combinedInvList,
			Function<IOSelector, Function<BlockFace, Iterable<BlockFace>>> ioTypeFunc) {
		FurnCraftChestStructure fccs = (FurnCraftChestStructure) getMultiBlockStructure();
		Block fblock = getFurnace();
		BlockFace facing = getFacing();
		for (BlockFace relativeFace : ioTypeFunc.apply(getFurnaceIOSelector()).apply(facing)) {
			Block relBlock = fblock.getRelative(relativeFace);
			if (relBlock.getType() == Material.CHEST || relBlock.getType() == Material.TRAPPED_CHEST) {
				combinedInvList.add(((Chest) relBlock.getState()).getInventory());
			}
		}
		Block tblock = fccs.getCraftingTable();
		for (BlockFace relativeFace : ioTypeFunc.apply(getTableIOSelector()).apply(facing)) {
			Block relBlock = tblock.getRelative(relativeFace);
			if (relBlock.getType() == Material.CHEST || relBlock.getType() == Material.TRAPPED_CHEST) {
				combinedInvList.add(((Chest) relBlock.getState()).getInventory());
			}
		}
	}

	@Override
	public int getInputCount() {
		return (furnaceIoSelector == null ? 0 : furnaceIoSelector.getInputCount())
				+ (tableIoSelector == null ? 0 : tableIoSelector.getInputCount());
	}

	@Override
	public int getOutputCount() {
		return (furnaceIoSelector == null ? 0 : furnaceIoSelector.getOutputCount())
				+ (tableIoSelector == null ? 0 : tableIoSelector.getOutputCount());
	}

	@Override
	public int getFuelCount() {
		return (furnaceIoSelector == null ? 0 : furnaceIoSelector.getFuelCount())
				+ (tableIoSelector == null ? 0 : tableIoSelector.getFuelCount());
	}

	public void setFurnaceIOSelector(IOSelector ioSelector) {
		this.furnaceIoSelector = ioSelector;
	}

	public IOSelector getFurnaceIOSelector() {
		if (furnaceIoSelector == null) {
			furnaceIoSelector = new IOSelector();
		}
		return furnaceIoSelector;
	}

	public void setTableIOSelector(IOSelector ioSelector) {
		this.tableIoSelector = ioSelector;
	}

	public IOSelector getTableIOSelector() {
		if (tableIoSelector == null) {
			tableIoSelector = new IOSelector();
			BlockFace front = getFacing();
			BlockFace chestDir = getFurnace().getFace(getCraftingTable());
			if (chestDir != null && front != null) {
				Direction defaultDir = Direction.getDirection(front, chestDir);
				tableIoSelector.setState(defaultDir, IOSelector.IOState.BOTH);
			}
		}
		return tableIoSelector;
	}

	public void setUiMenuMode(UiMenuMode uiMenuMode) {
		this.uiMenuMode = uiMenuMode;
	}

	public UiMenuMode getUiMenuMode() {
		return uiMenuMode;
	}

	/**
	 * @return the direction the furnace (and thus this factory) is facing.
	 */
	public @Nullable BlockFace getFacing() {
		Block fblock = getFurnace();
		if (fblock.getType() != Material.FURNACE) {
			return null;
		}
		Furnace fstate = (Furnace) fblock.getState();
		org.bukkit.block.data.type.Furnace fdata = (org.bukkit.block.data.type.Furnace) fstate.getBlockData();
		return fdata.getFacing();
	}

	/**
	 * @return Inventory of the furnace or null if there is no furnace where one
	 *         should be
	 */
	public FurnaceInventory getFurnaceInventory() {
		if (getFurnace().getType() != Material.FURNACE) {
			return null;
		}
		Furnace furnaceBlock = (Furnace) (getFurnace().getState());
		return furnaceBlock.getInventory();
	}

	/**
	 * Sets autoselect mode for this factory
	 * 
	 * @param mode
	 *            Whether autoselect should be set to true or false
	 */
	public void setAutoSelect(boolean mode) {
		this.autoSelect = mode;
	}

	/**
	 * @return Whether the factory is in auto select mode
	 */
	public boolean isAutoSelect() {
		return autoSelect;
	}

	/**
	 * Attempts to turn the factory on and does all the checks needed to ensure
	 * that the factory is allowed to turn on
	 */
	@Override
	public void attemptToActivate(Player p, boolean onStartUp) {
		LoggingUtils.debug((p != null ? p.getName() : "Redstone") + " is attempting to activate " + getLogData());
		mbs.recheckComplete();
		//dont activate twice
		if (active) {
			return;
		}
		//ensure factory is physically complete
		if (!mbs.isComplete()) {
			rm.breakIt();
			return;
		}

		//If Autoselect is on
		if (autoSelect) {
			//If the factory is in disrepair and we got autoSelect on, we want to repair it
			if (rm.inDisrepair() && !(currentRecipe instanceof RepairRecipe)) {
				IRecipe autoRepair = getRepairRecipe();
				//Just incase any factory for some reason cannot be repaired.
				if (autoRepair == null) {
					if (p != null) {
						p.sendMessage(ChatColor.RED + "The factory doesn't have a repair recipe.");
					}
					return;
				} else {
					if (p != null) {
						p.sendMessage(ChatColor.GOLD + "Automatically selected recipe " + autoRepair.getName());
					}
					setRecipe(autoRepair);
				}
			}
			if (!hasInputMaterials() || (!rm.inDisrepair() && (currentRecipe instanceof  RepairRecipe))) {
				//Let autoselect find something to run that isn't the repair recipe
				IRecipe autoSelected = getAutoSelectRecipe();
				if (autoSelected == null) {
					if (p != null) {
						p.sendMessage(ChatColor.RED + "Not enough materials available to run any recipe");
					}
					return;
				} else {
					if (p != null) {
						p.sendMessage(ChatColor.GOLD + "Automatically selected recipe " + autoSelected.getName());
					}
					setRecipe(autoSelected);
				}
			}
		} else {
			//We are running the factory manually, so we just do the usual checks
			if (!hasInputMaterials()) {
				if (p != null) {
					p.sendMessage(ChatColor.RED + "Not enough materials available");
				}
				return;
			}
			//The factory is broken and needs to be repaired
			if (rm.inDisrepair() && !(currentRecipe instanceof RepairRecipe)) {
				if (p != null) {
					p.sendMessage(ChatColor.RED + "This factory is in disrepair, you have to repair it before using it");
				}
				return;
			}
			//The factory is at full health, so there's no reason to repair it.
			if (rm.atFullHealth() && (currentRecipe instanceof RepairRecipe) ) {
				if (p != null) {
					p.sendMessage(ChatColor.GREEN + "This factory is already at full health");
				}
				return;
			}
		}

		//ensure we have fuel
		if (!pm.powerAvailable(1)) {
			if (p != null) {
				ItemStack fuel = ((FurnacePowerManager) pm).getFuel().clone();
				if (fuel != null) {
					p.sendMessage(ChatColor.RED + "Failed to activate factory, there is no fuel (" + ItemUtils.getItemName(fuel) + ") in the furnace");
				}else{
					p.sendMessage(ChatColor.RED + "Failed to activate factory, there is no fuel in the furnace");
				}
			}
			return;
		}

		// Ensure the recipe effect can be applied
		var effectFeasibility = currentRecipe.evaluateEffectFeasibility(getInputInventory(), getOutputInventory());
		if (!(effectFeasibility.isFeasible())) {
			LoggingUtils.log(String.format("Skipping activation of recipe [%s], since the effect wasn't feasible.", currentRecipe.getName()));
			if (p != null) {
				p.sendMessage(String.format("%sUnable to activate recipe because %s.",ChatColor.RED, effectFeasibility.reasonSnippet()));
			}
			return;
		}

		if (!onStartUp && currentRecipe instanceof Upgraderecipe && FactoryMod.getInstance().getManager().isCitadelEnabled()) {
			// only allow permitted members to upgrade the factory
			Reinforcement rein = ReinforcementLogic.getReinforcementAt(mbs.getCenter());
			if (rein != null) {
				if (p == null) {
					return;
				}
				if (!NameAPI.getGroupManager().hasAccess(rein.getGroup().getName(), p.getUniqueId(),
						PermissionType.getPermission("UPGRADE_FACTORY"))) {
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
			int consumptionIntervall = ((InputRecipe) currentRecipe).getFuelConsumptionIntervall() > 0 ? ((InputRecipe) currentRecipe)
					.getFuelConsumptionIntervall() : pm.getPowerConsumptionIntervall();
			if (((FurnacePowerManager) pm).getFuelAmountAvailable() < (currentRecipe.getProductionTime() / consumptionIntervall)) {
				p.sendMessage(ChatColor.RED
						+ "You don't have enough fuel, the factory will run out of it before completing");
			}
			p.sendMessage(ChatColor.GREEN + "Activated " + name + " with recipe: " + currentRecipe.getName());
			activator = p.getUniqueId();
		}
		activate();
	}

	/**
	 * Actually turns the factory on, never use this directly unless you know
	 * what you are doing, use attemptToActivate() instead to ensure the factory
	 * is allowed to turn on
	 */
	@Override
	public void activate() {
		LoggingUtils.log("Activating " + getLogData() + ", because of " + (activator != null ? 
				Bukkit.getPlayer(activator) : "Redstone"));
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
	@Override
	public void deactivate() {
		if (active) {
			LoggingUtils.log("Deactivating " + getLogData());
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
	 * @return The crafting table of this factory
	 */
	public Block getCraftingTable() {
		return ((FurnCraftChestStructure) mbs).getCraftingTable();
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
	@Override
	public void run() {
		if (active && mbs.isComplete()) {
			// if the materials required to produce the current recipe are in
			// the factory inventory
			if (hasInputMaterials()) {
				// if the factory has been working for less than the required
				// time for the recipe
				if (currentProductionTimer < currentRecipe.getProductionTime()) {
					int consumptionIntervall = ((InputRecipe) currentRecipe).getFuelConsumptionIntervall() > 0
							? ((InputRecipe) currentRecipe).getFuelConsumptionIntervall()
							: pm.getPowerConsumptionIntervall();

					int powerCounter = pm.getPowerCounter() + updateTime;
					int fuelCount = powerCounter / consumptionIntervall;

					// if the factory power source inventory has enough fuel for
					// at least 1 energyCycle
					if (pm.powerAvailable(fuelCount)) {
						// check whether the furnace is on, minecraft sometimes
						// turns it off
						turnFurnaceOn(getFurnace());
						// if we need to consume fuel - then do it
						if (fuelCount >= 1) {
							// remove fuel.
							pm.consumePower(fuelCount);
							// update power counter to remained time
							pm.setPowerCounter(powerCounter % consumptionIntervall);
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
						sendActivatorMessage(ChatColor.GOLD + name + " deactivated, because it ran out of fuel");
						deactivate();
					}
				}

				// if the production timer has reached the recipes production
				// time remove input from chest, and add output material
				else {
					LoggingUtils.log("Executing recipe " + currentRecipe.getName() + " for " + getLogData());
					RecipeExecuteEvent ree = new RecipeExecuteEvent(this, (InputRecipe) currentRecipe);
					Bukkit.getPluginManager().callEvent(ree);
					if (ree.isCancelled()) {
						LoggingUtils.log("Executing recipe " + currentRecipe.getName() + " for " + getLogData()
								+ " was cancelled over the event");
						deactivate();
						return;
					}
					sendActivatorMessage(ChatColor.GOLD + currentRecipe.getName() + " in " + name + " completed");
					if (currentRecipe instanceof Upgraderecipe || currentRecipe instanceof RecipeScalingUpgradeRecipe) {
						// this if else might look a bit weird, but because
						// upgrading changes the current recipe and a lot of
						// other stuff, this is needed
						currentRecipe.applyEffect(getInputInventory(), getOutputInventory(), this);
						deactivate();
						return;
					} else {
						if (currentRecipe.applyEffect(getInputInventory(), getOutputInventory(), this)) {
							runCount.put(currentRecipe, runCount.get(currentRecipe) + 1);
						} else {
							sendActivatorMessage(ChatColor.RED + currentRecipe.getName() + " in " + name + " deactivated because it ran out of storage space");
							deactivate();
							return;
						}
					}
					currentProductionTimer = 0;
					if (currentRecipe instanceof RepairRecipe && rm.atFullHealth()) {
						// already at full health, dont try to repair further
						sendActivatorMessage(ChatColor.GOLD + name + " repaired to full health");
						deactivate();
						return;
					}
					if (pm.powerAvailable(1)) {
						//not enough materials, but if auto select is on, we might find another recipe to run
						if (!hasInputMaterials() && isAutoSelect())  {
							IRecipe nextOne = getAutoSelectRecipe();
							if (nextOne != null) {
								sendActivatorMessage(ChatColor.GREEN + name + " automatically switched to recipe " + nextOne.getName() + " and began running it");
								currentRecipe = nextOne;
							}
							else {
								deactivate();
								return;
							}
						}
						pm.setPowerCounter(0);
						scheduleUpdate();
						// keep going
					} else {
						deactivate();
					}
				}
			} else {
				IRecipe nextOne;
				if (isAutoSelect() && (nextOne = getAutoSelectRecipe()) != null)  {
					sendActivatorMessage(ChatColor.GREEN + name + " automatically switched to recipe " + nextOne.getName() + " and began running it");
					currentRecipe = nextOne;
					scheduleUpdate();
					// don't setPowerCounter to 0, fuel has been consumed, let it be used for the new recipe
				} else {
					sendActivatorMessage(ChatColor.GOLD + name + " deactivated, because it ran out of required materials");
					deactivate();
				}
			}
		} else {
			sendActivatorMessage(ChatColor.GOLD + name + " deactivated, because the factory was destroyed");
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
		Integer level = recipeLevel.get(r);
		return level != null ? level: 1;
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
	 * 
	 * @param rec
	 *            Recipe to add
	 */
	public void addRecipe(IRecipe rec) {
		recipes.add(rec);
		runCount.put(rec, 0);
		recipeLevel.put(rec, 1);
	}

	/**
	 * Removes the given recipe from this factory
	 * 
	 * @param rec
	 *            Recipe to remove
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
		return currentRecipe.enoughMaterialAvailable(getInputInventory());
	}

	/**
	 * @return a recipe which the factory contains enough ressources to run except repair type recipes,
	 * returns null if none exists
	 *
	 */

	public IRecipe getAutoSelectRecipe() {
		for (IRecipe rec : recipes) {
			if (rec.enoughMaterialAvailable(getInventory()) && !(rec instanceof RepairRecipe)) {
				return rec;
			}
		}
		return null;
	}

	/**
	 * @return yields a repair type recipe for repairing the factory if any exists, returns null if none exists
	 *
	 */
	public IRecipe getRepairRecipe() {
		for (IRecipe rec : recipes) {
			if (rec instanceof RepairRecipe) {
				return rec;
			}
		}
		return null;
	}

	public static void removePylon(Factory f) {
		pylonFactories.remove(f);
	}

	public void upgrade(String name, List<IRecipe> recipes, ItemStack fuel, int fuelConsumptionIntervall,
			int updateTime, int maximumHealth, int damageAmountPerDecayIntervall, long gracePeriod,
			double citadelBreakReduction) {
		LoggingUtils.log("Upgrading " + getLogData() + " to " + name);
		pylonFactories.remove(this);
		deactivate();
		this.name = name;
		this.recipes = recipes;
		this.updateTime = updateTime;
		this.citadelBreakReduction = citadelBreakReduction;
		this.pm = new FurnacePowerManager(getFurnace(), fuel, fuelConsumptionIntervall);
		this.rm = new PercentageHealthRepairManager(maximumHealth, maximumHealth, 0, damageAmountPerDecayIntervall,
				gracePeriod);
		if (!recipes.isEmpty()) {
			setRecipe(recipes.get(0));
		} else {
			currentRecipe = null;
		}
		runCount = new HashMap<>();
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

	public enum UiMenuMode {
		SIMPLE(Material.PAPER, "Show simple menu"),
		IOCONFIG(Material.HOPPER, "Show IO config menu");

		public final Material uiMaterial;
		public final String uiDescription;

		private UiMenuMode(Material uiMaterial, String uiDescription) {
			this.uiMaterial = uiMaterial;
			this.uiDescription = uiDescription;
		}
	}
}
