package com.devotedmc.ExilePearl.storage;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

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
	
	private Document doc = new Document();
	
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
		
		FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
		doc = Document.configurationSectionToDocument(fileConfig);
		
		for(Entry<String, Object> entry : doc.entrySet()) {
			try {
				UUID playerId = UUID.fromString(entry.getKey());
				pearls.add(pearlFactory.createExilePearl(playerId, (Document)entry.getValue()));
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Failed to load pearl record: %s", doc.get(entry.getKey()));
				ex.printStackTrace();
			}
		}
		
		return pearls;
	}

	@Override
	public void pearlInsert(ExilePearl pearl) {
		Document pearlDoc = new Document()
				.append("killer_id", pearl.getKillerUniqueId().toString())
				.append("pearl_id", pearl.getPearlId())
				.append("location", pearl.getLocation())
				.append("health", pearl.getHealth())
				.append("pearled_on", pearl.getPearledOn())
				.append("freed_offline", pearl.getFreedOffline());
		
		doc.append(pearl.getPlayerId().toString(), pearlDoc);
		writeFile();
	}

	@Override
	public void pearlRemove(ExilePearl pearl) {
		doc.remove(pearl.getPlayerId().toString());
		writeFile();
	}

	@Override
	public void pearlUpdateLocation(ExilePearl pearl) {		
		doc.getDocument(pearl.getPlayerId().toString()).append("location", pearl.getLocation());
		writeFile();
	}

	@Override
	public void pearlUpdateHealth(ExilePearl pearl) {
		doc.getDocument(pearl.getPlayerId().toString()).append("health", pearl.getHealth());
		writeFile();
	}

	@Override
	public void pearlUpdateFreedOffline(ExilePearl pearl) {
		doc.getDocument(pearl.getPlayerId().toString()).append("freed_offline", pearl.getFreedOffline());
		writeFile();
	}
	
	private void writeFile() {
		YamlConfiguration config = new YamlConfiguration();
		Document.documentToConfigurationSection(config, doc);
		try {
			config.save(file);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to write pearl data to file");
			e.printStackTrace();
		}
	}

	@Override
	public boolean connect() {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				return false;
			}
		}
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
