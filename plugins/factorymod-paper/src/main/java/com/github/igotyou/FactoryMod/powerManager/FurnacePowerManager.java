package com.github.igotyou.FactoryMod.powerManager;

import com.github.igotyou.FactoryMod.utility.IIOFInventoryProvider;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

/**
 * Power manager for a FurnCraftChest factory, which uses a specific item in the
 * furnace of the factory as fuel
 *
 */
public class FurnacePowerManager implements IPowerManager {
	private ItemStack fuel;
	private int powerCounter;
	private int fuelConsumptionIntervall;
	private Block furnace;
	private IIOFInventoryProvider iofProvider;

	public FurnacePowerManager(Block furnace, ItemStack fuel,
			int fuelConsumptionIntervall) {
		this.fuel = fuel;
		this.fuelConsumptionIntervall = fuelConsumptionIntervall;
		this.furnace = furnace;
	}

	public FurnacePowerManager(ItemStack fuel, int fuelConsumptionIntervall) {
		this.fuel = fuel;
		this.fuelConsumptionIntervall = fuelConsumptionIntervall;
	}

	public void setIofProvider(IIOFInventoryProvider iofProvider) {
		this.iofProvider = iofProvider;
	}

	public IIOFInventoryProvider getIofProvider() {
		return iofProvider;
	}

	public int getPowerCounter() {
		return powerCounter;
	}

	public boolean powerAvailable(int fuelCount) {
		if (iofProvider != null) {
			Inventory fuelInv = iofProvider.getFuelInventory();
			if (fuelInv != null) {
				ItemMap im = new ItemMap(fuelInv);
				return im.getAmount(fuel) >= fuelCount;
			}
		}

		if (furnace.getType() != Material.FURNACE) {
			return false;
		}

		if(fuelCount == 0)
			fuelCount = 1;

		FurnaceInventory fi = ((Furnace) furnace.getState()).getInventory();
		ItemMap im = new ItemMap();
		im.addItemStack(fi.getFuel());
		im.addItemStack(fi.getSmelting());
		return im.getAmount(fuel) >= fuelCount;
	}

	public int getPowerConsumptionIntervall() {
		return fuelConsumptionIntervall;
	}

	public void increasePowerCounter(int amount) {
		powerCounter += amount;
	}

	public void setPowerCounter(int amount) {
		powerCounter = amount;
	}

	public void consumePower(int fuelCount) {
		if (iofProvider != null) {
			Inventory fuelInv = iofProvider.getFuelInventory();
			if (fuelInv != null) {
				fuelInv.removeItem(fuel);
			}
		}

		FurnaceInventory fi = ((Furnace) furnace.getState()).getInventory();

		for(int i = 0; i < fuelCount; i++)
			fi.removeItem(fuel);
	}

	public int getFuelAmountAvailable() {
		if (iofProvider != null) {
			Inventory fuelInv = iofProvider.getFuelInventory();
			if (fuelInv != null) {
				ItemMap im = new ItemMap(fuelInv);
				return im.getAmount(fuel);
			}
		}
		return new ItemMap(((Furnace) furnace.getState()).getInventory()).getAmount(fuel);
	}

	public ItemStack getFuel() {
		return fuel;
	}

}
