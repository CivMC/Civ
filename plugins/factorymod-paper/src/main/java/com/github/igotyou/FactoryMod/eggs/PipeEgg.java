package com.github.igotyou.FactoryMod.eggs;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.Pipe;
import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.interactionManager.PipeInteractionManager;
import com.github.igotyou.FactoryMod.powerManager.FurnacePowerManager;
import com.github.igotyou.FactoryMod.powerManager.IPowerManager;
import com.github.igotyou.FactoryMod.repairManager.IRepairManager;
import com.github.igotyou.FactoryMod.repairManager.NoRepairDestroyOnBreakManager;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.structures.PipeStructure;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PipeEgg implements IFactoryEgg {
	private String name;
	private int updateTime;
	private ItemStack fuel;
	private int fuelConsumptionIntervall;
	private List<Material> allowedMaterials;
	private int transferTimeMultiplier;
	private int transferAmount;
	private double returnRate;
	private int maximumLength;
	private Material pipeType;

	public PipeEgg(String name, int updateTime, ItemStack fuel,
			int fuelConsumptionIntervall, List<Material> allowedMaterials,
			int transferTimeMultiplier, int transferAmount, double returnRate, int maximumLength, Material pipeType) {
		this.name = name;
		this.fuel = fuel;
		this.updateTime = updateTime;
		this.fuelConsumptionIntervall = fuelConsumptionIntervall;
		this.transferTimeMultiplier = transferTimeMultiplier;
		this.transferAmount = transferAmount;
		this.allowedMaterials = allowedMaterials;
		this.returnRate = returnRate;
		this.maximumLength = maximumLength;
		this.pipeType = pipeType;
	}

	public String getName() {
		return name;
	}

	public int getUpdateTime() {
		return updateTime;
	}

	public int getFuelConsumptionIntervall() {
		return fuelConsumptionIntervall;
	}

	public ItemStack getFuel() {
		return fuel;
	}

	public List<Material> getAllowedMaterials() {
		return allowedMaterials;
	}

	public double getReturnRate() {
		return returnRate;
	}

	public int getTransferAmount() {
		return transferAmount;
	}

	public int getTransferTimeMultiplier() {
		return transferTimeMultiplier;
	}

	public int getMaximumLength() {
		return maximumLength;
	}

	public Material getPipeType() {
		return pipeType;
	}

	public Factory hatch(MultiBlockStructure mbs, Player p) {
		IInteractionManager im = new PipeInteractionManager();
		IRepairManager rm = new NoRepairDestroyOnBreakManager();
		IPowerManager pm = new FurnacePowerManager(
				((PipeStructure) mbs).getFurnace(), fuel,
				fuelConsumptionIntervall);
		Pipe pipe = new Pipe(im, rm, pm, mbs, updateTime, name,
				transferTimeMultiplier, transferAmount);
		((PipeInteractionManager) im).setPipe(pipe);
		((NoRepairDestroyOnBreakManager)rm).setFactory(pipe);
		return pipe;
	}

	public Factory revive(List<Location> blocks, List<Material> allowedMaterials,
			int runTime) {
		MultiBlockStructure ps = new PipeStructure(blocks);
		PipeInteractionManager im = new PipeInteractionManager();
		IRepairManager rm = new NoRepairDestroyOnBreakManager();
		IPowerManager pm = new FurnacePowerManager(
				((PipeStructure) ps).getFurnace(), fuel,
				fuelConsumptionIntervall);
		Pipe pipe = new Pipe(im, rm, pm, ps, updateTime, name,
				transferTimeMultiplier, transferAmount);
		((PipeStructure) ps).setPipeType(pipeType);
		((PipeInteractionManager) im).setPipe(pipe);
		((NoRepairDestroyOnBreakManager)rm).setFactory(pipe);
		pipe.setAllowedMaterials(allowedMaterials);
		if (runTime != 0) {
			pipe.attemptToActivate(null, true);
			if (pipe.isActive()) {
				pipe.setRunTime(runTime);
			}
		}
		return pipe;
	}

	public Class <PipeStructure> getMultiBlockStructure() {
		return PipeStructure.class;
	}
}
