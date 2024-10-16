package com.devotedmc.ExilePearl.storage;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlLogger;
import com.devotedmc.ExilePearl.config.Document;
import com.google.common.base.Preconditions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

/**
 * File storage for pearls. Not done yet
 * @author Gordon
 *
 */
class FileStorage implements PluginStorage {

	private final File pearlFile;
	private final PearlFactory pearlFactory;
	private final PearlLogger logger;

	private Document doc = new Document();
	private Document pearlDoc;

	public FileStorage(final File file, final PearlFactory pearlFactory, final PearlLogger logger) {
		Preconditions.checkNotNull(file, "file");
		Preconditions.checkNotNull(pearlFactory, "pearlFactory");
		Preconditions.checkNotNull(logger, "logger");

		this.pearlFile = file;
		this.pearlFactory = pearlFactory;
		this.logger = logger;
	}

	@Override
	public Collection<ExilePearl> loadAllPearls() {
		HashSet<ExilePearl> pearls = new HashSet<ExilePearl>();

		FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(pearlFile);
		doc = new Document(fileConfig);
		pearlDoc = doc.getDocument("pearls");
		if (pearlDoc == null) {
			pearlDoc = new Document();
			doc.append("pearls", pearlDoc);
			writeFile();
			return pearls;
		}

		for(Entry<String, Object> entry : pearlDoc.entrySet()) {
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
		Document insert = new Document()
				.append("player_name", pearl.getPlayerName()) // Not needed, just makes it easier to search file
				.append("killer_id", pearl.getKillerId().toString())
				.append("pearl_id", pearl.getPearlId())
				.append("type", pearl.getPearlType().toInt())
				.append("location", pearl.getLocation())
				.append("health", pearl.getHealth())
				.append("pearled_on", pearl.getPearledOn())
				.append("last_seen", pearl.getLastOnline())
				.append("freed_offline", pearl.getFreedOffline())
				.append("summoned", pearl.isSummoned());
		if(pearl.isSummoned()) {
			insert.append("returnLoc", pearl.getReturnLocation());
		}

		pearlDoc.append(pearl.getPlayerId().toString(), insert);
		writeFile();
	}

	@Override
	public void pearlRemove(ExilePearl pearl) {
		pearlDoc.remove(pearl.getPlayerId().toString());
		writeFile();
	}

	@Override
	public void updatePearlLocation(ExilePearl pearl) {		
		pearlDoc.getDocument(pearl.getPlayerId().toString()).append("location", pearl.getLocation());
		writeFile();
	}

	@Override
	public void updatePearlHealth(ExilePearl pearl) {
		pearlDoc.getDocument(pearl.getPlayerId().toString()).append("health", pearl.getHealth());
		writeFile();
	}

	@Override
	public void updatePearlFreedOffline(ExilePearl pearl) {
		pearlDoc.getDocument(pearl.getPlayerId().toString()).append("freed_offline", pearl.getFreedOffline());
		writeFile();
	}

	@Override
	public void updatePearlType(ExilePearl pearl) {
		pearlDoc.getDocument(pearl.getPlayerId().toString()).append("type", pearl.getPearlType().toInt());
		writeFile();
	}

	@Override
	public void updatePearlKiller(ExilePearl pearl) {
		pearlDoc.getDocument(pearl.getPlayerId().toString()).append("killer_id", pearl.getKillerId().toString());
		writeFile();
	}

	@Override
	public void updatePearlLastOnline(ExilePearl pearl) {
		pearlDoc.getDocument(pearl.getPlayerId().toString()).append("last_seen", pearl.getLastOnline());
		writeFile();
	}

	@Override
	public void updatePearlSummoned(ExilePearl pearl) {
		pearlDoc.getDocument(pearl.getPlayerId().toString()).append("summoned", pearl.isSummoned());
		writeFile();
	}

	@Override
	public void updateReturnLocation(ExilePearl pearl) {
		pearlDoc.getDocument(pearl.getPlayerId().toString()).append("returnLoc", pearl.getReturnLocation());
		writeFile();
	}

	@Override
	public void updatePearledOnDate(ExilePearl pearl) {
		pearlDoc.getDocument(pearl.getPlayerId().toString()).append("pearled_on", pearl.getPearledOn());
		writeFile();
	}

	private void writeFile() {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(pearlFile);
		doc.savetoConfig(config);
		try {
			config.save(pearlFile);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to write pearl data to file");
			e.printStackTrace();
		}
	}

	@Override
	public boolean connect() {
		doc = new Document();
		pearlDoc = new Document();
		if (!pearlFile.exists()) {
			try {
				pearlFile.createNewFile();
				try(FileWriter writer = new FileWriter(pearlFile)) {
					writer.write("# Do not edit this file directly while the server is running!");
				}
				doc.append("pearls", pearlDoc);
				writeFile();
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
