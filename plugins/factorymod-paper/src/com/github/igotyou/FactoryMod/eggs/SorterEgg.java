package com.github.igotyou.FactoryMod.eggs;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.Sorter;
import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.interactionManager.SorterInteractionManager;
import com.github.igotyou.FactoryMod.powerManager.FurnacePowerManager;
import com.github.igotyou.FactoryMod.powerManager.IPowerManager;
import com.github.igotyou.FactoryMod.repairManager.IRepairManager;
import com.github.igotyou.FactoryMod.repairManager.NoRepairDestroyOnBreakManager;
import com.github.igotyou.FactoryMod.structures.BlockFurnaceStructure;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;

public class SorterEgg implements IFactoryEgg {
	private String name;
	private int updateTime;
	private ItemStack fuel;
	private int fuelConsumptionIntervall;
	private int sortTime;
	private int matsPerSide;
	private int sortAmount;
	private double returnRate;

	public SorterEgg(String name, int updateTime, ItemStack fuel,
			int fuelConsumptionIntervall, int sortTime, int matsPerSide,
			int sortAmount, double returnRate) {
		this.name = name;
		this.fuel = fuel;
		this.updateTime = updateTime;
		this.fuelConsumptionIntervall = fuelConsumptionIntervall;
		this.sortTime = sortTime;
		this.sortAmount = sortAmount;
		this.returnRate = returnRate;
		this.matsPerSide = matsPerSide;
	}

	public Factory hatch(MultiBlockStructure mbs, Player p) {
		IInteractionManager im = new SorterInteractionManager();
		IRepairManager rm = new NoRepairDestroyOnBreakManager();
		IPowerManager pm = new FurnacePowerManager(
				((BlockFurnaceStructure) mbs).getFurnace(), fuel,
				fuelConsumptionIntervall);
		Sorter sorter = new Sorter(im, rm, pm, mbs, updateTime, name, sortTime,
				matsPerSide, sortAmount);
		((NoRepairDestroyOnBreakManager) rm).setFactory(sorter);
		((SorterInteractionManager) im).setSorter(sorter);
		return sorter;
	}

	public Factory revive(List<Location> blocks,
			Map<BlockFace, ItemMap> assignments, int runTime) {
		MultiBlockStructure ps = new BlockFurnaceStructure(blocks);
		SorterInteractionManager im = new SorterInteractionManager();
		IRepairManager rm = new NoRepairDestroyOnBreakManager();
		IPowerManager pm = new FurnacePowerManager(
				((BlockFurnaceStructure) ps).getFurnace(), fuel,
				fuelConsumptionIntervall);
		Sorter sorter = new Sorter(im, rm, pm, ps, updateTime, name, sortTime,
				matsPerSide, sortAmount);
		((SorterInteractionManager) im).setSorter(sorter);
		((NoRepairDestroyOnBreakManager) rm).setFactory(sorter);
		sorter.setAssignments(assignments);
		if (runTime != 0) {
			sorter.attemptToActivate(null);
			if (sorter.isActive()) {
				sorter.setRunTime(runTime);
			}
		}
		return sorter;
	}

	public String getName() {
		return name;
	}

	public int getUpdateTime() {
		return updateTime;
	}

	public ItemStack getFuel() {
		return fuel;
	}

	public double getReturnRate() {
		return returnRate;
	}

	public int getFuelConsumptionIntervall() {
		return fuelConsumptionIntervall;
	}

	public int getSortTime() {
		return sortTime;
	}

	public int getMaterialsPerSide() {
		return matsPerSide;
	}

	public int getSortAmount() {
		return sortAmount;
	}

	public Class getMultiBlockStructure() {
		return BlockFurnaceStructure.class;
	}

}
