package com.programmerdan.minecraft.banstick.handler;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.reflect.ClassPath;
import com.programmerdan.minecraft.banstick.BanStick;

/**
 * Many folks might be migrating from an existing ban management system to BanStick; this is meant
 * to enable such migration easily.
 * 
 * This is set up to be auto-loading modular like BanStickProxyHandler, so if you want to register
 * more importers just code 'm up and add them to the .importer package. Be sure they extend
 * {@link com.programmerdan.minecraft.banstick.handler.ImportWorker} class.
 * 
 * @author ProgrammerDan
 *
 */
public class BanStickImportHandler {

	ArrayList<ImportWorker> workers;
	
	public BanStickImportHandler(FileConfiguration config, ClassLoader classes) {
		setup(config.getConfigurationSection("import"), classes);
	}
	
	private void setup(ConfigurationSection config, ClassLoader classes) {
		if (config == null || !config.getBoolean("enable", false)) {
			BanStick.getPlugin().warning("All Import Workers disabled");
			return;
		}
		
		workers = new ArrayList<ImportWorker>();
		

		try {
			ClassPath getSamplersPath = ClassPath.from(classes);

			for (ClassPath.ClassInfo clsInfo : getSamplersPath.getTopLevelClasses("com.programmerdan.minecraft.banstick.importer")) {
				Class<?> clazz = clsInfo.load();
				BanStick.getPlugin().info("Found an import worker class {0}, attempting to find a suitable constructor", clazz.getName());
				if (clazz != null && ImportWorker.class.isAssignableFrom(clazz)) {
					ImportWorker loader = null;
					try {
						Constructor<?> constructBasic = clazz.getConstructor(ConfigurationSection.class);
						loader = (ImportWorker) constructBasic.newInstance(config);
						BanStick.getPlugin().info("Created a new import worker of type {0}", clazz.getName());
					} catch (Exception e) {
						BanStick.getPlugin().info("Failed to initialize an import worker of type {0}", clazz.getName());
						BanStick.getPlugin().warning("  Failure message: ", e.getMessage());
					}


					if (loader != null) {
						try {
							BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(BanStick.getPlugin(), loader, loader.getDelay());
							loader.setTask(task);
						} catch (Exception e) {
							BanStick.getPlugin().warning("Failed to activate scraper worker of type {0}", clazz.getName());
							BanStick.getPlugin().warning("  Failure message: ", e);
						}
					}
				}
			}
		} catch (IOException ioe) {
			BanStick.getPlugin().warning("Failed to load any scraper workers, due to IO error", ioe);
		}
	}
	
	public void shutdown() {
		if (this.workers == null) return;
		for (ImportWorker task : workers) {
			try {
				task.shutdown();
			} catch (Exception e) {}
		}
	}
}
