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
 * Quite a few places publish partial lists of active proxies that can be used to disguise IP address.
 * This framework allows the loading of many scrapers that will visit those sites, extract the proxies,
 * and add them to our banlists for a time; similar to Tor, but with more scraping magic instead of the
 * pretty lists that Tor publishes.
 * 
 * This is set up to be auto-loading modular like BanStickProxyHandler, so if you want to register
 * more scrapers just code 'm up and add them to the .scraper package. Be sure they extend
 * {@link com.programmerdan.minecraft.banstick.handler.ScraperWorker} class.
 * 
 * @author ProgrammerDan
 *
 */
public class BanStickScrapeHandler {

	ArrayList<ScraperWorker> workers;
	
	public BanStickScrapeHandler(FileConfiguration config, ClassLoader classes) {
		setup(config.getConfigurationSection("scrapers"), classes);
	}
	
	private void setup(ConfigurationSection config, ClassLoader classes) {
		if (config == null || !config.getBoolean("enable", false)) {
			BanStick.getPlugin().warning("All Scraper Workers disabled");
			return;
		}
		
		workers = new ArrayList<ScraperWorker>();
		
		// now load all configured proxy list loaders.
		// Build using constructor then launch repeating task
		//  if no exception thrown.
		try {
			ClassPath getSamplersPath = ClassPath.from(classes);

			for (ClassPath.ClassInfo clsInfo : getSamplersPath.getTopLevelClasses("com.programmerdan.minecraft.banstick.scraper")) {
				Class<?> clazz = clsInfo.load();
				BanStick.getPlugin().info("Found a scraper worker class {0}, attempting to find a suitable constructor", clazz.getName());
				if (clazz != null && ScraperWorker.class.isAssignableFrom(clazz)) {
					ScraperWorker loader = null;
					try {
						Constructor<?> constructBasic = clazz.getConstructor(ConfigurationSection.class);
						loader = (ScraperWorker) constructBasic.newInstance(config);
						BanStick.getPlugin().info("Created a new scraper worker of type {0}", clazz.getName());
					} catch (Exception e) {
						BanStick.getPlugin().info("Failed to initialize a scraper worker of type {0}", clazz.getName());
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
		for (ScraperWorker task : workers) {
			try {
				task.shutdown();
			} catch (Exception e) {}
		}
	}
}
