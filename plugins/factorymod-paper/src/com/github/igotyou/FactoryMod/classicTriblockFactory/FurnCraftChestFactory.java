package com.github.igotyou.FactoryMod.classicTriblockFactory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.Contraption;
import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.repairManager.IRepairManager;

//original file:
/**
 * MachineObject.java
 * Purpose: Basic object base for machines to extend
 *
 * @author MrTwiggy
 * @version 0.1 1/14/13
 */
//edited version:
/**
 * FactoryObject.java Purpose basic object base for factories to extend
 * 
 * @author igotyou
 *
 */
public abstract class FurnCraftChestFactory extends Contraption {

	protected Location factoryInventoryLocation;
	protected Location factoryPowerSourceLocation;
	protected boolean active;
	protected Inventory factoryInventory;
	protected Inventory factoryPowerInventory;
	protected int currentProductionTimer = 0;

	/**
	 * Constructor
	 */
	public FurnCraftChestFactory(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			IInteractionManager im, IRepairManager rm) {
		super(factoryLocation, im, rm);
		this.factoryInventoryLocation = factoryInventoryLocation;
		this.factoryPowerSourceLocation = factoryPowerSource;
		this.active = false;
	}

	/**
	 * Returns the factory Inventory(normally a chest), updates the inventory
	 * variable aswell.
	 */
	public Inventory getInventory() {
		Chest chestBlock = (Chest) factoryInventoryLocation.getBlock()
				.getState();
		factoryInventory = chestBlock.getInventory();
		return factoryInventory;
	}

	/**
	 * Returns the power Source inventory, updates it aswell.
	 */
	public Inventory getPowerSourceInventory() {
		if (!(factoryPowerSourceLocation.getBlock().getType() == Material.FURNACE || factoryPowerSourceLocation
				.getBlock().getType() == Material.BURNING_FURNACE)) {
			return null;
		}
		Furnace furnaceBlock = (Furnace) factoryPowerSourceLocation.getBlock()
				.getState();
		factoryPowerInventory = furnaceBlock.getInventory();
		return factoryPowerInventory;
	}

	/**
	 * Checks whether enough space for the output is available in the chest
	 * 
	 * @return true if enough space is available, false if not
	 */
	public boolean checkSpaceForOutput() {
		// TODO
		return true;
	}
	
	/**
	 * Turns the factory on
	 */
	public void activate() {
		active = true;
		// lots of code to make the furnace light up, without loosing contents.
		Furnace furnace = (Furnace) factoryPowerSourceLocation.getBlock()
				.getState();
		byte data = furnace.getData().getData();
		ItemStack[] oldContents = furnace.getInventory().getContents();
		furnace.getInventory().clear();
		factoryPowerSourceLocation.getBlock().setType(Material.BURNING_FURNACE);
		furnace = (Furnace) factoryPowerSourceLocation.getBlock().getState();
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
			Furnace furnace = (Furnace) factoryPowerSourceLocation.getBlock()
					.getState();
			byte data = furnace.getData().getData();
			ItemStack[] oldContents = furnace.getInventory().getContents();
			furnace.getInventory().clear();
			factoryPowerSourceLocation.getBlock().setType(Material.FURNACE);
			furnace = (Furnace) factoryPowerSourceLocation.getBlock()
					.getState();
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
	 * Returns the energy timer
	 */
	public int getEnergyTimer() {
		return currentEnergyTimer;
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
	 * @return How long since energy was consumed the last time in ticks
	 */
	public int getLastEnergyConsumptionTime() {
		return currentEnergyTimer;
	}

	/**
	 * returns true if all factory blocks are occupied with the correct blocks
	 */
	public boolean isWhole() {
		return (factoryPowerSourceLocation.getBlock().getType() == Material.FURNACE || factoryPowerSourceLocation
				.getBlock().getType() == Material.BURNING_FURNACE)
				&& (factoryInventoryLocation.getBlock().getType() == Material.CHEST)
				&& (physicalLocation.getBlock().getType() == Material.WORKBENCH);
	}
	
	/**
	 * called by the manager each update cycle
	 */
	public void update() {
		// if factory is turned on
		if (active) {
			// if the materials required to produce the current recipe are in
			// the factory inventory
			if (checkHasMaterials()) {
				// if the factory has been working for less than the required
				// time for the recipe
				if (currentProductionTimer < getProductionTime()) {
					// if the factory power source inventory has enough fuel for
					// at least 1 energyCycle
					if (isFuelAvailable()) {
						// if the time since fuel was last consumed is equal to
						// how often fuel needs to be consumed
						if (currentEnergyTimer == getEnergyTime() - 1) {
							// remove one fuel.
							getFuel().removeFrom(getPowerSourceInventory());
							// 0 seconds since last fuel consumption
							currentEnergyTimer = 0;
							fuelConsumed();
						}
						// if we don't need to consume fuel, just increment the
						// energy timer
						else {
							currentEnergyTimer++;
						}
						// increment the production timer
						currentProductionTimer++;
						postUpdate();
					}
					// if there is no fuel Available turn off the factory
					else {
						powerOff();
					}
				}

				// if the production timer has reached the recipes production
				// time remove input from chest, and add output material
				else if (currentProductionTimer >= getProductionTime()) {
					consumeInputs();
					produceOutputs();
					// Repairs the factory
					repair(getRepairs().removeMaxFrom(getInventory(),
							(int) currentRepair));
					recipeFinished();

					currentProductionTimer = 0;
					powerOff();
				}
			} else {
				powerOff();
			}
		}
	}
}
