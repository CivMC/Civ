package com.github.igotyou.FactoryMod.repairManager;

public class PercentageHealthRepairManager implements IRepairManager {
	private int health;

	public PercentageHealthRepairManager(int initialHealth) {
		health = initialHealth;
	}

	public boolean atFullHealth() {
		return health >= 100;
	}

	public boolean inDisrepair() {
		return health <= 0;
	}

	public String getHealth() {
		return String.valueOf(health) + " %";
	}

	public void repair(int amount) {
		health = Math.min(health + amount, 100);
	}

	public void breakIt() {
		health = 0;
	}
	
	public int getRawHealth() {
		return health;
	}
	
	public void setHealth(int health) {
		this.health = health;
	}
}
