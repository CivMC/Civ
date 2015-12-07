package com.github.igotyou.FactoryMod.repairManager;

public interface IRepairManager {
	public int getHealth();

	public boolean atFullHealth();

	public void repair();

	public boolean inDisrepair();

}
