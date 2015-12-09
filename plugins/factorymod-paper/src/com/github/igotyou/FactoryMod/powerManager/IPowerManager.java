package com.github.igotyou.FactoryMod.powerManager;

public interface IPowerManager {
	public void consumeFuel();

	public boolean fuelAvailable();

	public int getPowerConsumptionIntervall();

	public int getPowerCounter();
	
	public void incrementPowerCounter();
	
	public void setPowerCounter(int value);
	

}
