package com.github.igotyou.FactoryMod.repairManager;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;

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
		FactoryMod
				.getPlugin()
				.getServer()
				.getScheduler()
				.scheduleSyncDelayedTask(FactoryMod.getPlugin(),
						new Runnable() {

							@Override
							public void run() {
								if (factory.getMultiBlockStructure()
										.relevantBlocksDestroyed()) {
									LoggingUtils.log(factory.getLogData()
											+ " removed because blocks were destroyed");
									FactoryMod.getManager().removeFactory(
											factory);
									PercentageHealthRepairManager
											.returnStuff(factory);
								}

							}
						});
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
