package com.github.igotyou.FactoryMod.utility;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;

public class FactoryGarbageCollector implements Runnable {

	public void run() {
		FactoryModManager manager = FactoryMod.getInstance().getManager();
		for(Factory f: manager.getAllFactories()) {
			if (f.getRepairManager() instanceof PercentageHealthRepairManager) {
				PercentageHealthRepairManager rm = (PercentageHealthRepairManager) f.getRepairManager();
				long graceTime = rm.getGracePeriod();
				long broke = rm.getBreakTime();
				if (broke != 0) {
					if (System.currentTimeMillis() - broke > graceTime) {
						//grace period is over
						LoggingUtils.log(f.getLogData() + " has been at no health for too long and is being removed");
						manager.removeFactory(f);
					}
				}
				else {
					rm.setHealth(rm.getRawHealth() - rm.getDamageAmountPerDecayIntervall());
					if (rm.getRawHealth() <= 0) {
						rm.breakIt();
					}
				}
			}
		}
	}
}
