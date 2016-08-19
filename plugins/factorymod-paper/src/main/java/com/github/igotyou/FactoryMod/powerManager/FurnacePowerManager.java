package com.github.igotyou.FactoryMod.powerManager;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
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

	public int getPowerCounter() {
		return powerCounter;
	}

	public boolean powerAvailable() {
		if (furnace.getType() != Material.FURNACE
				&& furnace.getType() != Material.BURNING_FURNACE) {
			return false;
		}
		FurnaceInventory fi = ((Furnace) furnace.getState()).getInventory();
		ItemMap im = new ItemMap();
		im.addItemStack(fi.getFuel());
		im.addItemStack(fi.getSmelting());
		return im.getAmount(fuel) != 0;
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

	public void consumePower() {
		FurnaceInventory fi = ((Furnace) furnace.getState()).getInventory();
		fi.removeItem(fuel);
	}
	
	public int getFuelAmountAvailable() {
		return new ItemMap(((Furnace) furnace.getState()).getInventory()).getAmount(fuel);
	}

}
