package com.github.igotyou.FactoryMod.powerManager;

public interface IPowerManager {
	public void consumeFuel();

	public boolean powerAvailable();

	public boolean fuelAvailable();

	public int getPowerConsumptionIntervall();

	public int getTimeInCurrentPowerIntervall();

}
