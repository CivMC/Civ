package com.devotedmc.ExilePearl.storage;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlLogger;
import com.devotedmc.ExilePearl.config.Document;

import vg.civcraft.mc.civmodcore.util.Guard;

/**
 * File storage for pearls. Not done yet
 * @author Gordon
 *
 */
class FileStorage implements PluginStorage {
	
	private final File file;
	private final PearlFactory pearlFactory;
	private final PearlLogger logger;
	
	public FileStorage(final File file, final PearlFactory pearlFactory, final PearlLogger logger) {
		Guard.ArgumentNotNull(file, "file");
		Guard.ArgumentNotNull(pearlFactory, "pearlFactory");
		Guard.ArgumentNotNull(logger, "logger");
		
		this.file = file;
		this.pearlFactory = pearlFactory;
		this.logger = logger;
	}

	@Override
	public Collection<ExilePearl> loadAllPearls() {
		HashSet<ExilePearl> pearls = new HashSet<ExilePearl>();
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		Document doc = Document.configurationSectionToDocument(config);
		
		for(String k : doc.keySet()) {
			try {
				UUID playerId = UUID.fromString(doc.getString("uid"));
				UUID killerId = UUID.fromString(doc.getString("killer_id"));
				int pearlId = doc.getInteger("pearl_id");
				World world = Bukkit.getWorld(doc.getString("world"));
				int x = doc.getInteger("x");
				int y = doc.getInteger("y");
				int z = doc.getInteger("z");
				int health = doc.getInteger("health");
				Date pearledOn = new Date(doc.getLong("pearled_on"));
				boolean freedOffline = doc.getBoolean("freed_offline");

				if (world == null) {
					logger.log(Level.WARNING, "Failed to load world for pearl %s", playerId.toString());
					continue;
				}
				Location loc = new Location(world, x, y, z);

				ExilePearl pearl = pearlFactory.createExilePearl(playerId, killerId, pearlId, loc);
				pearl.setHealth(health);
				pearl.setPearledOn(pearledOn);
				pearl.setFreedOffline(freedOffline);
				pearl.enableStorage();
				pearls.add(pearl);
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Failed to load pearl record: %s", doc.get(k));
				ex.printStackTrace();
			}
		}
		
		return pearls;
	}

	@Override
	public void pearlInsert(ExilePearl pearl) {
	}

	@Override
	public void pearlRemove(ExilePearl pearl) {
	}

	@Override
	public void pearlUpdateLocation(ExilePearl pearl) {
	}

	@Override
	public void pearlUpdateHealth(ExilePearl pearl) {
		
	}

	@Override
	public void pearlUpdateFreedOffline(ExilePearl pearl) {
		
	}

	@Override
	public boolean connect() {
		return true;
	}

	@Override
	public void disconnect() {
	}

	@Override
	public boolean isConnected() {
		return true;
	}
}
