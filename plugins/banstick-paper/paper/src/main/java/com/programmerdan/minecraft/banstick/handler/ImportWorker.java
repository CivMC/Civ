package com.programmerdan.minecraft.banstick.handler;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

/**
 * ImportWorker definition, used elsewhere
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public abstract class ImportWorker implements Runnable {

	private BukkitTask importTask;
	private long delay = 100L;
	private boolean enable;
	
	/**
	 * Set up an import worker, using its name.
	 * 
	 * @param config the config to load.
	 */
	public ImportWorker(ConfigurationSection config) {
		if (config != null && setup(config.getConfigurationSection(name()))) {
			enable = internalSetup(config.getConfigurationSection(name()));
		} else {
			enable = false;
		}
	}
	
	private boolean setup(ConfigurationSection config) {
		if (config == null) {
			return false;
		}
		delay = config.getLong("delay", delay);
		return config.getBoolean("enable", enable);
	}
	
	@Override
	public void run() {
		if (enable) {
			doImport();
		}
	}
	
	public abstract boolean internalSetup(ConfigurationSection config);

	public abstract void doImport();
	
	public abstract String name();

	public long getDelay() {
		return 0;
	}

	public void setTask(BukkitTask task) {
		importTask = task;
	}

	/**
	 * Attempt to shut this worker down.
	 */
	public void shutdown() {
		try {
			if (enable && importTask != null) {
				importTask.cancel();
			}
		} catch (Exception e) {
			// shutdown? woops/
		}
	}

}
