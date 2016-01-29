package com.github.igotyou.FactoryMod.repairManager;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.Factory;

public class PercentageHealthRepairManager implements IRepairManager {
	private int health;
	private Factory factory;

	public PercentageHealthRepairManager(int initialHealth) {
		health = initialHealth;
	}

	public boolean atFullHealth() {
		return health >= 100;
	}

	public boolean inDisrepair() {
		return health <= 0;
	}
	
	public void setFactory(Factory factory) {
		this.factory = factory;
	}

	public String getHealth() {
		return String.valueOf(health) + " %";
	}

	public void repair(int amount) {
		health = Math.min(health + amount, 100);
	}

	public void breakIt() {
		health = 0;
		if (factory.getMultiBlockStructure().relevantBlocksDestroyed()) {
			FactoryMod.getManager().removeFactory(factory);
		}
	}
	
	public int getRawHealth() {
		return health;
	}
	
	public void setHealth(int health) {
		this.health = health;
	}
}
