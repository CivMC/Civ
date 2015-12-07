package com.github.igotyou.FactoryMod.Factorys;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.classicTriblockFactory.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.EnchantmentOptions;
import com.github.igotyou.FactoryMod.recipes.ProbabilisticEnchantment;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.AdvancedItemStack;
import com.github.igotyou.FactoryMod.utility.StringUtils;

public abstract class ABaseFactory extends FurnCraftChestFactory {
	

	public ABaseFactory(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active, FactoryType factoryType, String subFactoryType) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource,
				active, factoryType, subFactoryType);
		this.currentRepair = 0.0;
		this.timeDisrepair = 3155692597470L;
	}

	public ABaseFactory(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active, int tierLevel, FactoryType factoryType,
			Inventory factoryInventory, String subFactoryType) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource,
				active, tierLevel, factoryType, factoryInventory,
				subFactoryType);
		this.currentRepair = 0.0;
		this.timeDisrepair = 3155692597470L;
	}

	public ABaseFactory(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			FactoryType factoryType, String subFactoryType) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource,
				factoryType, subFactoryType);
		this.currentRepair = 0.0;
		this.timeDisrepair = 3155692597470L;// Year 2070, default starting value
	}

	public ABaseFactory(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			FactoryType factoryType, boolean active, String subFactoryType,
			int currentProductionTimer, int currentEnergyTimer,
			double currentMaintenance, long timeDisrepair) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource,
				factoryType, subFactoryType);
		this.active = active;
		this.currentEnergyTimer = currentEnergyTimer;
		this.currentProductionTimer = currentProductionTimer;
		this.currentRepair = currentMaintenance;
		this.timeDisrepair = timeDisrepair;
	}

	private void setActivationLever(boolean state) {
		Block lever = findActivationLever();
		if (lever != null) {
			setLever(lever, state);
			shotGunUpdate(factoryPowerSourceLocation.getBlock());
		}
	}

	

	

	public boolean checkHasMaterials() {
		return getAllInputs().allIn(getInventory());
	}

	/
	/**
	 * Returns either a success or error message. Called by the blockListener
	 * when a player left clicks the powerSourceLocation with the
	 * InteractionMaterial
	 */
	public List<InteractionResponse> togglePower() {
		List<InteractionResponse> response = new ArrayList<InteractionResponse>();
		// if the factory is turned off
		if (!active) {
			// if the factory isn't broken or the current recipe can repair it
			if (!isBroken() || isRepairing()) {
				// is there fuel enough for at least once energy cycle?
				if (isFuelAvailable()) {
					// are there enough materials for the current recipe in the
					// chest?
					if (checkHasMaterials()) {
						// turn the factory on
						powerOn();
						// return a success message
						response.add(new InteractionResponse(
								InteractionResult.SUCCESS, "Factory activated!"));
						return response;
					}
					// there are not enough materials for the recipe!
					else {
						// return a failure message, containing which materials
						// are needed for the recipe
						// [Requires the following: Amount Name, Amount Name.]
						// [Requires one of the following: Amount Name, Amount
						// Name.]

						ItemList<AdvancedItemStack> needAll = new ItemList<AdvancedItemStack>();
						ItemList<AdvancedItemStack> allInputs = getAllInputs();
						needAll.addAll(allInputs.getDifference(getInventory()));
						if (!needAll.isEmpty()) {
							response.add(new InteractionResponse(
									InteractionResult.FAILURE,
									"You need all of the following: "
											+ needAll.toString() + "."));
						} else if (allInputs == null || allInputs.isEmpty()) {
							log.warning("getAllInputs() returned null or empty; recipe is returning no expectation of input!");
						}
						return response;
					}
				}
				// if there isn't enough fuel for at least one energy cycle
				else {
					// return a error message
					int multiplesRequired = (int) Math.ceil(getProductionTime()
							/ (double) getEnergyTime());
					response.add(new InteractionResponse(
							InteractionResult.FAILURE,
							"Factory is missing fuel! ("
									+ getFuel().getMultiple(multiplesRequired)
											.toString() + ")"));
					return response;
				}
			} else {
				response.add(new InteractionResponse(InteractionResult.FAILURE,
						"Factory is in disrepair!"));
				return response;
			}
		}
		// if the factory is on already
		else {
			// turn the factory off
			powerOff();
			// return success message
			response.add(new InteractionResponse(InteractionResult.FAILURE,
					"Factory has been deactivated!"));
			return response;
		}
	}

	public abstract ItemList<AdvancedItemStack> getFuel();

	public abstract ItemList<AdvancedItemStack> getInputs();

	public abstract ItemList<AdvancedItemStack> getOutputs();

	public abstract ItemList<AdvancedItemStack> getRepairs();

	public void consumeInputs() {
		// Remove inputs from chest
		getInputs().removeFrom(getInventory());
	}

	/**
	 * Implementations should override this to define any controls on
	 * enchantment.
	 * 
	 * @return an instance of EnchantmentOptions
	 */
	public EnchantmentOptions getEnchantmentOptions() {
		return EnchantmentOptions.DEFAULT;
	}

	public void produceOutputs() {
		// Adds outputs to chest with appropriate enchantments
		getOutputs().putIn(getInventory(), getEnchantments(),
				getEnchantmentOptions());
	}

	public ItemList<AdvancedItemStack> getAllInputs() {
		ItemList<AdvancedItemStack> allInputs = new ItemList<AdvancedItemStack>();
		allInputs.addAll(getInputs());
		allInputs.addAll(getRepairs());
		return allInputs;
	}

	

	protected void postUpdate() {
		// Hook for subtypes
	}

	protected void fuelConsumed() {
		// Hook for subtypes
	}

	public List<ProbabilisticEnchantment> getEnchantments() {
		return new ArrayList<ProbabilisticEnchantment>();
	}

	protected abstract void recipeFinished();



	

	/**
	 * Sets the toggled state of a single lever<br>
	 * <b>No Lever type check is performed</b>
	 *
	 * @param lever
	 *            block
	 * @param down
	 *            state to set to
	 */
	private static void setLever(org.bukkit.block.Block lever, boolean down) {
		if (lever.getType() != Material.LEVER) {
			return;
		}

		byte data = lever.getData();
		int newData;
		if (down) {
			newData = data | 0x8;
		} else {
			newData = data & 0x7;
		}
		if (newData != data) {
			// CraftBukkit start - Redstone event for lever
			int old = !down ? 1 : 0;
			int current = down ? 1 : 0;
			BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(lever,
					old, current);
			Bukkit.getServer().getPluginManager().callEvent(eventRedstone);
			if ((eventRedstone.getNewCurrent() > 0) != down) {
				return;
			}
			// CraftBukkit end
			lever.setData((byte) newData, true);
			lever.getState().update();
			Block attached = lever.getRelative(((Attachable) lever.getState()
					.getData()).getAttachedFace());
		}
	}

	/**
	 * returns the Location of the central block of the factory
	 */
	public Location getCenterLocation() {
		return factoryLocation;
	}

	/**
	 * returns the Location of the factory Inventory
	 */
	public Location getInventoryLocation() {
		return factoryInventoryLocation;
	}

	/**
	 * returns the Location of the factory power source
	 */
	public Location getPowerSourceLocation() {
		return factoryPowerSourceLocation;
	}

	/**
	 * Checks if there is enough fuel Available for atleast once energy cycle
	 * 
	 * @return true if there is enough fuel, false otherwise
	 */
	public boolean isFuelAvailable() {
		Inventory inv = getPowerSourceInventory();
		if (inv == null) {
			return false;
		} else {
			return getFuel().allIn(inv);
		}
	}



}
