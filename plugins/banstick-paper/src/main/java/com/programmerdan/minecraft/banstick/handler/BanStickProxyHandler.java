package com.programmerdan.minecraft.banstick.handler;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;


import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.reflect.ClassPath;
import com.programmerdan.minecraft.banstick.BanStick;

public class BanStickProxyHandler {

	ArrayList<ProxyLoader> loaders;
	ArrayList<BukkitTask> loaderTasks;
	
	public BanStickProxyHandler(FileConfiguration config, ClassLoader classes) {
		setup(config.getConfigurationSection("proxy"), classes);
	}
	
	private void setup(ConfigurationSection config, ClassLoader classes) {
		if (config == null || !config.getBoolean("enable", false)) {
			BanStick.getPlugin().warning("All Proxy List Loaders disabled");
			return;
		}
		
		loaders = new ArrayList<ProxyLoader>();
		loaderTasks = new ArrayList<BukkitTask>();
		
		
		// now load all configured proxy list loaders.
		// Build using constructor then launch repeating task
		//  if no exception thrown.
		try {
			ClassPath getSamplersPath = ClassPath.from(classes);

			for (ClassPath.ClassInfo clsInfo : getSamplersPath.getTopLevelClasses("com.programmerdan.minecraft.banstick.proxy")) {
				Class<?> clazz = clsInfo.load();
				BanStick.getPlugin().info("Found a proxy loader class {0}, attempting to find a suitable constructor", clazz.getName());
				if (clazz != null && ProxyLoader.class.isAssignableFrom(clazz)) {
					ProxyLoader loader = null;
					try {
						Constructor<?> constructBasic = clazz.getConstructor(ConfigurationSection.class);
						loader = (ProxyLoader) constructBasic.newInstance(config);
						BanStick.getPlugin().info("Created a new proxy loader of type {0}", clazz.getName());
					} catch (Exception e) {
						BanStick.getPlugin().info("Failed to initialize a proxy loader of type {0}", clazz.getName());
						BanStick.getPlugin().warning("  Failure message: ", e.getMessage());
					}


					if (loader != null) {
						try {
							BukkitTask loaderTask = loader.runTaskTimerAsynchronously(BanStick.getPlugin(), loader.getDelay(), loader.getPeriod());
							loaderTasks.add(loaderTask);
						} catch (Exception e) {
							BanStick.getPlugin().warning("Failed to activate proxy loader of type {0}", clazz.getName());
							BanStick.getPlugin().warning("  Failure message: ", e);
						}
					}
				}
			}
		} catch (IOException ioe) {
			BanStick.getPlugin().warning("Failed to load any proxy loaders, due to IO error", ioe);
		}
	}
	
	public void shutdown() {
		if (loaderTasks == null) return;
		for (BukkitTask task : loaderTasks) {
			try {
				task.cancel();
			} catch (Exception e) {}
		}
	}
}
