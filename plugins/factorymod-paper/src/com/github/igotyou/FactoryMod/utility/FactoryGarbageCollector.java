package com.github.igotyou.FactoryMod.utility;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;

public class FactoryGarbageCollector implements Runnable {
	private int healthPerCall;
	
	public FactoryGarbageCollector(int healthPerCall) {
		this.healthPerCall = healthPerCall;
	}

	public void run() {
		long graceTime = FactoryMod.getManager().getNoHealthGracePeriod();
		for(Factory f: FactoryMod.getManager().getAllFactories()) {
			if (f.getRepairManager() instanceof PercentageHealthRepairManager) {
				PercentageHealthRepairManager rm = (PercentageHealthRepairManager) f.getRepairManager();
				long broke = rm.getBreakTime();
				if (broke != 0) {
					if (System.currentTimeMillis() - broke > graceTime) {
						//grace period is over
						LoggingUtils.log(f.getLogData() + " has been at no health for too long and is being removed");
						FactoryMod.getManager().removeFactory(f);
					}
				}
				else {
					rm.setHealth(rm.getRawHealth()-healthPerCall);
					if (rm.getRawHealth() <= 0) {
						rm.breakIt();
					}
				}
			}
		}
	}
}
