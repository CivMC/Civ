package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.BadBoyWatchConfig;

/**
 * Watches for bad boys by tracking block breaks.
 * FIRST HIT will also catch breaks that block protection plugins cancel, which if you skip 
 * can lead to confusion
 * 
 * @author ProgrammerDan
 *
 */
public class BadBoyWatch extends SimpleHack<BadBoyWatchConfig> implements Listener {

	/**
	 *  tracking cache, persists and ignores logouts.
	 */
	private Map<UUID, BadBoyRecord> boys = null;
	private Map<UUID, BadBoyRecord> lowBoys = null;

	public BadBoyWatch(SimpleAdminHacks plugin, BadBoyWatchConfig config) {
		super(plugin, config);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
	public void breakListenLow(BlockBreakEvent bbe) {
		if (!config.isEnabled()) return;
		try {
			Player player = bbe.getPlayer();
			Block block = bbe.getBlock();
			BlockState bs = block.getState();
			Material material = bs.getType();

			lowTrackAndReport(player.getUniqueId(), block.getLocation(), material);
		} catch (Exception e) {
			// insane catchall
			plugin().log(Level.WARNING, "Failed to track a low break for badboy", e);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void breakListen(BlockBreakEvent bbe) {
		if (!config.isEnabled()) return;
		try {
			Player player = bbe.getPlayer();
			Block block = bbe.getBlock();
			BlockState bs = block.getState();
			Material material = bs.getType();

			trackAndReport(player.getUniqueId(), block.getLocation(), material);
		} catch (Exception e) {
			// insane catchall
			plugin().log(Level.WARNING, "Failed to track a break for badboy", e);
		}
	}

	private void trackAndReport(UUID uuid, Location location, Material material) {
		if (uuid == null || location == null || material == null) return;

		BadBoyRecord record = boys.get(uuid);
		if (record == null) {
			record = new BadBoyRecord(config);
			boys.put(uuid, record);
		}

		String report = record.registerBreak(location, material);
		if (report != null) {
			plugin().log(uuid.toString() + "} " + report);
		}
	}

	private void lowTrackAndReport(UUID uuid, Location location, Material material) {
		if (uuid == null || location == null || material == null) return;

		BadBoyRecord record = lowBoys.get(uuid);
		if (record == null) {
			record = new BadBoyRecord(config);
			lowBoys.put(uuid, record);
		}

		String report = record.registerBreak(location, material);
		if (report != null) {
			plugin().log(uuid.toString() + "} FIRST HITS " + report);
		}
	}

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering BadBoyWatcher listener");
			plugin().registerListener(this);
		}
	}

	@Override
	public void dataBootstrap() {
		boys = new ConcurrentHashMap<UUID, BadBoyRecord>();
		lowBoys = new ConcurrentHashMap<UUID, BadBoyRecord>();
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
		boys.clear();
		lowBoys.clear();
	}

	@Override
	public String status() {
		if (!config.isEnabled()) {
			return "Bad Boy Listening disabled.";
		}
		StringBuffer status = new StringBuffer("Listening for bad boy breaks");

		status.append("\n  Currently watching ").append(boys.size()).append(" players.");
		status.append("\n  Currently watching LOW ").append(lowBoys.size()).append(" players.");
		return status.toString();
	}

	public static BadBoyWatchConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new BadBoyWatchConfig(plugin, config);
	}

	@Override
	public void registerCommands() {}

	private static class BadBoyRecord {
		private int nextBreak;
		private int breakCount;
		private BadBoyBlock[] breaks;
		private BadBoyWatchConfig config;

		public BadBoyRecord(BadBoyWatchConfig config) {
			this.config = config;
			this.nextBreak = 0;
			this.breakCount = 0;
			this.breaks = new BadBoyBlock[config.getTrackingDepth() + 1];
		}

		/**
		 * Returns a string if this is a watched break that passes all checks.
		 * Handles abiding the config too.
		 * @return null if not watched break or if conditions not met, string of
		 *   breaks leading to this otherwise.
		 */
		public synchronized String registerBreak(Location location, Material material) {
			breaks[nextBreak] = new BadBoyBlock(location, material);

			nextBreak = (nextBreak + 1) % breaks.length;
			breakCount ++;

			if (config.getWatchedMaterials().contains(material)) {
				String breaks = null;
				if (breakCount >= config.getMinDepthToMatch()) {
					breaks = showBreaks();
				}
				if (config.isClearOnMatch()) {
					this.clear();
					breakCount = 0;
				}
				return breaks;
			}
			return null;
		}

		/**
		 * Clears everything tracked and resets break pointer.
		 */
		private void clear() {
			for (int i = 0; i < breaks.length; i++) {
				breaks[i] = null;
			}
			nextBreak = 0;
		}

		/**
		 * Just outputs what we know right now.
		 * @return a String showing locations and block materials
		 */
		private String showBreaks() {
			StringBuilder sb = new StringBuilder("BadBoyBreak: ");
			int max = breaks.length;

			for (int i = (nextBreak - 1 + max) % max; i != nextBreak; i = (i - 1 + max) % max) {
				BadBoyBlock bbb = breaks[i];
				if (bbb == null) break;
				sb.append('[').append(bbb.toString()).append(']');
			}

			return sb.toString();
		}
	}

	private static class BadBoyBlock {
		public final Location location;
		public final Material material;

		public BadBoyBlock(Location location, Material material) {
			this.location = location;
			this.material = material;
		}

		@Override
		public String toString() {
			StringBuilder se = new StringBuilder();

			se.append(location.getWorld().getName());
			se.append('(').append(location.getBlockX()).append(',')
				.append(location.getBlockY()).append(',')
				.append(location.getBlockZ()).append(')');
			se.append(material.toString());
			return se.toString();
		}
	}
}
