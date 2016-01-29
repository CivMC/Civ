package com.github.igotyou.FactoryMod.repairManager;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.Factory;

public class NoRepairDestroyOnBreakManager implements IRepairManager {
	private Factory factory;

	public NoRepairDestroyOnBreakManager(Factory factory) {
		this.factory = factory;
	}

	public NoRepairDestroyOnBreakManager() {
		// we have to offer this explicitly as a possible constructor if we also
		// want the one which directly defines the factory
	}

	public void setFactory(Factory factory) {
		this.factory = factory;
	}

	public void breakIt() {
		if (factory.getMultiBlockStructure().relevantBlocksDestroyed()) {
			FactoryMod.getManager().removeFactory(factory);
		}
	}

	public boolean atFullHealth() {
		return true;
	}

	public boolean inDisrepair() {
		return false;
	}

	public String getHealth() {
		return "full";
	}

}
