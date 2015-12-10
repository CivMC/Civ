package com.github.igotyou.FactoryMod.powerManager;

public interface IPowerManager {
	public void consumePower();

	public boolean powerAvailable();

	public int getPowerConsumptionIntervall();

	public int getPowerCounter();
	
	public void increasePowerCounter(int amount);
	
	public void setPowerCounter(int value);
	

}
